package com.worklife.boundary.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.worklife.boundary.data.local.BoundaryDatabase
import com.worklife.boundary.data.local.DayOfWeekBitmask
import com.worklife.boundary.data.local.entity.AppSettingsEntity
import com.worklife.boundary.data.local.entity.BlockEventEntity
import com.worklife.boundary.data.local.entity.BlocklistEntryEntity
import com.worklife.boundary.data.local.entity.BlocklistNumberEntity
import com.worklife.boundary.domain.phone.PhoneNormalizer
import com.worklife.boundary.domain.policy.BlocklistEntry
import com.worklife.boundary.domain.schedule.EntryScheduleOverrides
import com.worklife.boundary.domain.schedule.GlobalSchedule
import com.worklife.boundary.domain.schedule.OverrideMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalTime

class BoundaryRepository(
    private val database: BoundaryDatabase,
    private val phoneNormalizer: PhoneNormalizer = PhoneNormalizer(defaultRegion = "US"),
) {
    private val settingsDao = database.appSettingsDao()
    private val blocklistDao = database.blocklistDao()
    private val blockEventDao = database.blockEventDao()

    val appSettings: Flow<AppSettingsEntity?> = settingsDao.observe()
    val blocklistCount: Flow<Int> = blocklistDao.observeCount()
    val blocklistEntries: Flow<List<BlocklistEntryEntity>> = blocklistDao.observeEntries()

    fun observeEntry(id: Long): Flow<BlocklistEntryEntity?> = blocklistDao.observeEntry(id)

    suspend fun ensureDefaults() {
        if (settingsDao.get() == null) {
            settingsDao.upsert(BoundaryMappers.defaultSettings())
        }
    }

    suspend fun getSettings(): AppSettingsEntity =
        settingsDao.get() ?: BoundaryMappers.defaultSettings().also { settingsDao.upsert(it) }

    suspend fun loadBlocklistForScreening(): List<BlocklistEntry> {
        val entries = blocklistDao.observeEntries().first()
        return entries.map { entity ->
            val numbers = blocklistDao.numbersForEntry(entity.id)
            BoundaryMappers.toBlocklistEntry(entity, numbers)
        }
    }

    suspend fun setMasterBlocking(enabled: Boolean) {
        val current = getSettings()
        settingsDao.upsert(current.copy(masterBlockingEnabled = enabled))
    }

    suspend fun setSendBlockedCallsToVoicemail(enabled: Boolean) {
        val current = getSettings()
        settingsDao.upsert(current.copy(sendBlockedCallsToVoicemail = enabled))
    }

    suspend fun completeOnboarding(
        activeDays: Set<DayOfWeek>,
        start: LocalTime,
        end: LocalTime,
    ) {
        val current = getSettings()
        settingsDao.upsert(
            current.copy(
                onboardingComplete = true,
                masterBlockingEnabled = true,
                activeDaysMask = DayOfWeekBitmask.encode(activeDays),
                startMinutes = BoundaryMappers.localTimeToMinutes(start),
                endMinutes = BoundaryMappers.localTimeToMinutes(end),
            ),
        )
    }

    suspend fun updateGlobalSchedule(
        activeDays: Set<DayOfWeek>,
        start: LocalTime,
        end: LocalTime,
    ) {
        val current = getSettings()
        settingsDao.upsert(
            current.copy(
                activeDaysMask = DayOfWeekBitmask.encode(activeDays),
                startMinutes = BoundaryMappers.localTimeToMinutes(start),
                endMinutes = BoundaryMappers.localTimeToMinutes(end),
            ),
        )
    }

    suspend fun addManualEntry(displayName: String, rawNumber: String): Result<Long> {
        val normalized = phoneNormalizer.normalize(rawNumber)
            ?: return Result.failure(IllegalArgumentException("Invalid phone number"))
        return addEntry(displayName, contactId = null, numbers = listOf(normalized))
    }

    suspend fun addContactEntry(
        displayName: String,
        contactId: String,
        rawNumbers: List<String>,
    ): Result<Long> {
        val normalized = rawNumbers.mapNotNull { phoneNormalizer.normalize(it) }.distinct()
        if (normalized.isEmpty()) {
            return Result.failure(IllegalArgumentException("No valid phone numbers"))
        }
        return addEntry(displayName, contactId, normalized)
    }

    private suspend fun addEntry(
        displayName: String,
        contactId: String?,
        numbers: List<String>,
    ): Result<Long> {
        val entryId = blocklistDao.insertEntry(
            BlocklistEntryEntity(
                displayName = displayName,
                contactId = contactId,
                enabled = true,
                daysMode = OverrideMode.INHERIT_GLOBAL.toStorage(),
                customDaysMask = null,
                timesMode = OverrideMode.INHERIT_GLOBAL.toStorage(),
                customStartMinutes = null,
                customEndMinutes = null,
            ),
        )
        blocklistDao.insertNumbers(
            numbers.map { BlocklistNumberEntity(entryId = entryId, normalizedNumber = it) },
        )
        return Result.success(entryId)
    }

    suspend fun setEntryEnabled(id: Long, enabled: Boolean) {
        blocklistDao.setEnabled(id, enabled)
    }

    suspend fun updateEntrySchedule(id: Long, overrides: EntryScheduleOverrides) {
        blocklistDao.updateSchedule(
            id = id,
            daysMode = overrides.daysMode.toStorage(),
            customDaysMask = if (overrides.daysMode == OverrideMode.CUSTOM) {
                DayOfWeekBitmask.encode(overrides.customDays)
            } else {
                null
            },
            timesMode = overrides.timesMode.toStorage(),
            customStartMinutes = overrides.customStartTime?.let { BoundaryMappers.localTimeToMinutes(it) },
            customEndMinutes = overrides.customEndTime?.let { BoundaryMappers.localTimeToMinutes(it) },
        )
    }

    suspend fun deleteEntry(id: Long) {
        blocklistDao.deleteEntry(id)
    }

    suspend fun recordBlockEvent(
        normalizedNumber: String,
        entryId: Long?,
        displayName: String?,
    ) {
        blockEventDao.insert(
            BlockEventEntity(
                timestampEpochMillis = System.currentTimeMillis(),
                normalizedNumber = normalizedNumber,
                entryId = entryId,
                displayName = displayName,
            ),
        )
    }

    suspend fun globalSchedule(): GlobalSchedule =
        BoundaryMappers.toGlobalSchedule(getSettings())

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN sendBlockedCallsToVoicemail INTEGER NOT NULL DEFAULT 1",
                )
            }
        }

        fun from(context: Context): BoundaryRepository {
            val db = Room.databaseBuilder(
                context.applicationContext,
                BoundaryDatabase::class.java,
                "boundary.db",
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
            return BoundaryRepository(db)
        }
    }
}

package com.worklife.boundary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.worklife.boundary.data.local.entity.AppSettingsEntity
import com.worklife.boundary.data.local.entity.BlockEventEntity
import com.worklife.boundary.data.local.entity.BlocklistEntryEntity
import com.worklife.boundary.data.local.entity.BlocklistNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observe(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun get(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)
}

@Dao
interface BlocklistDao {
    @Query("SELECT * FROM blocklist_entries ORDER BY displayName COLLATE NOCASE")
    fun observeEntries(): Flow<List<BlocklistEntryEntity>>

    @Query("SELECT * FROM blocklist_entries WHERE id = :id")
    fun observeEntry(id: Long): Flow<BlocklistEntryEntity?>

    @Query("SELECT * FROM blocklist_numbers WHERE entryId = :entryId")
    suspend fun numbersForEntry(entryId: Long): List<BlocklistNumberEntity>

    @Query("SELECT * FROM blocklist_numbers")
    suspend fun allNumbers(): List<BlocklistNumberEntity>

    @Query("SELECT COUNT(*) FROM blocklist_entries")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: BlocklistEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNumbers(numbers: List<BlocklistNumberEntity>)

    @Query("UPDATE blocklist_entries SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query(
        """
        UPDATE blocklist_entries SET
            daysMode = :daysMode,
            customDaysMask = :customDaysMask,
            timesMode = :timesMode,
            customStartMinutes = :customStartMinutes,
            customEndMinutes = :customEndMinutes
        WHERE id = :id
        """,
    )
    suspend fun updateSchedule(
        id: Long,
        daysMode: String,
        customDaysMask: Int?,
        timesMode: String,
        customStartMinutes: Int?,
        customEndMinutes: Int?,
    )

    @Query("DELETE FROM blocklist_entries WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Transaction
    suspend fun replaceEntryNumbers(entryId: Long, numbers: List<BlocklistNumberEntity>) {
        deleteNumbersForEntry(entryId)
        insertNumbers(numbers)
    }

    @Query("DELETE FROM blocklist_numbers WHERE entryId = :entryId")
    suspend fun deleteNumbersForEntry(entryId: Long)
}

@Dao
interface BlockEventDao {
    @Insert
    suspend fun insert(event: BlockEventEntity)
}

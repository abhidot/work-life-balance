package com.worklife.boundary.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val masterBlockingEnabled: Boolean = true,
    val onboardingComplete: Boolean = false,
    val activeDaysMask: Int,
    val startMinutes: Int,
    val endMinutes: Int,
    /** When true, blocked calls use reject semantics so carriers can route to voicemail. */
    val sendBlockedCallsToVoicemail: Boolean = true,
)

@Entity(tableName = "blocklist_entries")
data class BlocklistEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    val contactId: String?,
    val enabled: Boolean = true,
    val daysMode: String,
    val customDaysMask: Int?,
    val timesMode: String,
    val customStartMinutes: Int?,
    val customEndMinutes: Int?,
)

@Entity(
    tableName = "blocklist_numbers",
    foreignKeys = [
        ForeignKey(
            entity = BlocklistEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("entryId"), Index("normalizedNumber", unique = true)],
)
data class BlocklistNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val normalizedNumber: String,
)

@Entity(tableName = "block_events")
data class BlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampEpochMillis: Long,
    val normalizedNumber: String,
    val entryId: Long?,
    val displayName: String?,
)

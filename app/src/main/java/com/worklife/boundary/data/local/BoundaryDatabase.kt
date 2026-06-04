package com.worklife.boundary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.worklife.boundary.data.local.dao.AppSettingsDao
import com.worklife.boundary.data.local.dao.BlockEventDao
import com.worklife.boundary.data.local.dao.BlocklistDao
import com.worklife.boundary.data.local.entity.AppSettingsEntity
import com.worklife.boundary.data.local.entity.BlockEventEntity
import com.worklife.boundary.data.local.entity.BlocklistEntryEntity
import com.worklife.boundary.data.local.entity.BlocklistNumberEntity

@Database(
    entities = [
        AppSettingsEntity::class,
        BlocklistEntryEntity::class,
        BlocklistNumberEntity::class,
        BlockEventEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class BoundaryDatabase : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun blocklistDao(): BlocklistDao
    abstract fun blockEventDao(): BlockEventDao
}

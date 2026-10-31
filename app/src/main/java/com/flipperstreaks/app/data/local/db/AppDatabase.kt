package com.flipperstreaks.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flipperstreaks.app.data.local.db.dao.LocationCacheDao
import com.flipperstreaks.app.data.local.db.dao.PlayLogDao
import com.flipperstreaks.app.data.local.db.entity.LocationEntity
import com.flipperstreaks.app.data.local.db.entity.MachineEntity
import com.flipperstreaks.app.data.local.db.entity.PlayLogEntity

@Database(
    entities = [PlayLogEntity::class, LocationEntity::class, MachineEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playLogDao(): PlayLogDao
    abstract fun locationCacheDao(): LocationCacheDao

    companion object {
        const val DB_NAME = "flipper_streaks.db"
    }
}

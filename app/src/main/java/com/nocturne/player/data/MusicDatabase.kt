package com.nocturne.player.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AudioEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun audioDao(): AudioDao
}

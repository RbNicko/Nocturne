package com.nocturne.player.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AudioEntity::class,
        FavoriteEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun audioDao(): AudioDao

    abstract fun favoriteDao(): FavoriteDao
}
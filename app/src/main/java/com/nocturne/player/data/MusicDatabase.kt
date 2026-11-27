package com.nocturne.player.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AudioEntity::class,
        FavoriteEntity::class,
        PlayStatsEntity::class,
        DailyPlayStatsEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {

    abstract fun audioDao(): AudioDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun playStatsDao(): PlayStatsDao

    abstract fun dailyPlayStatsDao(): DailyPlayStatsDao
}
package com.nocturne.player.data

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "daily_play_stats",
    primaryKeys = ["trackId", "date"],
    indices = [
        Index(value = ["date"])
    ]
)
data class DailyPlayStatsEntity(
    val trackId: Long,
    val date: String,
    val playCount: Int = 0
)
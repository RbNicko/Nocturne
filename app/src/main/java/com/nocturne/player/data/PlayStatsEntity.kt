package com.nocturne.player.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_stats")
data class PlayStatsEntity(

    @PrimaryKey
    val trackId: Long,

    val playCount: Int = 0,

    val lastPlayed: Long? = null
)

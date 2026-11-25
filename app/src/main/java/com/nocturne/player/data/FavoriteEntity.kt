package com.nocturne.player.data

import androidx.room.Entity

@Entity(
    tableName = "favorites",
    primaryKeys = ["type", "itemId"]
)
data class FavoriteEntity(
    val type: String,
    val itemId: String,
    val title: String,
    val artist: String,
    val addedAt: Long = System.currentTimeMillis()
)
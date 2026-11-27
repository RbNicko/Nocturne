package com.nocturne.player.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PlayStatsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialStats(stats: PlayStatsEntity)

    @Query("""
        UPDATE play_stats
        SET playCount = playCount + 1,
            lastPlayed = :playedAt
        WHERE trackId = :trackId
    """)
    suspend fun incrementPlayCount(trackId: Long, playedAt: Long)

    @Transaction
    suspend fun registerPlay(trackId: Long, playedAt: Long) {
        insertInitialStats(
            PlayStatsEntity(trackId = trackId)
        )
        incrementPlayCount(trackId, playedAt)
    }

    @Query("""
        SELECT * FROM play_stats
        WHERE playCount > 0
        ORDER BY playCount DESC, lastPlayed DESC
        LIMIT 15
    """)
    suspend fun getTopTenLocal(): List<PlayStatsEntity>

    @Query("""
        SELECT * FROM play_stats
        WHERE trackId = :trackId
        LIMIT 1
    """)
    suspend fun getStatsForTrack(trackId: Long): PlayStatsEntity?
}
package com.nocturne.player.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

data class TrackPlayCount(
    val trackId: Long,
    val playCount: Int
)

@Dao
interface DailyPlayStatsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialStats(
        stats: DailyPlayStatsEntity
    )

    @Query("""
        UPDATE daily_play_stats
        SET playCount = playCount + 1
        WHERE trackId = :trackId
        AND date = :date
    """)
    suspend fun incrementPlayCount(
        trackId: Long,
        date: String
    )

    @Transaction
    suspend fun registerPlay(
        trackId: Long,
        date: String
    ) {
        insertInitialStats(
            DailyPlayStatsEntity(
                trackId = trackId,
                date = date
            )
        )

        incrementPlayCount(trackId, date)
    }

    // Top 15 de un día específico
    @Query("""
        SELECT * FROM daily_play_stats
        WHERE date = :date
        ORDER BY playCount DESC
        LIMIT 15
    """)
    suspend fun getTopTracksForDay(
        date: String
    ): List<DailyPlayStatsEntity>

    // Top 15 acumulado entre dos fechas
    @Query("""
        SELECT
            trackId,
            SUM(playCount) AS playCount
        FROM daily_play_stats
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY trackId
        ORDER BY playCount DESC, trackId ASC
        LIMIT 15
    """)
    suspend fun getTopTracksBetweenDates(
        startDate: String,
        endDate: String
    ): List<TrackPlayCount>

    // Último día con actividad, sin superar la fecha actual
    @Query("""
        SELECT date FROM daily_play_stats
        WHERE date <= :currentDate
        GROUP BY date
        ORDER BY date DESC
        LIMIT 1
    """)
    suspend fun getLatestRecordedDate(
        currentDate: String
    ): String?
}
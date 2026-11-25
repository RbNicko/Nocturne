package com.nocturne.player.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class AudioDao() {

    @Query("""
        SELECT * FROM audio_tracks
        ORDER BY title COLLATE NOCASE ASC
    """)
    abstract suspend fun getAllTracks(): List<AudioEntity>

    @Query("""
        SELECT * FROM audio_tracks
        WHERE COALESCE(NULLIF(albumArtist, ''), artist) = :artistName
        ORDER BY album COLLATE NOCASE ASC, discNumber ASC, trackNumber ASC
    """)
    abstract suspend fun getTracksByAlbumArtist(
        artistName: String
    ): List<AudioEntity>

    @Query("""
        SELECT * FROM audio_tracks
        WHERE album = :albumName
        AND COALESCE(NULLIF(albumArtist, ''), artist) = :artistName
        ORDER BY discNumber ASC, trackNumber ASC, title COLLATE NOCASE ASC
    """)
    abstract suspend fun getTracksByAlbum(
        albumName: String,
        artistName: String
    ): List<AudioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTracks(tracks: List<AudioEntity>)

    @Query("DELETE FROM audio_tracks")
    abstract suspend fun deleteAllTracks()

    @Query("SELECT COUNT(*) FROM audio_tracks")
    abstract suspend fun getTrackCount(): Int

    @Transaction
    open suspend fun replaceAllTracks(tracks: List<AudioEntity>) {
        deleteAllTracks()
        insertTracks(tracks)
    }
}

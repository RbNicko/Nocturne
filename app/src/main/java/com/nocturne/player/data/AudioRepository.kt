package com.nocturne.player.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow

class AudioRepository(
    context: Context
) {

    private val database = DatabaseProvider.getDatabase(context)
    private val audioDao = database.audioDao()
    private val favoriteDao = database.favoriteDao()

    private fun AudioEntity.toAudioTrack(): AudioTrack =
        AudioTrack(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            uri = Uri.parse(uri),

            albumArtist = albumArtist,
            genre = genre,
            year = year,
            composer = composer,
            trackNumber = trackNumber,
            discNumber = discNumber,
            isrc = isrc,
            audioFormat = audioFormat,
             audioQuality = audioQuality,
            isDolbyAtmos = isDolbyAtmos
        )

    suspend fun getSavedTracks(): List<AudioTrack> =
        withContext(Dispatchers.IO) {
            audioDao.getAllTracks().map { it.toAudioTrack() }
        }

    suspend fun getTracksByArtist(
        artistName: String
    ): List<AudioTrack> =
        withContext(Dispatchers.IO) {
            audioDao.getTracksByAlbumArtist(artistName)
                .map { it.toAudioTrack() }
        }

    suspend fun getTracksByAlbum(
        albumName: String,
        artistName: String
    ): List<AudioTrack> =
        withContext(Dispatchers.IO) {
            audioDao.getTracksByAlbum(albumName, artistName)
                .map { it.toAudioTrack() }
        }


    suspend fun getArtists(): List<ArtistInfo> =
        withContext(Dispatchers.IO) {
            val tracks = audioDao.getAllTracks()
                .map { it.toAudioTrack() }

            LibraryOrganizer.getArtists(tracks)
        }

    suspend fun getAlbums(): List<AlbumInfo> =
        withContext(Dispatchers.IO) {
            val tracks = audioDao.getAllTracks()
                .map { it.toAudioTrack() }

            LibraryOrganizer.getAlbums(tracks)
        }


    suspend fun addFavorite(favorite: FavoriteEntity) =
        withContext(Dispatchers.IO) {
            favoriteDao.addFavorite(favorite)
        }

    suspend fun removeFavorite(
        type: String,
        itemId: String
    ) =
        withContext(Dispatchers.IO) {
            favoriteDao.removeFavorite(type, itemId)
        }

    fun observeFavoritesByType(
        type: String
    ): Flow<List<FavoriteEntity>> =
        favoriteDao.observeFavoritesByType(type)

    fun observeIsFavorite(
        type: String,
        itemId: String
    ): Flow<Boolean> =
        favoriteDao.observeIsFavorite(type, itemId)

    fun observeAllFavorites(): Flow<List<FavoriteEntity>> =
        favoriteDao.observeAllFavorites()


    suspend fun scanAndSaveTracks(context: Context): List<AudioTrack> =
        withContext(Dispatchers.IO) {

            val scannedTracks = loadAudioTracks(context)

            val entities = scannedTracks.map { track ->
                AudioEntity(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    duration = track.duration,
                    uri = track.uri.toString(),

                    albumArtist = track.albumArtist,
                    genre = track.genre,
                    year = track.year,
                    composer = track.composer,
                    trackNumber = track.trackNumber,
                    discNumber = track.discNumber,
                    isrc = track.isrc,
                    audioFormat = track.audioFormat,
                    audioQuality = track.audioQuality,
                    isDolbyAtmos = track.isDolbyAtmos
                )
            }

            audioDao.replaceAllTracks(entities)

            scannedTracks
        }
}

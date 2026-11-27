package com.nocturne.player.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class AudioRepository(
    context: Context
) {

    private val database = DatabaseProvider.getDatabase(context)
    private val audioDao = database.audioDao()
    private val favoriteDao = database.favoriteDao()
    private val playStatsDao = database.playStatsDao()
    private val dailyPlayStatsDao = database.dailyPlayStatsDao()

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

            private suspend fun buildRankedTracks(
        stats: List<Pair<Long, Int>>
    ): List<RankedTrack> {

        if (stats.isEmpty()) {
            return emptyList()
        }

        val trackIds = stats.map { it.first }

        val tracks = audioDao.getTracksByIds(trackIds)
            .associateBy { it.id }

        return stats.mapNotNull { (trackId, playCount) ->
            tracks[trackId]?.let { entity ->
                RankedTrack(
                    track = entity.toAudioTrack(),
                    playCount = playCount
                )
            }
        }
    }

    suspend fun getSavedTracks(): List<AudioTrack> =
        withContext(Dispatchers.IO) {
            audioDao.getAllTracks().map { it.toAudioTrack() }
        }

    // TOP 15 de todos los tiempos
    suspend fun getTopAllTimeTracks(): List<RankedTrack> =
        withContext(Dispatchers.IO) {

            val stats = playStatsDao.getTopTenLocal()

            buildRankedTracks(
                stats.map { it.trackId to it.playCount }
            )
        }

    // TOP 15 del último día con actividad
    suspend fun getTopDailyTracks(): List<RankedTrack> =
        withContext(Dispatchers.IO) {

            val currentDate = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.ROOT
            ).format(Date())

            val latestDate = dailyPlayStatsDao
                .getLatestRecordedDate(currentDate)
                ?: return@withContext emptyList()

            val stats = dailyPlayStatsDao
                .getTopTracksForDay(latestDate)

            buildRankedTracks(
                stats.map { it.trackId to it.playCount }
            )
        }

    // TOP 15 entre dos fechas
    suspend fun getTopTracksBetweenDates(
        startDate: String,
        endDate: String
    ): List<RankedTrack> =
        withContext(Dispatchers.IO) {

            val stats = dailyPlayStatsDao
                .getTopTracksBetweenDates(startDate, endDate)

            buildRankedTracks(
                stats.map { it.trackId to it.playCount }
            )
        }

    // Convierte una fecha del calendario a yyyy-MM-dd
    private fun formatCalendarDate(calendar: Calendar): String {
        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ROOT
        ).format(calendar.time)
    }

    // TOP 15 de la semana actual
    suspend fun getTopWeeklyTracks(): List<RankedTrack> =
        withContext(Dispatchers.IO) {

            val today = Calendar.getInstance()
            val startOfWeek = today.clone() as Calendar

            startOfWeek.set(
                Calendar.DAY_OF_WEEK,
                startOfWeek.firstDayOfWeek
            )

            getTopTracksBetweenDates(
                startDate = formatCalendarDate(startOfWeek),
                endDate = formatCalendarDate(today)
            )
        }

    // TOP 15 del mes actual
    suspend fun getTopMonthlyTracks(): List<RankedTrack> =
        withContext(Dispatchers.IO) {

            val today = Calendar.getInstance()
            val startOfMonth = today.clone() as Calendar

            startOfMonth.set(Calendar.DAY_OF_MONTH, 1)

            getTopTracksBetweenDates(
                startDate = formatCalendarDate(startOfMonth),
                endDate = formatCalendarDate(today)
            )
        }

    // TOP 15 del año actual
    suspend fun getTopYearlyTracks(): List<RankedTrack> =
        withContext(Dispatchers.IO) {

            val today = Calendar.getInstance()
            val startOfYear = today.clone() as Calendar

            startOfYear.set(Calendar.DAY_OF_YEAR, 1)

            getTopTracksBetweenDates(
                startDate = formatCalendarDate(startOfYear),
                endDate = formatCalendarDate(today)
            )
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
        
suspend fun registerPlay(trackId: Long) =
    withContext(Dispatchers.IO) {

        val currentTime = System.currentTimeMillis()

        val currentDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ROOT
        ).format(Date(currentTime))

        // Estadística histórica
        playStatsDao.registerPlay(
            trackId = trackId,
            playedAt = currentTime
        )

        // Estadística diaria
        dailyPlayStatsDao.registerPlay(
            trackId = trackId,
            date = currentDate
        )
    
        }

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

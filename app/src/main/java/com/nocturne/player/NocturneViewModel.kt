package com.nocturne.player

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nocturne.player.data.AlbumInfo
import com.nocturne.player.data.ArtistInfo
import com.nocturne.player.data.AudioRepository
import com.nocturne.player.data.ArtworkLoader
import com.nocturne.player.data.AudioTrack
import com.nocturne.player.data.FavoriteEntity
import com.nocturne.player.data.LibraryOrganizer
import com.nocturne.player.data.RankedTrack
import com.nocturne.player.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Concentra el estado y la lógica de la app.
 *
 * Sobrevive a rotaciones de pantalla: la biblioteca no se vuelve a cargar
 * y la navegación (pestaña, artista, álbum...) se conserva.
 */
class NocturneViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioRepository(application)
    private val settingsStore = SettingsStore(application)

    // ------------------------------------------------------------------
    // PERMISO
    // ------------------------------------------------------------------

    val audioPermission: String =
        if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    var hasPermission by mutableStateOf(
        application.checkSelfPermission(audioPermission) ==
            PackageManager.PERMISSION_GRANTED
    )
        private set

    // ------------------------------------------------------------------
    // NAVEGACIÓN Y ESTADO DE LA INTERFAZ
    // ------------------------------------------------------------------

    var selectedTab by mutableIntStateOf(0)
        private set

    var searchText by mutableStateOf("")
        private set

    var selectedLibrarySection by mutableIntStateOf(0)
        private set

    var selectedArtist by mutableStateOf<ArtistInfo?>(null)
        private set

    var selectedAlbum by mutableStateOf<AlbumInfo?>(null)
        private set

    var showArtistSongs by mutableStateOf(false)
        private set

    var selectedTopPeriod by mutableIntStateOf(0)
        private set

    // Pantalla de reproducción a pantalla completa.
    var showPlayer by mutableStateOf(false)
        private set

    // Pantalla "Carpetas excluidas" dentro de Ajustes.
    var showExcludedFolders by mutableStateOf(false)
        private set

    // Carpetas cuyas canciones no entran a la biblioteca (ej. "Music/Podcasts/").
    var excludedFolders by mutableStateOf(settingsStore.getExcludedFolders())
        private set

    // ------------------------------------------------------------------
    // DATOS
    // ------------------------------------------------------------------

    var tracks by mutableStateOf<List<AudioTrack>>(emptyList())
        private set

    var artists by mutableStateOf<List<ArtistInfo>>(emptyList())
        private set

    var albums by mutableStateOf<List<AlbumInfo>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var topTracks by mutableStateOf<List<RankedTrack>>(emptyList())
        private set

    var isTopLoading by mutableStateOf(false)
        private set

    private var hasLoadedLibrary = false

    // Se pidió un nuevo escaneo mientras ya había uno en curso.
    private var rescanRequested = false

    val favoriteTracks: StateFlow<List<FavoriteEntity>> =
        repository.observeFavoritesByType("track")
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    val favoriteAlbums: StateFlow<List<FavoriteEntity>> =
        repository.observeFavoritesByType("album")
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    // ------------------------------------------------------------------
    // REPRODUCCIÓN
    // ------------------------------------------------------------------

    // Estas dos variables deben declararse ANTES del controlador,
    // porque el controlador las usa cuando termina de conectarse.
    private var playbackConnected = false
    private var pendingPlayback: (() -> Unit)? = null

    private val playbackController = PlaybackController(application) {
        playbackConnected = true
        pendingPlayback?.invoke()
        pendingPlayback = null
    }

    // Estado de reproducción que observa la interfaz.
    val playbackState: StateFlow<PlaybackUiState> = playbackController.state

    // Posición actual (ms). Se actualiza varias veces por segundo mientras
    // suena, y se lee solo al dibujar para no recomponer pantallas enteras.
    var positionMs by mutableLongStateOf(0L)
        private set

    // ------------------------------------------------------------------
    // ARRANQUE
    // ------------------------------------------------------------------

    init {
        // La biblioteca se carga al abrir la app (ya no al tocar la pestaña),
        // así está lista cuando el usuario llega a ella.
        loadLibraryIfNeeded()
        loadTopTracks()

        viewModelScope.launch {
            playbackController.state.collectLatest { state ->
                positionMs = playbackController.currentPosition()

                if (state.isPlaying) {
                    while (true) {
                        delay(250)
                        positionMs = playbackController.currentPosition()
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // PERMISO
    // ------------------------------------------------------------------

    fun onPermissionResult(granted: Boolean) {
        hasPermission = granted
        if (granted) {
            loadLibraryIfNeeded()
        }
    }

    // ------------------------------------------------------------------
    // BIBLIOTECA
    // ------------------------------------------------------------------

    private fun loadLibraryIfNeeded() {
        if (hasLoadedLibrary || isLoading || !hasPermission) return

        viewModelScope.launch {
            isLoading = true

            try {
                val saved = repository.getSavedTracks()

                val loaded = if (saved.isNotEmpty()) {
                    saved
                } else {
                    repository.scanAndSaveTracks(
                        getApplication<Application>(),
                        excludedFolders
                    )
                }

                applyLibrary(loaded)
                hasLoadedLibrary = true

            } finally {
                isLoading = false
            }

            if (rescanRequested) {
                rescanRequested = false
                refreshLibrary()
            }
        }
    }

    fun refreshLibrary() {
        if (isLoading) {
            // Ya hay una carga en curso: se repetirá cuando termine.
            rescanRequested = true
            return
        }

        viewModelScope.launch {
            isLoading = true

            try {
                val scanned = repository.scanAndSaveTracks(
                    getApplication<Application>(),
                    excludedFolders
                )

                applyLibrary(scanned)
                hasLoadedLibrary = true

            } finally {
                isLoading = false
            }

            if (rescanRequested) {
                rescanRequested = false
                refreshLibrary()
            }
        }
    }

    // Calcula artistas y álbumes a partir de las canciones que ya tenemos
    // (antes se volvía a leer la tabla completa dos veces más).
    private suspend fun applyLibrary(newTracks: List<AudioTrack>) {
        val (newArtists, newAlbums) = withContext(Dispatchers.Default) {
            // Los álbumes se calculan una vez y se reutilizan para los artistas.
            val organizedAlbums = LibraryOrganizer.getAlbums(newTracks)

            LibraryOrganizer.getArtists(newTracks, organizedAlbums) to
                organizedAlbums
        }

        tracks = newTracks
        artists = newArtists
        albums = newAlbums

        // Precarga las portadas de las primeras canciones, para que estén
        // listas cuando se abra la biblioteca.
        viewModelScope.launch(Dispatchers.Default) {
            ArtworkLoader.preload(
                getApplication<Application>(),
                newTracks.take(60).map { it.uri },
                192
            )
        }
    }

    // ------------------------------------------------------------------
    // TOP 15
    // ------------------------------------------------------------------

    fun loadTopTracks() {
        val period = selectedTopPeriod

        viewModelScope.launch {
            isTopLoading = true

            try {
                val result = when (period) {
                    0 -> repository.getTopDailyTracks()
                    1 -> repository.getTopWeeklyTracks()
                    2 -> repository.getTopMonthlyTracks()
                    3 -> repository.getTopYearlyTracks()
                    else -> repository.getTopAllTimeTracks()
                }

                // Si el usuario cambió de periodo mientras cargaba,
                // se descarta este resultado.
                if (period == selectedTopPeriod) {
                    topTracks = result
                }
            } finally {
                if (period == selectedTopPeriod) {
                    isTopLoading = false
                }
            }
        }
    }

    fun selectTopPeriod(period: Int) {
        selectedTopPeriod = period
        loadTopTracks()
    }

    // ------------------------------------------------------------------
    // NAVEGACIÓN
    // ------------------------------------------------------------------

    fun selectTab(index: Int) {
        // Tocar una pestaña de la cápsula cierra cualquier detalle abierto
        // (artista, canciones del artista o álbum).
        selectedAlbum = null
        selectedArtist = null
        showArtistSongs = false
        showExcludedFolders = false
        showPlayer = false

        selectedTab = index

        // Al volver a Inicio se refresca el Top (las estadísticas cambian
        // mientras se escucha música).
        if (index == 0) {
            loadTopTracks()
        }
    }

    fun updateSearchText(text: String) {
        searchText = text
    }

    fun selectLibrarySection(index: Int) {
        selectedLibrarySection = index
    }

    fun openArtist(artist: ArtistInfo) {
        showArtistSongs = false
        selectedArtist = artist
    }

    fun closeArtist() {
        selectedArtist = null
        showArtistSongs = false
    }

    fun openArtistSongs() {
        showArtistSongs = true
    }

    fun closeArtistSongs() {
        showArtistSongs = false
    }

    fun openAlbum(album: AlbumInfo) {
        selectedAlbum = album
    }

    fun closeAlbum() {
        selectedAlbum = null
    }

    val canGoBack: Boolean
        get() = showPlayer ||
            showExcludedFolders ||
            selectedAlbum != null ||
            showArtistSongs ||
            selectedArtist != null ||
            selectedTab != 0

    fun goBack() {
        when {
            showPlayer -> showPlayer = false

            showExcludedFolders -> showExcludedFolders = false

            selectedAlbum != null -> selectedAlbum = null

            showArtistSongs && selectedArtist != null ->
                showArtistSongs = false

            selectedArtist != null -> closeArtist()

            selectedTab != 0 -> selectTab(0)
        }
    }

    // ------------------------------------------------------------------
    // CARPETAS EXCLUIDAS
    // ------------------------------------------------------------------

    fun openExcludedFolders() {
        showExcludedFolders = true
    }

    fun closeExcludedFolders() {
        showExcludedFolders = false
    }

    /**
     * Excluye la carpeta elegida con el selector del sistema.
     * Devuelve false si la selección no sirve (por ejemplo, el
     * almacenamiento completo).
     */
    fun addExcludedFolder(treeUri: Uri): Boolean {
        val documentId = runCatching {
            DocumentsContract.getTreeDocumentId(treeUri)
        }.getOrNull() ?: return false

        // El identificador tiene la forma "primary:Music/Podcasts".
        val path = documentId
            .substringAfter(":", "")
            .trim('/')

        if (path.isEmpty()) return false

        updateExcludedFolders(excludedFolders + "$path/")
        return true
    }

    fun removeExcludedFolder(folder: String) {
        updateExcludedFolders(excludedFolders - folder)
    }

    // Guarda el cambio y vuelve a escanear para que la biblioteca lo refleje.
    private fun updateExcludedFolders(folders: Set<String>) {
        settingsStore.setExcludedFolders(folders)
        excludedFolders = folders
        refreshLibrary()
    }

    // ------------------------------------------------------------------
    // FAVORITOS
    // ------------------------------------------------------------------

    fun albumFavoriteId(album: AlbumInfo): String =
        "album:${album.artist.trim().lowercase()}|${album.title.trim().lowercase()}"

    fun toggleTrackFavorite(track: AudioTrack) {
        val trackId = track.id.toString()
        val alreadyFavorite = favoriteTracks.value.any { it.itemId == trackId }

        viewModelScope.launch {
            if (alreadyFavorite) {
                repository.removeFavorite("track", trackId)
            } else {
                repository.addFavorite(
                    FavoriteEntity(
                        type = "track",
                        itemId = trackId,
                        title = track.title,
                        artist = track.artist
                    )
                )
            }
        }
    }

    fun toggleAlbumFavorite(album: AlbumInfo) {
        val albumId = albumFavoriteId(album)
        val alreadyFavorite = favoriteAlbums.value.any { it.itemId == albumId }

        viewModelScope.launch {
            if (alreadyFavorite) {
                repository.removeFavorite("album", albumId)
            } else {
                repository.addFavorite(
                    FavoriteEntity(
                        type = "album",
                        itemId = albumId,
                        title = album.title,
                        artist = album.artist
                    )
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // REPRODUCCIÓN
    // ------------------------------------------------------------------

    fun playSelectedAlbum(shuffle: Boolean) {
        val album = selectedAlbum ?: return

        val request: () -> Unit = {
            viewModelScope.launch {
                val albumTracks = repository.getTracksByAlbum(
                    albumName = album.title,
                    artistName = album.artist
                )

                if (albumTracks.isNotEmpty()) {
                    playbackController.playTracks(
                        tracks = albumTracks,
                        startIndex = 0,
                        shuffle = shuffle
                    )
                }
            }
        }

        runWhenConnected(request)
    }

    // Si el reproductor todavía se está conectando, la orden espera.
    private fun runWhenConnected(action: () -> Unit) {
        if (playbackConnected) {
            action()
        } else {
            pendingPlayback = action
        }
    }

    // Reproduce una lista de canciones empezando por la indicada.
    fun playTracks(tracks: List<AudioTrack>, startIndex: Int) {
        runWhenConnected {
            playbackController.playTracks(
                tracks = tracks,
                startIndex = startIndex,
                shuffle = false
            )
        }
    }

    fun togglePlayPause() {
        playbackController.togglePlayPause()
    }

    fun openPlayer() {
        if (playbackState.value.hasMedia) {
            showPlayer = true
        }
    }

    fun closePlayer() {
        showPlayer = false
    }

    fun nextTrack() {
        playbackController.next()
    }

    fun previousTrack() {
        playbackController.previous()
    }

    fun seekTo(positionMs: Long) {
        playbackController.seekTo(positionMs)

        // La barra responde al instante, sin esperar a la siguiente lectura.
        this.positionMs = positionMs
    }

    fun toggleShuffle() {
        playbackController.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playbackController.cycleRepeatMode()
    }

    // Marca o desmarca como favorita la canción que está sonando.
    fun toggleCurrentTrackFavorite() {
        val mediaId = playbackState.value.mediaId ?: return

        val track = tracks.firstOrNull { it.id.toString() == mediaId }
            ?: return

        toggleTrackFavorite(track)
    }

    // ------------------------------------------------------------------
    // LIMPIEZA
    // ------------------------------------------------------------------

    override fun onCleared() {
        playbackController.release()
        super.onCleared()
    }
}
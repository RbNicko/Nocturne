package com.nocturne.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.nocturne.player.ui.components.NavCapsule
import com.nocturne.player.ui.components.rememberCapsuleScrollState
import com.nocturne.player.ui.screens.AlbumDetailScreen
import com.nocturne.player.ui.screens.ExcludedFoldersScreen
import com.nocturne.player.ui.screens.ArtistDetailScreen
import com.nocturne.player.ui.screens.ArtistSongsScreen
import com.nocturne.player.ui.screens.HomeScreen
import com.nocturne.player.ui.screens.LibraryScreen
import com.nocturne.player.ui.screens.PlayerScreen
import com.nocturne.player.ui.screens.SettingsScreen
import com.nocturne.player.ui.theme.*


class MainActivity : ComponentActivity() {

    private val viewModel: NocturneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // La app dibuja detrás de las barras del sistema (necesario para que
        // la pantalla de reproducción ocupe toda la pantalla).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            NocturneApp(viewModel)
        }
    }
}


@Composable
fun NocturneApp(vm: NocturneViewModel) {

    val libraryListState = rememberLazyListState()

    // Controla cuánto está oculta la cápsula de navegación según el scroll.
    val capsuleState = rememberCapsuleScrollState()

    val favoriteTracks by vm.favoriteTracks.collectAsState()
    val favoriteAlbums by vm.favoriteAlbums.collectAsState()
    val playback by vm.playbackState.collectAsState()

    // Copias locales para que Kotlin pueda comprobar que no son nulas.
    val selectedArtist = vm.selectedArtist
    val selectedAlbum = vm.selectedAlbum

    // Canción que está sonando, su calidad de audio y si es favorita.
    val currentTrack = remember(playback.mediaId, vm.tracks) {
        vm.tracks.firstOrNull { it.id.toString() == playback.mediaId }
    }

    val qualityLabel = currentTrack
        ?.let { listOfNotNull(it.audioQuality, it.audioFormat) }
        ?.joinToString(" • ")
        ?.takeIf { it.isNotBlank() }

    val currentIsFavorite = favoriteTracks.any {
        it.itemId == playback.mediaId
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            vm.onPermissionResult(granted)
        }

    BackHandler(enabled = vm.canGoBack) {
        vm.goBack()
    }

    // Al cambiar de pantalla, la cápsula vuelve a mostrarse.
    LaunchedEffect(
        vm.selectedTab,
        selectedArtist,
        selectedAlbum,
        vm.showArtistSongs,
        vm.showExcludedFolders,
        vm.showPlayer
    ) {
        capsuleState.show()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background,
            surface = SurfaceDark,
            primary = Accent,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        )
    ) {

        Scaffold(
            containerColor = Background
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(capsuleState.connection)
            ) {

                // CONTENIDO DE LAS PANTALLAS

                if (selectedAlbum != null) {

                    AlbumDetailScreen(
                        album = selectedAlbum,
                        onBack = { vm.closeAlbum() },
                        onShuffle = { vm.playSelectedAlbum(shuffle = true) },
                        onPlay = { vm.playSelectedAlbum(shuffle = false) },
                        onFavorite = { vm.toggleAlbumFavorite(selectedAlbum) },
                        isFavorite = favoriteAlbums.any {
                            it.itemId == vm.albumFavoriteId(selectedAlbum)
                        },
                        modifier = Modifier.statusBarsPadding()
                    )

                } else if (selectedArtist != null && vm.showArtistSongs) {

                    ArtistSongsScreen(
                        artist = selectedArtist,
                        tracks = vm.tracks,
                        onBack = { vm.closeArtistSongs() },
                        modifier = Modifier.padding(padding)
                    )

                } else if (selectedArtist != null) {

                    ArtistDetailScreen(
                        artist = selectedArtist,
                        tracks = vm.tracks,
                        onBack = { vm.closeArtist() },
                        onViewMore = { vm.openArtistSongs() },
                        modifier = Modifier.padding(padding)
                    )

                } else {

                    when (vm.selectedTab) {

                        // INICIO

                        0 -> HomeScreen(
                            topTracks = vm.topTracks,
                            selectedTopPeriod = vm.selectedTopPeriod,
                            onTopPeriodChange = { vm.selectTopPeriod(it) },
                            isTopLoading = vm.isTopLoading,
                            modifier = Modifier.padding(padding)
                        )

                        // BIBLIOTECA

                        1 -> LibraryScreen(
                            tracks = vm.tracks,
                            listState = libraryListState,
                            artists = vm.artists,
                            albums = vm.albums,
                            favoriteTracks = favoriteTracks,
                            onTrackFavoriteClick = { track ->
                                vm.toggleTrackFavorite(track)
                            },
                            onTrackClick = { list, index ->
                                vm.playTracks(list, index)
                            },
                            onArtistClick = { artist ->
                                vm.openArtist(artist)
                            },
                            onAlbumClick = { album ->
                                vm.openAlbum(album)
                            },
                            isLoading = vm.isLoading,
                            hasPermission = vm.hasPermission,
                            searchText = vm.searchText,
                            onSearchTextChange = { vm.updateSearchText(it) },
                            selectedLibrarySection = vm.selectedLibrarySection,
                            onLibrarySectionChange = {
                                vm.selectLibrarySection(it)
                            },
                            onRequestPermission = {
                                permissionLauncher.launch(vm.audioPermission)
                            },
                            onRefreshLibrary = { vm.refreshLibrary() },
                            modifier = Modifier.padding(padding)
                        )

                        // AJUSTES

                        2 -> if (vm.showExcludedFolders) {

                            ExcludedFoldersScreen(
                                folders = vm.excludedFolders.sorted(),
                                onBack = { vm.closeExcludedFolders() },
                                onFolderPicked = { uri ->
                                    vm.addExcludedFolder(uri)
                                },
                                onRemoveFolder = { folder ->
                                    vm.removeExcludedFolder(folder)
                                },
                                modifier = Modifier.padding(padding)
                            )

                        } else {

                            SettingsScreen(
                                excludedFoldersCount = vm.excludedFolders.size,
                                onOpenExcludedFolders = {
                                    vm.openExcludedFolders()
                                },
                                modifier = Modifier.padding(padding)
                            )
                        }
                    }
                }

                // CÁPSULA FLOTANTE (se oculta mientras está abierto el reproductor)

                if (!vm.showPlayer) {

                    NavCapsule(
                        selectedTab = vm.selectedTab,
                        onTabClick = { index ->
                            vm.selectTab(index)

                            if (index == 1 && !vm.hasPermission) {
                                permissionLauncher.launch(vm.audioPermission)
                            }
                        },
                        scrollState = capsuleState,
                        playback = playback,
                        positionMs = { vm.positionMs },
                        onTogglePlayPause = { vm.togglePlayPause() },
                        onOpenPlayer = { vm.openPlayer() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                // PANTALLA DE REPRODUCCIÓN

                AnimatedVisibility(
                    visible = vm.showPlayer,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {

                    PlayerScreen(
                        playback = playback,
                        positionMs = { vm.positionMs },
                        qualityLabel = qualityLabel,
                        isFavorite = currentIsFavorite,
                        onClose = { vm.closePlayer() },
                        onTogglePlayPause = { vm.togglePlayPause() },
                        onNext = { vm.nextTrack() },
                        onPrevious = { vm.previousTrack() },
                        onSeek = { vm.seekTo(it) },
                        onToggleShuffle = { vm.toggleShuffle() },
                        onCycleRepeat = { vm.cycleRepeatMode() },
                        onToggleFavorite = { vm.toggleCurrentTrackFavorite() }
                    )
                }
            }
        }
    }
}
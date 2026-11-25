package com.nocturne.player

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import com.nocturne.player.data.AudioTrack
import com.nocturne.player.data.ArtistInfo
import com.nocturne.player.data.AlbumInfo
import com.nocturne.player.data.AudioRepository
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.nocturne.player.ui.theme.*
import com.nocturne.player.ui.screens.HomeScreen
import com.nocturne.player.ui.screens.LibraryScreen
import com.nocturne.player.ui.screens.SettingsScreen
import com.nocturne.player.ui.screens.ArtistDetailScreen
import com.nocturne.player.ui.screens.AlbumDetailScreen
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NocturneApp()
        }
    }
}


@Composable
fun NocturneApp() {

    var selectedTab by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }

    var selectedLibrarySection by remember {
        mutableStateOf(0)
    }

    val libraryListState = rememberLazyListState()

    var selectedArtist by remember {
    mutableStateOf<ArtistInfo?>(null)
    }

    var selectedAlbum by remember {
    mutableStateOf<AlbumInfo?>(null)
    }

    var favoriteAlbums by remember {
    mutableStateOf<Set<String>>(emptySet())
    }

    val context = LocalContext.current

    val audioRepository = remember(context) {
        AudioRepository(context.applicationContext)
    }

    val coroutineScope = rememberCoroutineScope()

    var tracks by remember {
        mutableStateOf<List<AudioTrack>>(emptyList())
    }

    var artists by remember {
        mutableStateOf<List<ArtistInfo>>(emptyList())
    }

    var albums by remember {
        mutableStateOf<List<AlbumInfo>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var hasLoadedLibrary by remember {
        mutableStateOf(false)
    }

    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(
                if (Build.VERSION.SDK_INT >= 33)
                    Manifest.permission.READ_MEDIA_AUDIO
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermission =
        if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
        }

    // CARGA DE BIBLIOTECA

    LaunchedEffect(selectedTab, hasPermission, hasLoadedLibrary) {
        if (selectedTab == 1 && hasPermission && !hasLoadedLibrary) {

            isLoading = true

            try {
                val savedTracks = audioRepository.getSavedTracks()

                tracks = if (savedTracks.isNotEmpty()) {
                    savedTracks
                } else {
                    audioRepository.scanAndSaveTracks(context)
                }

                artists = audioRepository.getArtists()
                albums = audioRepository.getAlbums()

                hasLoadedLibrary = true

            } finally {
                isLoading = false
            }
        }
    }

            BackHandler(
                enabled = selectedAlbum != null ||
                        selectedArtist != null ||
                        selectedTab != 0
            ) {
                when {
                    selectedAlbum != null -> {
                        selectedAlbum = null
                    }

                    selectedArtist != null -> {
                        selectedArtist = null
                    }

                    else -> {
                        selectedTab = 0
                    }
                }
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
        modifier = Modifier.fillMaxSize()
    ) {

        // CONTENIDO DE LAS PANTALLAS

            if (selectedAlbum != null) {

                AlbumDetailScreen(
                    album = selectedAlbum!!,
                    onBack = {
                        selectedAlbum = null
                    },
                    onShuffle = {
                        // Reproducción aleatoria del álbum
                    },
                    onPlay = {
                        // Reproducción del álbum desde el inicio
                    },
                    onFavorite = {
                        val albumKey = "${selectedAlbum!!.artist}_${selectedAlbum!!.title}"

                        favoriteAlbums = if (albumKey in favoriteAlbums) {
                            favoriteAlbums - albumKey
                        } else {
                            favoriteAlbums + albumKey
                        }
                    },
                    isFavorite = "${selectedAlbum!!.artist}_${selectedAlbum!!.title}" in favoriteAlbums,
                    modifier = Modifier.padding(padding)
                )

            } else if (selectedArtist != null) {

                ArtistDetailScreen(
                    artist = selectedArtist!!,
                    onBack = {
                        selectedArtist = null
                    },
                    modifier = Modifier.padding(padding)
                )

            } else {

            when (selectedTab) {

                // INICIO

                0 -> HomeScreen(
                    modifier = Modifier.padding(padding)
                )

                // BIBLIOTECA

                1 -> LibraryScreen(
                    tracks = tracks,
                    listState = libraryListState,
                    artists = artists,
                    albums = albums,
                    onArtistClick = { artist ->
                        selectedArtist = artist
                    },
                    onAlbumClick = { album ->
                        selectedAlbum = album
                    },
                    isLoading = isLoading,
                    hasPermission = hasPermission,
                    searchText = searchText,
                    onSearchTextChange = {
                        searchText = it
                    },
                    selectedLibrarySection = selectedLibrarySection,
                    onLibrarySectionChange = {
                        selectedLibrarySection = it
                    },
                    onRequestPermission = {
                        permissionLauncher.launch(audioPermission)
                    },
                    onRefreshLibrary = {
                        coroutineScope.launch {
                            isLoading = true

                            try {
                                tracks = audioRepository.scanAndSaveTracks(context)
                                artists = audioRepository.getArtists()
                                albums = audioRepository.getAlbums()

                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )

                // AJUSTES

                2 -> SettingsScreen(
                    modifier = Modifier.padding(padding)
                )
            }
        }

        // CÁPSULA FLOTANTE

        if (selectedArtist == null && selectedAlbum == null) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        top = 8.dp,
                        bottom = 14.dp
                    ),
                contentAlignment = Alignment.Center
            ) {

                Row(
                    modifier = Modifier
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    val tabs = listOf(
                        "⌂",
                        "♫",
                        "⚙"
                    )

                    tabs.forEachIndexed { index, icon ->

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedTab == index) {
                                        Color(0xFF292034)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                                .clickable {

                                    selectedTab = index

                                    if (
                                        index == 1 &&
                                        !hasPermission
                                    ) {
                                        permissionLauncher.launch(
                                            audioPermission
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = icon,
                                fontSize = 23.sp,
                                color = if (
                                    selectedTab == index
                                ) Accent else TextSecondary
                            )
                            }
                        }
                    }
                }
          }
        }
    }

    }
}
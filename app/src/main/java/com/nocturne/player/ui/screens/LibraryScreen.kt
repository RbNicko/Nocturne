package com.nocturne.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturne.player.data.AudioTrack
import com.nocturne.player.data.ArtistInfo
import com.nocturne.player.data.AlbumInfo
import com.nocturne.player.ui.components.SongItem
import com.nocturne.player.data.FavoriteEntity
import com.nocturne.player.ui.theme.*
import androidx.compose.foundation.lazy.LazyListState

@Composable
fun LibraryScreen(
    tracks: List<AudioTrack>,
    artists: List<ArtistInfo>,
    albums: List<AlbumInfo>,
    favoriteTracks: List<FavoriteEntity>,
    onTrackFavoriteClick: (AudioTrack) -> Unit,
    isLoading: Boolean,
    hasPermission: Boolean,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedLibrarySection: Int,
    onLibrarySectionChange: (Int) -> Unit,
    onArtistClick: (ArtistInfo) -> Unit,
    onAlbumClick: (AlbumInfo) -> Unit,
    onRequestPermission: () -> Unit,
    onRefreshLibrary: () -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {

    val normalizedSearch = searchText.trim()

    val libraryTracks = remember(
        tracks,
        normalizedSearch,
        selectedLibrarySection
    ) {
        if (selectedLibrarySection == 0) {
            tracks.filter {
                it.title.contains(normalizedSearch, ignoreCase = true) ||
                it.artist.contains(normalizedSearch, ignoreCase = true) ||
                it.album.contains(normalizedSearch, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    val libraryArtists = remember(
        artists,
        normalizedSearch,
        selectedLibrarySection
    ) {
        if (selectedLibrarySection == 1) {
            artists.filter {
                it.name.contains(normalizedSearch, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    val libraryAlbums = remember(
        albums,
        normalizedSearch,
        selectedLibrarySection
    ) {
        if (selectedLibrarySection == 2) {
            albums.filter {
                it.title.contains(normalizedSearch, ignoreCase = true) ||
                it.artist.contains(normalizedSearch, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        contentPadding = PaddingValues(
            top = 28.dp,
            bottom = 28.dp
        )
    ) {

        item {
            Text(
                text = "BIBLIOTECA",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tu música.",
                color = TextPrimary,
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "${tracks.size} canciones en tu colección",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                TextButton(
                    onClick = onRefreshLibrary,
                    enabled = !isLoading && hasPermission
                ) {
                    Text(
                        text = "↻ Actualizar",
                        color = Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Buscar canciones, artistas o álbumes...",
                        color = TextSecondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = SurfaceDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Accent
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Canciones",
                    "Artistas",
                    "Álbumes"
                ).forEachIndexed { index, section ->

                    TextButton(
                        onClick = {
                            onLibrarySectionChange(index)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = section,
                            color = if (
                                selectedLibrarySection == index
                            ) Accent else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (selectedLibrarySection) {
                    0 -> "TODAS LAS CANCIONES"
                    1 -> "TODOS LOS ARTISTAS"
                    else -> "TODOS LOS ÁLBUMES"
                },
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
        }

        when {
            !hasPermission -> {
                item {
                    Text(
                        text = "Nocturne necesita permiso para acceder a tu música.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRequestPermission
                    ) {
                        Text("Conceder permiso")
                    }
                }
            }

            isLoading -> {
                item {
                    CircularProgressIndicator(
                        color = Accent
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Buscando tu música...",
                        color = TextSecondary
                    )
                }
            }

            tracks.isEmpty() -> {
                item {
                    Text(
                        text = "Todavía no encontramos canciones.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            else -> {
                when (selectedLibrarySection) {

                    0 -> {
                        item {
                            Text(
                                text = "${libraryTracks.size} canciones",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (libraryTracks.isEmpty()) {
                            item {
                                Text(
                                    text = "No encontramos coincidencias.",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            items(
                                items = libraryTracks,
                                key = { it.id }
                            ) { track ->
                                SongItem(
                                    title = track.title,
                                    artist = track.artist,
                                    symbol = "♫",
                                    format = track.audioFormat,
                                    isFavorite = favoriteTracks.any {
                                        it.itemId == track.id.toString()
                                    },
                                    onFavoriteClick = {
                                        onTrackFavoriteClick(track)
                                    }
                                )
                            }
                        }
                    }

                    1 -> {
                        item {
                            Text(
                                text = "${libraryArtists.size} artistas",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (libraryArtists.isEmpty()) {
                            item {
                                Text(
                                    text = "No encontramos artistas.",
                                    color = TextSecondary
                                )
                            }
                        } else {
                            items(
                                items = libraryArtists,
                                key = { it.name }
                            ) { artist ->
                                SongItem(
                                    title = artist.name,
                                    artist = "${artist.trackCount} canciones · ${artist.albumCount} álbumes",
                                    symbol = "♫",
                                    onClick = {
                                        onArtistClick(artist)
                                    }
                                )
                            }
                        }
                    }

                    2 -> {
                        item {
                            Text(
                                text = "${libraryAlbums.size} álbumes",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (libraryAlbums.isEmpty()) {
                            item {
                                Text(
                                    text = "No encontramos álbumes.",
                                    color = TextSecondary
                                )
                            }
                        } else {
                            items(
                                items = libraryAlbums,
                                key = { "${it.artist}::${it.title}" }
                            ) { album ->
                                val albumSubtitle = buildString {
                                    append(album.artist)

                                    album.year?.let {
                                        append(" · ")
                                        append(it)
                                    }

                                    append(" · ")
                                    append(album.trackCount)
                                    append(" canciones")
                                }

                                SongItem(
                                    title = album.title,
                                    artist = albumSubtitle,
                                    symbol = "▣",
                                    onClick = {
                                        onAlbumClick(album)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
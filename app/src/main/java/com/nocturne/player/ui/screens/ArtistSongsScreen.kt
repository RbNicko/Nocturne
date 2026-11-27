package com.nocturne.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturne.player.data.ArtistInfo
import com.nocturne.player.data.AudioTrack
import com.nocturne.player.data.LibraryOrganizer
import com.nocturne.player.ui.components.SongItem
import com.nocturne.player.ui.theme.*

private enum class SongSortMode {
    ALPHABETICAL,
    LOCAL_TOP,
    WORLDWIDE_TOP
}

@Composable
fun ArtistSongsScreen(
    artist: ArtistInfo,
    tracks: List<AudioTrack>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val artistTracks = tracks.filter {
        LibraryOrganizer.getMainArtist(it)
            .equals(artist.name, ignoreCase = true)
    }

    var sortMode by remember {
        mutableStateOf(SongSortMode.ALPHABETICAL)
    }

    var sortMenuExpanded by remember {
    mutableStateOf(false)
    }

    val sortedTracks = when (sortMode) {
        SongSortMode.ALPHABETICAL ->
            artistTracks.sortedBy { it.title.lowercase() }

        SongSortMode.LOCAL_TOP ->
            artistTracks

        SongSortMode.WORLDWIDE_TOP ->
            artistTracks
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        // ENCABEZADO

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            TextButton(onClick = onBack) {
                Text(
                    text = "← Volver",
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "CANCIONES",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ARTISTA

        Text(
            text = artist.name,
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(
                start = 22.dp,
                end = 22.dp,
                bottom = 18.dp
            )
        )


            // ORGANIZACIÓN

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "ORGANIZAR",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Box {

                    TextButton(
                        onClick = {
                            sortMenuExpanded = true
                        }
                    ) {
                        Text(
                            text = when (sortMode) {
                                SongSortMode.ALPHABETICAL -> "Alfabético"
                                SongSortMode.LOCAL_TOP -> "Top 10 local"
                                SongSortMode.WORLDWIDE_TOP -> "Top 10 worldwide"
                            },
                            color = TextPrimary,
                            fontSize = 12.sp
                        )

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Cambiar organización",
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = {
                            sortMenuExpanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Alfabético") },
                            onClick = {
                                sortMode = SongSortMode.ALPHABETICAL
                                sortMenuExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Top 10 local") },
                            onClick = {
                                sortMode = SongSortMode.LOCAL_TOP
                                sortMenuExpanded = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Top 10 worldwide") },
                            onClick = {
                                sortMode = SongSortMode.WORLDWIDE_TOP
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }


        Spacer(modifier = Modifier.height(16.dp))

        // LISTA COMPLETA

        if (artistTracks.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay canciones disponibles.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 22.dp,
                    end = 22.dp,
                    bottom = 32.dp
                )
            ) {

                items(
                    items = sortedTracks,
                    key = { it.id }
                ) { track ->

                    SongItem(
                        title = track.title,
                        artist = track.artist,
                        symbol = "♫",
                        format = track.audioFormat
                    )
                }
            }
        }
    }
    }

package com.nocturne.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import com.nocturne.player.data.ArtistInfo
import com.nocturne.player.data.AudioTrack
import com.nocturne.player.data.LibraryOrganizer
import com.nocturne.player.ui.components.SongItem
import com.nocturne.player.ui.theme.*

@Composable
fun ArtistDetailScreen(
    artist: ArtistInfo,
    tracks: List<AudioTrack>,
    onBack: () -> Unit,
    onViewMore: () -> Unit,
    modifier: Modifier = Modifier
) {

    val artistTracks = tracks.filter {
    LibraryOrganizer.getMainArtist(it)
        .equals(artist.name, ignoreCase = true)
}

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        item {

            // BANNER DEL ARTISTA

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to SurfaceDark,
                                0.45f to SurfaceDark,
                                1.0f to Background
                            )
                        )
                    )
            ) {

                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "← Volver",
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
            }

            // FOTO DE PERFIL CENTRADA DEBAJO DEL BANNER

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-30).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .background(Accent),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = artist.name
                                .firstOrNull()
                                ?.uppercase() ?: "?",
                            color = Background,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = artist.name,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                // DESCRIPCIÓN PROVISIONAL

                    Text(
                        text = "Información del artista próximamente.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                // ESTADÍSTICAS

                    Text(
                        text = "${artist.trackCount} canciones · ${artist.albumCount} álbumes",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height((-10).dp))

                Column(
                    modifier = Modifier.padding(horizontal = 22.dp)
                ) {

                // CANCIONES DEL ARTISTA
                Text(
                    text = "CANCIONES",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (artistTracks.isEmpty()) {
                    Text(
                        text = "No hay canciones disponibles.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                } else {
                    artistTracks.take(10).forEach { track ->
                        SongItem(
                            title = track.title,
                            artist = track.artist,
                            symbol = "♫",
                            format = track.audioFormat
                        )
                    }
                }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (artistTracks.size > 10) {

                        TextButton(
                            onClick = onViewMore,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Ver más",
                                color = Accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                // ÁLBUMES PROPIOS

                Text(
                    text = "ÁLBUMES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (artist.albums.isEmpty()) {

                    Text(
                        text = "Todavía no hay álbumes disponibles.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )

                } else {

                    artist.albums.forEach { album ->

                        Text(
                            text = album,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(
                                vertical = 10.dp
                            )
                        )
                    }
                }

                // COLABORACIONES

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "TAMBIÉN APARECE EN",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (artist.featuredAlbums.isEmpty()) {

                    Text(
                        text = "No hay colaboraciones disponibles.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )

                } else {

                    artist.featuredAlbums.forEach { album ->

                        Column(
                            modifier = Modifier.padding(
                                vertical = 10.dp
                            )
                        ) {

                            Text(
                                text = album.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = album.artist,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

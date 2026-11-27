package com.nocturne.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturne.player.data.AlbumInfo
import com.nocturne.player.ui.theme.*


@Composable
fun AlbumDetailScreen(
    album: AlbumInfo,
    onBack: () -> Unit,
    onShuffle: () -> Unit,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        item {

            // CONTENIDO DEL ÁLBUM

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // BANNER DEL ÁLBUM

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

                // INFORMACIÓN DEL ÁLBUM

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = album.title,
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = album.artist,
                        color = TextSecondary,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))


                    // DETALLES DEL ÁLBUM

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlbumMetadataItem(
                            value = album.genre ?: "—",
                            modifier = Modifier.weight(1f)
                        )

                        AlbumMetadataItem(
                            value = album.year?.toString() ?: "—",
                            modifier = Modifier.weight(1f)
                        )

                        AlbumMetadataItem(
                            value = album.audioQuality,
                            modifier = Modifier.weight(1f)
                        )

                        AlbumMetadataItem(
                            value = album.audioFormat,
                            modifier = Modifier.weight(1f)
                        )
                    }

                        Spacer(modifier = Modifier.height(24.dp))

    // BOTONES DEL ÁLBUM

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ALEATORIO

        FilledIconButton(
            onClick = onShuffle,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = SurfaceDark,
                contentColor = TextPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = "Reproducción aleatoria",
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        // REPRODUCIR

        FilledIconButton(
            onClick = onPlay,
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = TextPrimary,
                contentColor = Background
            )
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Reproducir álbum",
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        // FAVORITO

        FilledIconButton(
            onClick = onFavorite,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = SurfaceDark,
                contentColor = TextPrimary
            )
        ) {
            Icon(
                imageVector = if (isFavorite) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Filled.FavoriteBorder
                },
                contentDescription = "Favorito",
                modifier = Modifier.size(23.dp)
            )
        }
    }
        Spacer(modifier = Modifier.height(28.dp))

                    // LISTA DE CANCIONES

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CANCIONES",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // ESPACIO RESERVADO PARA ORGANIZAR
                        Text(
                            text = "ORGANIZAR",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    album.tracks.forEach { track ->

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {

                            Text(
                                text = track.title,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = track.artist,
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

@Composable
private fun AlbumMetadataItem(
    value: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = value,
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier
    )
}
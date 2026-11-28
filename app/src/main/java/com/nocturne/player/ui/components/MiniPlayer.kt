package com.nocturne.player.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturne.player.PlaybackUiState
import com.nocturne.player.ui.theme.*

// Una vuelta completa cada 12 segundos.
private const val DEGREES_PER_SECOND = 30f

/**
 * Portada circular que gira mientras suena la música.
 * Al pausar se queda en el ángulo en que estaba.
 */
@Composable
fun RotatingCover(
    artworkUri: Uri?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var angle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var last = withFrameNanos { it }

            while (true) {
                val now = withFrameNanos { it }

                angle = (angle + (now - last) / 1_000_000_000f *
                    DEGREES_PER_SECOND) % 360f

                last = now
            }
        }
    }

    AlbumArt(
        artworkUri = artworkUri,
        modifier = modifier
            .clip(CircleShape)
            .background(SurfaceDark)
            // El ángulo se lee aquí dentro para no recomponer en cada frame.
            .graphicsLayer { rotationZ = angle },
        placeholder = {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = Accent
            )
        }
    )
}

/**
 * Portada giratoria rodeada de un contorno que se va llenando con el
 * progreso de la canción, con el color principal de la carátula.
 * [progress] devuelve un valor entre 0 y 1.
 */
@Composable
fun NowPlayingOrb(
    artworkUri: Uri?,
    isPlaying: Boolean,
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    val ringColor = rememberArtworkColor(artworkUri)

    Box(
        modifier = modifier
            .drawBehind {
                val stroke = 3.dp.toPx()
                val inset = stroke / 2f
                val arcSize = Size(
                    size.width - stroke,
                    size.height - stroke
                )

                // Contorno base, tenue.
                drawArc(
                    color = ringColor.copy(alpha = 0.25f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke)
                )

                // Parte ya reproducida. Se lee progress() aquí dentro
                // para redibujar solo el contorno, sin recomponer.
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress().coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        RotatingCover(
            artworkUri = artworkUri,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Mini reproductor alargado: portada giratoria, título, artista
 * y botón de pausa.
 */
@Composable
fun MiniPlayerBar(
    playback: PlaybackUiState,
    onTogglePlayPause: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(
                start = 6.dp,
                end = 10.dp,
                top = 6.dp,
                bottom = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        RotatingCover(
            artworkUri = playback.artworkUri,
            isPlaying = playback.isPlaying,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = playback.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = playback.artist,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onTogglePlayPause) {
            Icon(
                imageVector = if (playback.isPlaying) {
                    Icons.Filled.Pause
                } else {
                    Icons.Filled.PlayArrow
                },
                contentDescription = if (playback.isPlaying) {
                    "Pausar"
                } else {
                    "Reproducir"
                },
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
package com.nocturne.player.ui.screens

import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.nocturne.player.PlaybackUiState
import com.nocturne.player.ui.components.AlbumArt
import com.nocturne.player.ui.components.rememberArtworkColor
import com.nocturne.player.ui.theme.Accent

// Relleno y borde de los botones de "cristal".
private val GlassFill = Color.White.copy(alpha = 0.10f)
private val GlassBorder = Color.White.copy(alpha = 0.18f)
private val DarkGlassFill = Color.Black.copy(alpha = 0.28f)

/**
 * Pantalla de reproducción: portada a pantalla completa y controles
 * sobre una capa difuminada de la propia portada.
 */
@Composable
fun PlayerScreen(
    playback: PlaybackUiState,
    positionMs: () -> Long,
    qualityLabel: String?,
    isFavorite: Boolean,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val dominant = rememberArtworkColor(playback.artworkUri)
    val scrimColor = lerp(dominant, Color.Black, 0.75f)
    val seekColor = lerp(dominant, Color.White, 0.5f)

    // Posición (0..1) donde el usuario arrastra la barra; null si no arrastra.
    var dragFraction by remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            // Evita que los toques atraviesen la pantalla hacia lo que hay detrás.
            .pointerInput(Unit) { detectTapGestures { } }
    ) {

        // 1. Portada a pantalla completa. Primero la versión pequeña (que ya
        //    está en memoria y aparece al instante) y encima la de alta calidad.
        AlbumArt(
            artworkUri = playback.artworkUri,
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor),
            sizePx = 192,
            placeholder = {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(120.dp)
                )
            }
        )

        AlbumArt(
            artworkUri = playback.artworkUri,
            modifier = Modifier.fillMaxSize(),
            sizePx = 1024
        )

        // 2. Sombra superior, para que se lean los botones y el texto.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Capa difuminada que aparece poco a poco hacia abajo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()

                    drawRect(
                        brush = Brush.verticalGradient(
                            0.40f to Color.Transparent,
                            0.70f to Color.Black,
                            1.00f to Color.Black
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            AlbumArt(
                artworkUri = playback.artworkUri,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp),
                sizePx = 192
            )
        }

        // 4. Tinte oscuro con el color de la portada.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        0.72f to scrimColor.copy(alpha = 0.55f),
                        1.00f to scrimColor.copy(alpha = 0.95f)
                    )
                )
        )

        // 5. Contenido.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {

            // ---- Barra superior ----

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                GlassCircleButton(
                    icon = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Cerrar",
                    onClick = onClose,
                    size = 52.dp,
                    fill = DarkGlassFill
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NOW PLAYING",
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = playback.album,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                GlassCircleButton(
                    icon = Icons.Filled.MoreVert,
                    contentDescription = "Más opciones",
                    onClick = { },
                    size = 52.dp,
                    fill = DarkGlassFill
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ---- Título, artista y botones ----

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = playback.title,
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = playback.artist,
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                GlassCircleButton(
                    icon = if (isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = "Favorita",
                    onClick = onToggleFavorite,
                    size = 56.dp,
                    tint = if (isFavorite) Color(0xFFFF5C7C) else Color.White
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Letras: todavía sin función.
                GlassCircleButton(
                    icon = Icons.Filled.FormatQuote,
                    contentDescription = "Letra",
                    onClick = { },
                    size = 56.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ---- Barra de progreso y tiempos ----

            SeekBar(
                progress = {
                    fractionOf(positionMs(), playback.durationMs)
                },
                dragFraction = dragFraction,
                onDragChange = { dragFraction = it },
                onSeek = { fraction ->
                    onSeek((fraction * playback.durationMs).toLong())
                },
                color = seekColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            TimeLabels(
                positionMs = positionMs,
                durationMs = playback.durationMs,
                dragFraction = dragFraction,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // ---- Calidad de audio ----

            if (qualityLabel != null) {

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkGlassFill)
                            .border(1.dp, GlassBorder, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = qualityLabel,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---- Controles principales ----

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                GlassCircleButton(
                    icon = Icons.Filled.SkipPrevious,
                    contentDescription = "Anterior",
                    onClick = onPrevious,
                    size = 68.dp,
                    iconSize = 30.dp
                )

                GlassPill(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    onClick = onTogglePlayPause
                ) {
                    Icon(
                        imageVector = if (playback.isPlaying) {
                            Icons.Filled.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (playback.isPlaying) "Pause" else "Play",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                GlassCircleButton(
                    icon = Icons.Filled.SkipNext,
                    contentDescription = "Siguiente",
                    onClick = onNext,
                    size = 68.dp,
                    iconSize = 30.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- Aleatorio, volumen y repetir ----

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                GlassPill(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    onClick = onToggleShuffle
                ) {
                    IconWithDot(
                        icon = Icons.Filled.Shuffle,
                        contentDescription = "Aleatorio",
                        active = playback.shuffle
                    )
                }

                // Volumen | Cola
                GlassPill(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                // Muestra el control de volumen del sistema.
                                context.getSystemService(AudioManager::class.java)
                                    ?.adjustStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        AudioManager.ADJUST_SAME,
                                        AudioManager.FLAG_SHOW_UI
                                    )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "Volumen",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    // Cola de reproducción: todavía sin función.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QueueMusic,
                            contentDescription = "Cola",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                GlassPill(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    onClick = onCycleRepeat
                ) {
                    IconWithDot(
                        icon = if (
                            playback.repeatMode == Player.REPEAT_MODE_ONE
                        ) {
                            Icons.Filled.RepeatOne
                        } else {
                            Icons.Filled.Repeat
                        },
                        contentDescription = "Repetir",
                        active = playback.repeatMode != Player.REPEAT_MODE_OFF
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ----------------------------------------------------------------------
// COMPONENTES
// ----------------------------------------------------------------------

@Composable
private fun GlassCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp,
    modifier: Modifier = Modifier,
    iconSize: Dp = 26.dp,
    fill: Color = GlassFill,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fill)
            .border(1.dp, GlassBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun GlassPill(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}

// Icono con un puntito debajo cuando la función está activa.
@Composable
private fun IconWithDot(
    icon: ImageVector,
    contentDescription: String,
    active: Boolean
) {
    Box(
        modifier = Modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        if (active) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 9.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Barra de progreso: línea fina con un círculo que se puede arrastrar.
 * [progress] devuelve el avance (0..1); se lee al dibujar para no recomponer.
 */
@Composable
private fun SeekBar(
    progress: () -> Float,
    dragFraction: Float?,
    onDragChange: (Float?) -> Unit,
    onSeek: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Siempre se usan las versiones más recientes de las funciones, aunque
    // los gestos se hayan registrado antes (la duración cambia con cada canción).
    val currentOnSeek by rememberUpdatedState(onSeek)
    val currentOnDragChange by rememberUpdatedState(onDragChange)
    val currentDrag by rememberUpdatedState(dragFraction)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    currentOnSeek((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        currentOnDragChange(
                            (offset.x / size.width).coerceIn(0f, 1f)
                        )
                    },
                    onDragEnd = {
                        currentDrag?.let { currentOnSeek(it) }
                        currentOnDragChange(null)
                    },
                    onDragCancel = {
                        currentOnDragChange(null)
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        currentOnDragChange(
                            (change.position.x / size.width).coerceIn(0f, 1f)
                        )
                    }
                )
            }
            .drawBehind {
                val fraction = (dragFraction ?: progress()).coerceIn(0f, 1f)

                val trackHeight = 4.dp.toPx()
                val thumbRadius = 9.dp.toPx()
                val centerY = size.height / 2f

                // El círculo nunca se sale de los extremos de la barra.
                val centerX = thumbRadius + fraction *
                    (size.width - thumbRadius * 2f)

                val corner = CornerRadius(trackHeight / 2f)

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(size.width, trackHeight),
                    cornerRadius = corner
                )

                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(centerX, trackHeight),
                    cornerRadius = corner
                )

                drawCircle(
                    color = color,
                    radius = thumbRadius,
                    center = Offset(centerX, centerY)
                )
            }
    )
}

// Tiempo transcurrido a la izquierda y tiempo restante a la derecha.
@Composable
private fun TimeLabels(
    positionMs: () -> Long,
    durationMs: Long,
    dragFraction: Float?,
    modifier: Modifier = Modifier
) {
    val shownMs = if (dragFraction != null) {
        (dragFraction * durationMs).toLong()
    } else {
        positionMs()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatTime(shownMs),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "-" + formatTime((durationMs - shownMs).coerceAtLeast(0L)),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun fractionOf(positionMs: Long, durationMs: Long): Float =
    if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L

    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
package com.nocturne.player.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.nocturne.player.data.ArtworkLoader
import com.nocturne.player.ui.theme.Accent
import kotlinx.coroutines.delay

/**
 * Muestra la portada de una canción.
 *
 * Mientras carga (o si la canción no tiene portada) se dibuja [placeholder].
 * El tamaño y la forma los define quien la use, con [modifier].
 */
@Composable
fun AlbumArt(
    artworkUri: Uri?,
    modifier: Modifier = Modifier,
    sizePx: Int = 192,
    placeholder: @Composable () -> Unit = {}
) {
    val context = LocalContext.current

    // Si la portada ya está en memoria se usa al instante, sin parpadeo.
    val cached = remember(artworkUri, sizePx) {
        artworkUri
            ?.let { ArtworkLoader.peek(it, sizePx) }
            ?.asImageBitmap()
    }

    val bitmap by produceState<ImageBitmap?>(
        initialValue = cached,
        artworkUri,
        sizePx
    ) {
        // Al cambiar de canción se descarta la portada anterior.
        value = cached

        if (cached == null && artworkUri != null) {
            // Pequeña espera: si la fila sale de pantalla enseguida (flick
            // rápido), esta carga se cancela y nunca llega a empezar.
            delay(70)

            value = ArtworkLoader
                .load(context.applicationContext, artworkUri, sizePx)
                ?.asImageBitmap()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val image = bitmap

        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            placeholder()
        }
    }
}

/**
 * Color principal de la portada. Mientras se calcula (o si no hay portada)
 * devuelve [fallback]. Se usará en el mini reproductor y en la pantalla
 * de reproducción.
 */
@Composable
fun rememberArtworkColor(
    artworkUri: Uri?,
    fallback: Color = Accent
): Color {
    val context = LocalContext.current

    val color by produceState(
        initialValue = fallback,
        artworkUri,
        fallback
    ) {
        value = artworkUri?.let {
            ArtworkLoader.dominantColor(context.applicationContext, it)
        } ?: fallback
    }

    return color
}
package com.nocturne.player.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Carga portadas de canciones y calcula su color dominante.
 *
 * - Las portadas se guardan en una caché en memoria, así que cada una se
 *   lee del archivo una sola vez, aunque aparezca en varias pantallas.
 * - Las canciones sin portada se recuerdan para no volver a intentarlo.
 */
object ArtworkLoader {

    // La caché ocupa como máximo 1/8 de la memoria disponible de la app.
    private val bitmapCache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            value.allocationByteCount / 1024
    }

    // Canciones de las que ya sabemos que no tienen portada.
    private val missing = ConcurrentHashMap.newKeySet<String>()

    // Color dominante por canción (0 = no se pudo calcular).
    private val colorCache = ConcurrentHashMap<String, Int>()

    // Como máximo 3 portadas se cargan al mismo tiempo. Antes se lanzaban
    // decenas a la vez durante un flick y competían con el scroll.
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val loaderDispatcher: CoroutineDispatcher =
        Dispatchers.IO.limitedParallelism(3)

    private fun cacheKey(uri: Uri, sizePx: Int) = "$uri@$sizePx"

    /** Devuelve la portada si ya está en memoria, sin leer ningún archivo. */
    fun peek(uri: Uri, sizePx: Int): Bitmap? =
        bitmapCache.get(cacheKey(uri, sizePx))

    /** Carga la portada de una canción (en un hilo secundario). */
    suspend fun load(
        context: Context,
        uri: Uri,
        sizePx: Int
    ): Bitmap? = withContext(loaderDispatcher) {

        val key = cacheKey(uri, sizePx)

        bitmapCache.get(key)?.let { return@withContext it }

        if (key in missing) return@withContext null

        val bitmap = loadFromThumbnail(context, uri, sizePx)
            ?: loadFromEmbeddedPicture(context, uri, sizePx)

        if (bitmap == null) {
            missing.add(key)
            return@withContext null
        }

        // Se guarda tal cual (en Android 10+ llega como bitmap "hardware",
        // que ya está en la memoria de la GPU y se dibuja sin coste extra).
        bitmapCache.put(key, bitmap)
        bitmap
    }

    /** Carga por adelantado las portadas indicadas, de una en una. */
    suspend fun preload(
        context: Context,
        uris: List<Uri>,
        sizePx: Int
    ) {
        for (uri in uris) {
            load(context, uri, sizePx)
        }
    }

    /** Color principal de la portada, o null si no tiene portada. */
    suspend fun dominantColor(
        context: Context,
        uri: Uri
    ): Color? = withContext(Dispatchers.Default) {

        val key = uri.toString()

        colorCache[key]?.let { cached ->
            return@withContext if (cached == 0) {
                null
            } else {
                Color(cached)
            }
        }

        val bitmap = load(context, uri, 128)

        val rgb = bitmap?.let { original ->
            // Palette necesita un bitmap normal, no "hardware".
            val software =
                if (original.config == Bitmap.Config.HARDWARE) {
                    original.copy(Bitmap.Config.ARGB_8888, false) ?: original
                } else {
                    original
                }

            val palette = Palette.from(software)
                .maximumColorCount(16)
                .generate()
            palette.vibrantSwatch?.rgb ?: palette.dominantSwatch?.rgb
        }

        colorCache[key] = rgb ?: 0

        rgb?.let { Color(it) }
    }

    // Android 10 o superior: el sistema entrega la miniatura ya lista.
    private fun loadFromThumbnail(
        context: Context,
        uri: Uri,
        sizePx: Int
    ): Bitmap? {
        if (Build.VERSION.SDK_INT < 29) return null

        return try {
            context.contentResolver.loadThumbnail(
                uri,
                Size(sizePx, sizePx),
                null
            )
        } catch (e: Exception) {
            null
        }
    }

    // Respaldo (y única opción en Android 9 o inferior):
    // lee la imagen incrustada en el propio archivo de audio.
    private fun loadFromEmbeddedPicture(
        context: Context,
        uri: Uri,
        sizePx: Int
    ): Bitmap? {
        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(context, uri)

            val bytes = retriever.embeddedPicture

            if (bytes == null) {
                null
            } else {
                decodeSampled(bytes, sizePx)
            }
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    // Decodifica reduciendo la imagen para no gastar memoria de más.
    private fun decodeSampled(bytes: ByteArray, targetPx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        var sample = 1

        while (
            bounds.outWidth / (sample * 2) >= targetPx &&
            bounds.outHeight / (sample * 2) >= targetPx
        ) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }
}
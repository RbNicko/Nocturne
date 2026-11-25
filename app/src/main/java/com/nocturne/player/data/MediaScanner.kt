package com.nocturne.player.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun loadAudioTracks(
    context: Context
): List<AudioTrack> = withContext(Dispatchers.IO) {

    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ARTIST,
        MediaStore.Audio.Media.YEAR,
        MediaStore.Audio.Media.COMPOSER,
        MediaStore.Audio.Media.TRACK,
        MediaStore.MediaColumns.MIME_TYPE
    )

    val sortOrder =
        "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

    val tracks = mutableListOf<AudioTrack>()

    context.contentResolver.query(
        collection,
        projection,
        null,
        null,
        sortOrder
    )?.use { cursor ->

        val idColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media._ID
        )

        val titleColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.TITLE
        )

        val artistColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.ARTIST
        )

        val albumColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.ALBUM
        )

        val durationColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.DURATION
        )

        val albumArtistColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.ALBUM_ARTIST
        )

        val yearColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.YEAR
        )

        val composerColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.COMPOSER
        )

        val trackColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.TRACK
        )

        val mimeTypeColumn = cursor.getColumnIndexOrThrow(
            MediaStore.MediaColumns.MIME_TYPE
        )

        while (cursor.moveToNext()) {

            val id = cursor.getLong(idColumn)

            val title = cursor.getString(titleColumn)
                ?: "Título desconocido"

            val artist = cursor.getString(artistColumn)
                ?: "Artista desconocido"

            val album = cursor.getString(albumColumn)
                ?: "Álbum desconocido"

            val duration = cursor.getLong(durationColumn)

            val mimeType = cursor.getString(mimeTypeColumn)
            ?.lowercase()

            val audioFormat = when (mimeType) {
                "audio/flac", "audio/x-flac" -> "FLAC"
                "audio/eac3", "audio/x-eac3" -> "E-AC-3"
                "audio/aac" -> "AAC"
                "audio/mpeg" -> "MP3"
                "audio/ogg" -> "OGG"
                "audio/wav", "audio/x-wav" -> "WAV"
                "audio/alac" -> "ALAC"
                "audio/mp4", "audio/x-m4a" -> "M4A"
                else -> mimeType?.substringAfter("/")?.uppercase()
            }

            val audioQuality = when (audioFormat) {
                "FLAC", "ALAC" -> "Lossless"
                else -> null
            }

            val albumArtist = cursor.getString(albumArtistColumn)
                ?.takeIf { it.isNotBlank() }

            val year = cursor.getInt(yearColumn)
                .takeIf { it > 0 }

            val composer = cursor.getString(composerColumn)
                ?.takeIf { it.isNotBlank() }

            // Los 16 bits inferiores contienen la pista.
            // Los 16 bits superiores contienen el disco.
            val trackValue = cursor.getInt(trackColumn)

            val trackNumber = (trackValue and 0xFFFF)
                .takeIf { it > 0 }

            val discNumber = (trackValue ushr 16)
                .takeIf { it > 0 }

            val contentUri = ContentUris.withAppendedId(
                collection,
                id
            )

            tracks.add(
                AudioTrack(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    uri = contentUri,
                    albumArtist = albumArtist,
                    year = year,
                    composer = composer,
                    trackNumber = trackNumber,
                    discNumber = discNumber,
                    audioFormat = audioFormat,
                    audioQuality = audioQuality
                )
            )
        }
    }

    tracks
}

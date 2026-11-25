package com.nocturne.player.data

import android.net.Uri

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,

    // Metadatos locales complementarios
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val composer: String? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,

    // Identificador externo
    val isrc: String? = null,

    // Calidad y formato de audio
    val audioFormat: String? = null,
    val audioQuality: String? = null,
    val isDolbyAtmos: Boolean? = null
)

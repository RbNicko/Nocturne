package com.nocturne.player

import android.net.Uri
import androidx.media3.common.Player

/**
 * Lo que la interfaz necesita saber de la reproducción actual.
 * (La posición no va aquí porque cambia varias veces por segundo.)
 */
data class PlaybackUiState(
    val hasMedia: Boolean = false,
    val mediaId: String? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUri: Uri? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)
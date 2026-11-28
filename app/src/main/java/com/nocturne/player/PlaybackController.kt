package com.nocturne.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.nocturne.player.data.AudioTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackController(
    context: Context,
    private val onConnected: () -> Unit = {}
) {

    private val sessionToken = SessionToken(
        context,
        ComponentName(context, PlaybackService::class.java)
    )

    // Estado que observa la interfaz (canción actual, si suena, etc.).
    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    // Se avisa de cualquier cambio del reproductor y se actualiza el estado.
    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish()
        }
    }

    private val controllerFuture: ListenableFuture<MediaController> =
        MediaController.Builder(context, sessionToken)
            .buildAsync()

    init {
        controllerFuture.addListener(
            {
                controller?.addListener(playerListener)

                // Si la música ya estaba sonando, se refleja de inmediato.
                publish()

                onConnected()
            },
            { command -> command.run() }
        )
    }

    private val controller: MediaController?
        get() = if (controllerFuture.isDone &&
            !controllerFuture.isCancelled
        ) {
            runCatching { controllerFuture.get() }.getOrNull()
        } else {
            null
        }

    private fun publish() {
        val player = controller ?: return

        val item = player.currentMediaItem
        val metadata = item?.mediaMetadata

        val duration = player.duration.takeIf { it > 0L }
            ?: metadata?.durationMs
            ?: 0L

        _state.value = PlaybackUiState(
            hasMedia = item != null,
            mediaId = item?.mediaId,
            title = metadata?.title?.toString().orEmpty(),
            artist = metadata?.artist?.toString().orEmpty(),
            album = metadata?.albumTitle?.toString().orEmpty(),
            artworkUri = item?.localConfiguration?.uri,
            isPlaying = player.isPlaying,
            durationMs = duration,
            shuffle = player.shuffleModeEnabled,
            repeatMode = player.repeatMode
        )
    }

    fun currentPosition(): Long =
        (controller?.currentPosition ?: 0L).coerceAtLeast(0L)

    fun playTracks(
        tracks: List<AudioTrack>,
        startIndex: Int = 0,
        shuffle: Boolean = false
    ) {
        val player = controller ?: return
        if (tracks.isEmpty()) return

        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setDurationMs(track.duration)
                        .build()
                )
                .build()
        }

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.shuffleModeEnabled = shuffle
        player.prepare()
        player.play()
    }

    fun play() {
        controller?.play()
    }

    fun pause() {
        controller?.pause()
    }

    fun togglePlayPause() {
        val player = controller ?: return

        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        // Si la canción lleva más de unos segundos, vuelve a su inicio;
        // si no, pasa a la anterior.
        controller?.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun toggleShuffle() {
        val player = controller ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun cycleRepeatMode() {
        val player = controller ?: return

        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun release() {
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
    }
}
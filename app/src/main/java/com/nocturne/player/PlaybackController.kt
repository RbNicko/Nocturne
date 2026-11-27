package com.nocturne.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.nocturne.player.data.AudioTrack

class PlaybackController(
    context: Context,
    private val onConnected: () -> Unit = {}
) {

    private val sessionToken = SessionToken(
        context,
        ComponentName(context, PlaybackService::class.java)
    )

    private val controllerFuture: ListenableFuture<MediaController> =
        MediaController.Builder(context, sessionToken)
            .buildAsync()

    init {
        controllerFuture.addListener(
            { onConnected() },
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

    fun release() {
        MediaController.releaseFuture(controllerFuture)
    }
}
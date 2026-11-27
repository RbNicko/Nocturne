package com.nocturne.player

import android.os.SystemClock
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.nocturne.player.data.AudioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {

    companion object {
        private const val PLAY_THRESHOLD_MS = 30_000L
    }

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private lateinit var audioRepository: AudioRepository

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private var trackedMediaId: String? = null
    private var accumulatedPlaybackMs = 0L
    private var playbackStartedAtMs: Long? = null

    private var thresholdJob: Job? = null
    private var countedCurrentTrack = false

    override fun onCreate() {
        super.onCreate()

        audioRepository = AudioRepository(applicationContext)

        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->

                exoPlayer.addListener(object : Player.Listener {

                    override fun onMediaItemTransition(
                        mediaItem: androidx.media3.common.MediaItem?,
                        reason: Int
                    ) {
                        resetTracking(mediaItem?.mediaId)

                        if (exoPlayer.isPlaying) {
                            startTrackingCurrentTrack()
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            startTrackingCurrentTrack()
                        } else {
                            pauseTracking()
                        }
                    }
                })
            }

        mediaSession = MediaSession.Builder(
            this,
            player!!
        ).build()
    }

    private fun resetTracking(mediaId: String?) {
        thresholdJob?.cancel()
        thresholdJob = null

        trackedMediaId = mediaId
        accumulatedPlaybackMs = 0L
        playbackStartedAtMs = null
        countedCurrentTrack = false
    }

    private fun startTrackingCurrentTrack() {
        val currentPlayer = player ?: return

        if (!currentPlayer.isPlaying) return

        val mediaId = currentPlayer.currentMediaItem?.mediaId ?: return

        if (trackedMediaId != mediaId) {
            resetTracking(mediaId)
        }

        if (countedCurrentTrack) return

        if (playbackStartedAtMs != null) return

        playbackStartedAtMs = SystemClock.elapsedRealtime()

        val remainingMs = (
            PLAY_THRESHOLD_MS - accumulatedPlaybackMs
        ).coerceAtLeast(0L)

        thresholdJob = serviceScope.launch {
            delay(remainingMs)

            if (
                trackedMediaId == mediaId &&
                currentPlayer.isPlaying &&
                playbackStartedAtMs != null
            ) {
                registerIfThresholdReached()
            }
        }
    }

    private fun pauseTracking() {
        val startedAt = playbackStartedAtMs ?: return

        accumulatedPlaybackMs += (
            SystemClock.elapsedRealtime() - startedAt
        )

        playbackStartedAtMs = null

        thresholdJob?.cancel()
        thresholdJob = null

        registerIfThresholdReached()
    }

    private fun getCurrentPlaybackTime(): Long {
        val startedAt = playbackStartedAtMs

        return accumulatedPlaybackMs + if (startedAt != null) {
            SystemClock.elapsedRealtime() - startedAt
        } else {
            0L
        }
    }

    private fun registerIfThresholdReached() {
        if (countedCurrentTrack) return

        val mediaId = trackedMediaId ?: return
        val playedMs = getCurrentPlaybackTime()

        if (playedMs < PLAY_THRESHOLD_MS) return

        val trackId = mediaId.toLongOrNull()

        if (trackId == null) {
            Log.e("NocturneStats", "Invalid mediaId: $mediaId")
            return
        }

        countedCurrentTrack = true

        serviceScope.launch {
            try {
                audioRepository.registerPlay(trackId)
            } catch (error: Exception) {
                Log.e(
                    "NocturneStats",
                    "Failed to register playback. trackId=$trackId",
                    error
                )
            }
        }
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        thresholdJob?.cancel()
        serviceScope.cancel()

        mediaSession?.release()
        player?.release()

        mediaSession = null
        player = null

        super.onDestroy()
    }
}
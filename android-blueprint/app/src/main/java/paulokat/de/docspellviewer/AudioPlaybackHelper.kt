package paulokat.de.docspellviewer

import android.media.AudioAttributes
import android.media.MediaPlayer
import java.io.File
import kotlin.math.max

class AudioPlaybackHelper {
    private var mediaPlayer: MediaPlayer? = null
    private var currentAttachmentId: String? = null

    fun hasPreparedPlayer(attachmentId: String): Boolean {
        return currentAttachmentId == attachmentId && mediaPlayer != null
    }

    fun isPlaying(attachmentId: String): Boolean {
        return currentAttachmentId == attachmentId && mediaPlayer?.isPlaying == true
    }

    fun getDurationMs(): Int {
        val duration = mediaPlayer?.duration ?: 0
        return if (duration > 0) duration else 0
    }

    fun getCurrentPositionMs(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun play(file: File, attachmentId: String, onCompleted: () -> Unit): Result<Unit> {
        stop()
        return runCatching {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    onCompleted()
                }
                prepare()
                start()
            }
            mediaPlayer = player
            currentAttachmentId = attachmentId
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun seekTo(positionMs: Int) {
        val player = mediaPlayer ?: return
        val duration = player.duration
        if (duration <= 0) {
            return
        }
        val target = positionMs.coerceIn(0, duration)
        player.seekTo(target)
    }

    fun skipBy(deltaMs: Int) {
        val player = mediaPlayer ?: return
        val duration = player.duration
        if (duration <= 0) {
            return
        }
        val target = (player.currentPosition + deltaMs).coerceIn(0, duration)
        player.seekTo(target)
    }

    fun stop() {
        mediaPlayer?.apply {
            runCatching {
                if (isPlaying) {
                    stop()
                }
            }
            reset()
            release()
        }
        mediaPlayer = null
        currentAttachmentId = null
    }

    fun release() {
        stop()
    }
}

fun formatAudioTime(ms: Long): String {
    val totalSeconds = max(0L, ms / 1000L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

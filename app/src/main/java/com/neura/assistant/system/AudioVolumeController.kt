package com.neura.assistant.system

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent

class AudioVolumeController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun adjustVolume(action: String, percentage: Int? = null): Result<String> {
        val am = audioManager ?: return Result.failure(Exception("Audio service not available"))

        return try {
            when (action.lowercase()) {
                "up" -> {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    Result.success("Volume increased")
                }
                "down" -> {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    Result.success("Volume decreased")
                }
                "mute" -> {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
                    Result.success("Media muted")
                }
                "unmute" -> {
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
                    Result.success("Media unmuted")
                }
                "max" -> {
                    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI)
                    Result.success("Volume set to maximum")
                }
                "set" -> {
                    val targetPercent = (percentage ?: 50).coerceIn(0, 100)
                    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val targetVolume = ((targetPercent / 100.0) * max).toInt()
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI)
                    Result.success("Volume set to $targetPercent%")
                }
                else -> Result.failure(Exception("Unknown volume action '$action'"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun mediaPlayPause(): Result<String> {
        return try {
            val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            audioManager?.dispatchMediaKeyEvent(eventDown)
            audioManager?.dispatchMediaKeyEvent(eventUp)
            Result.success("Toggled media play/pause")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

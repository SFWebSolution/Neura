package com.neura.assistant.voice

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

class TextToSpeechManager(
    private val context: Context,
    private val onSpeakingStarted: (() -> Unit)? = null,
    private val onSpeakingFinished: (() -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var mediaPlayer: MediaPlayer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isInitialized = true
            setupProgressListener()
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                onSpeakingStarted?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                onSpeakingFinished?.invoke()
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                onSpeakingFinished?.invoke()
            }
        })
    }

    fun speak(text: String, pitch: Float = 1.0f, rate: Float = 1.0f) {
        stop()
        if (!isInitialized) return

        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)

        val utteranceId = "neura_tts_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun playAudioFile(audioFile: File) {
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    _isSpeaking.value = false
                    onSpeakingFinished?.invoke()
                }
                start()
            }
            _isSpeaking.value = true
            onSpeakingStarted?.invoke()
        } catch (e: Exception) {
            _isSpeaking.value = false
            onSpeakingFinished?.invoke()
        }
    }

    fun stop() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            // Ignore
        } finally {
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}

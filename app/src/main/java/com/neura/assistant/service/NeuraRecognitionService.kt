package com.neura.assistant.service

import android.content.Intent
import android.speech.RecognitionService

class NeuraRecognitionService : RecognitionService() {
    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        // Recognition callback delegation if needed
    }

    override fun onCancel(listener: Callback?) {
        // Cancel listener
    }

    override fun onStopListening(listener: Callback?) {
        // Stop listener
    }
}

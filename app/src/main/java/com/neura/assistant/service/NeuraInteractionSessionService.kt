package com.neura.assistant.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import com.neura.assistant.ui.MainActivity

class NeuraInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return NeuraInteractionSession(this)
    }
}

class NeuraInteractionSession(context: Context) : VoiceInteractionSession(context) {

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)

        // Launch Neura in Voice Mode or show Assistant overlay
        val intent = Intent(context, MainActivity::class.java).apply {
            action = NeuraForegroundService.ACTION_START_LISTENING
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(intent)
        hide()
    }
}

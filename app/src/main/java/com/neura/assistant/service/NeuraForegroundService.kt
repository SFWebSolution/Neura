package com.neura.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.neura.assistant.R
import com.neura.assistant.ui.MainActivity

class NeuraForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "neura_background_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_LISTENING = "com.neura.assistant.ACTION_START_LISTENING"
        const val ACTION_STOP_SERVICE = "com.neura.assistant.ACTION_STOP_SERVICE"

        fun start(context: Context) {
            val intent = Intent(context, NeuraForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NeuraForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Auto-launch floating glowing orb overlay if permission is granted
        try {
            if (Settings.canDrawOverlays(this)) {
                NeuraOverlayService.start(this)
            }
        } catch (e: Exception) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_LISTENING -> {
                val mainIntent = Intent(this, MainActivity::class.java).apply {
                    action = ACTION_START_LISTENING
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(mainIntent)
            }
        }

        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Neura Siri-Like Background Assistant",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Neura listening continuously in background for 'Neura wake up'"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val listenIntent = Intent(this, NeuraForegroundService::class.java).apply {
            action = ACTION_START_LISTENING
        }
        val listenPendingIntent = PendingIntent.getService(
            this,
            1,
            listenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Neura is Listening")
            .setContentText("Say 'Neura wake up' or tap to speak from anywhere")
            .setSmallIcon(R.drawable.neura_logo)
            .setContentIntent(openPendingIntent)
            .addAction(R.drawable.neura_logo, "🎙️ Talk to Neura", listenPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}

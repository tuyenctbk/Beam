package com.example.beam.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.beam.MainActivity
import com.example.R

class BeamTransferService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_START -> {
                val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Beam Server Active"
                val text = intent?.getStringExtra(EXTRA_TEXT) ?: "Ready for incoming file transfers"
                val progress = intent?.getIntExtra(EXTRA_PROGRESS, -1) ?: -1
                startForegroundWithNotification(title, text, progress)
            }
            ACTION_UPDATE -> {
                val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Transferring Files..."
                val text = intent?.getStringExtra(EXTRA_TEXT) ?: ""
                val progress = intent?.getIntExtra(EXTRA_PROGRESS, -1) ?: -1
                updateNotification(title, text, progress)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification(title: String, text: String, progress: Int) {
        val notification = buildNotification(title, text, progress)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification(title: String, text: String, progress: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(title, text, progress))
    }

    private fun buildNotification(title: String, text: String, progress: Int): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (progress >= 0) {
            builder.setProgress(100, progress, false)
        } else {
            builder.setProgress(0, 0, false)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Beam File Transfers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live transfer progress and HTTP server status"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "beam_transfer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.beam.action.START_SERVICE"
        const val ACTION_UPDATE = "com.example.beam.action.UPDATE_SERVICE"
        const val ACTION_STOP = "com.example.beam.action.STOP_SERVICE"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_PROGRESS = "extra_progress"

        fun start(context: Context, title: String, text: String, progress: Int = -1) {
            val intent = Intent(context, BeamTransferService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TEXT, text)
                putExtra(EXTRA_PROGRESS, progress)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun update(context: Context, title: String, text: String, progress: Int = -1) {
            val intent = Intent(context, BeamTransferService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TEXT, text)
                putExtra(EXTRA_PROGRESS, progress)
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BeamTransferService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

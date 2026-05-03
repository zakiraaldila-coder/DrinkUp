package com.example.drinkup

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    companion object {
        private var activeMediaPlayer: MediaPlayer? = null
        private var activeVibrator: Vibrator? = null

        @Volatile
        var isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == "STOP") {
            stopAlarm()
            return START_NOT_STICKY
        }

        startForeground(1, buildNotification())

        if (isRunning || activeMediaPlayer?.isPlaying == true) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        isRunning = true

        startVibration()
        startAudio()

        return START_NOT_STICKY
    }

    private fun startAudio() {
        activeMediaPlayer?.release()
        activeMediaPlayer = null

        try {
            val afd = resources.openRawResourceFd(R.raw.drink_reminder)
            activeMediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                isLooping = true
                prepareAsync()

                setOnPreparedListener {
                    if (isRunning) it.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        activeVibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 800, 400)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
            activeVibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            activeVibrator?.vibrate(pattern, 0)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP"
        }

        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "DrinkUp Alarm",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("💧 Waktunya Minum Air!")
            .setContentText("Jangan lupa minum ya!")
            .setSmallIcon(R.drawable.ic_notification_drop)
            .addAction(
                R.drawable.ic_stop_red,
                "STOP",
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun stopAlarm() {
        isRunning = false

        activeMediaPlayer?.release()
        activeMediaPlayer = null

        activeVibrator?.cancel()
        activeVibrator = null

        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
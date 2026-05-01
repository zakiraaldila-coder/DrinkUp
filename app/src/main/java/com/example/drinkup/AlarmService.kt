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

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // 🔴 STOP ACTION
        if (intent?.action == "STOP") {
            stopAlarm()
            return START_NOT_STICKY
        }

        // 🔊 PLAY SOUND LOOP
        mediaPlayer = MediaPlayer.create(this, R.raw.drink_reminder)
        mediaPlayer?.setVolume(1.0f, 1.0f)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        // 📳 VIBRATION LOOP
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 800, 400) // delay, vibrate, pause

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0) // 0 = repeat from start
            // ✅ FIX: Pakai USAGE_ALARM agar vibration benar-benar loop di Android baru
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val audioAttr = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                vibrator?.vibrate(effect, audioAttr)
            } else {
                vibrator?.vibrate(effect)
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }

        // 🛑 STOP BUTTON
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP"
        }

        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("DrinkUp Reminder 💧")
            .setContentText("Waktunya minum air!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(0, "STOP", stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()

        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
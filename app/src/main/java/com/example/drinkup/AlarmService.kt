package com.example.drinkup

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import android.os.Handler
import android.os.Looper

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        // ✅ FIX #1: Cegah double-start yang menyebabkan suara menumpuk
        var isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // 🔴 STOP ACTION
        if (intent?.action == "STOP") {
            stopAlarm()
            return START_NOT_STICKY
        }

        if (isRunning) {
            return START_NOT_STICKY
        }
        isRunning = true

        mediaPlayer = MediaPlayer()

        val afd = resources.openRawResourceFd(R.raw.drink_reminder)

        mediaPlayer?.apply {
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            isLooping = true
            setVolume(1.0f, 1.0f)

            prepare()

            Handler(Looper.getMainLooper()).postDelayed({
                mediaPlayer?.start()
            }, 100)
        }

        // 📳 VIBRATION LOOP
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 800, 400)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
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
        val stopIntent = Intent(this, AlarmService::class.java).apply { action = "STOP" }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ FIX #2 & #3: Notifikasi lebih hidup, logo custom, tombol STOP berkesan merah
        val notification = NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("💧 Waktunya Minum Air!")
            .setContentText("DrinkUp mengingatkanmu untuk tetap terhidrasi.")
            .setSubText("DrinkUp Reminder")
            // ✅ FIX #2: Logo DrinkUp kamu — simpan file logo sebagai ic_drinkup_logo di res/drawable
            .setSmallIcon(R.drawable.ic_notification_drop)
            // BigTextStyle agar notif expanded lebih informatif & hidup
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Jangan lupa minum air! Tubuhmu butuh hidrasi yang cukup setiap hari. 🚰")
                    .setBigContentTitle("💧 Waktunya Minum Air!")
                    .setSummaryText("DrinkUp")
            )
            // Warna aksen notifikasi = biru air
            .setColor(0xFF1565C0.toInt())
            .setColorized(true)
            // ✅ FIX #3: Emoji ⛔ sebagai penanda merah + icon stop berwarna merah
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_stop_red,  // drawable icon merah (buat di res/drawable)
                    "⛔  STOP",
                    stopPendingIntent
                ).build()
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .build()

        startForeground(1, notification)
        return START_STICKY
    }

    private fun stopAlarm() {
        isRunning = false

        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
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
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    companion object {
        private var activeMediaPlayer: MediaPlayer? = null
        private var activeVibrator: Vibrator? = null

        @Volatile
        var isRunning = false

        @Volatile
        private var lastStartTime = 0L

        @Volatile
        private var isStopping = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val action = intent?.action
        Log.e("DRINKUP_DEBUG", "SERVICE onStartCommand - startId=$startId action=$action isRunning=$isRunning time=${System.currentTimeMillis()}")

        // ✅ FIX: STOP hanya dari internal service call (startService dari StopAlarmReceiver)
        // Vivo tidak bisa auto-fire ini karena tombol notifikasi sekarang pakai BroadcastReceiver
        if (action == "STOP") {
            if (isRunning) {
                Log.e("DRINKUP_DEBUG", "STOP action received - stopping alarm")
                stopAlarm()
            } else {
                Log.e("DRINKUP_DEBUG", "STOP action ignored - not running")
            }
            return START_NOT_STICKY
        }

        synchronized(this) {
            val now = System.currentTimeMillis()

            if (isRunning) {
                Log.e("DRINKUP_DEBUG", "SERVICE IGNORED - already running, startId=$startId")
                return START_NOT_STICKY
            }

            if (now - lastStartTime < 2000) {
                Log.e("DRINKUP_DEBUG", "SERVICE BLOCKED - too soon, startId=$startId")
                return START_NOT_STICKY
            }

            isRunning     = true
            isStopping    = false
            lastStartTime = now
        }

        val label = intent?.getStringExtra("label") ?: ""
        Log.e("DRINKUP_DEBUG", "SERVICE STARTED - playing alarm")

        startForeground(1, buildNotification(label))
        startVibration()
        startAudio()

        return START_NOT_STICKY
    }

    private fun startAudio() {
        activeMediaPlayer?.release()
        activeMediaPlayer = null

        try {
            val afd = resources.openRawResourceFd(R.raw.drink_reminder)

            val player = MediaPlayer()
            activeMediaPlayer = player

            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            player.isLooping = true

            player.setOnPreparedListener {
                if (isRunning && activeMediaPlayer === player) {
                    Log.e("DRINKUP_DEBUG", "AUDIO STARTED")
                    it.start()
                } else {
                    Log.e("DRINKUP_DEBUG", "AUDIO BLOCKED - instance mismatch or not running")
                    it.release()
                }
            }

            player.prepareAsync()

        } catch (e: Exception) {
            Log.e("DRINKUP_DEBUG", "AUDIO ERROR: ${e.message}")
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

    private fun buildNotification(label: String = ""): Notification {
        // ✅ FIX UTAMA: Tombol STOP pakai BroadcastReceiver, BUKAN PendingIntent service
        val stopIntent = Intent(this, StopAlarmReceiver::class.java).apply {
            action = "STOP_ALARM"
        }

        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tap notifikasi → buka app utama
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 1, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "DrinkUp Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description             = "Pengingat minum air harian"
                enableLights(true)
                lightColor              = android.graphics.Color.CYAN
                enableVibration(false)  // vibration dihandle manual di service
                lockscreenVisibility    = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Waktu sekarang untuk ditampilkan di notifikasi
        val calendar = java.util.Calendar.getInstance()
        val hour     = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute   = calendar.get(java.util.Calendar.MINUTE)
        val timeStr  = String.format("%02d:%02d", hour, minute)

        // Nama pengingat ditampilkan jika ada
        val displayLabel = if (label.isNotBlank()) label else "Pengingat Minum Air"

        // Quotes pendek rotasi
        val quotes = listOf(
            "Tubuhmu butuh air. Minum sekarang! 💧",
            "Satu tegukan lebih baik dari nol. 🥤",
            "Hidrasi = energi. Ayo minum! ⚡",
            "Air adalah bahan bakarmu. 🌊",
            "Jangan tunggu haus. Minum sekarang! 💙"
        )
        val quoteText = quotes[((System.currentTimeMillis() / 60000) % quotes.size).toInt()]

        return NotificationCompat.Builder(this, "alarm_channel")
            // ── Konten utama ─────────────────────────────────────────────────
            .setContentTitle(displayLabel)
            .setContentText(quoteText)
            .setSubText("DrinkUp • $timeStr")
            // ── Ikon ─────────────────────────────────────────────────────────
            .setSmallIcon(R.drawable.ic_notification_drop)
            // ── BigTextStyle ─────────────────────────────────────────────────
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(quoteText)
                    .setSummaryText("Reminder Hidrasi")
            )
            // ── Warna aksen navy ──────────────────────────────────────────────
            .setColor(android.graphics.Color.parseColor("#0D1B3E"))
            .setColorized(true)
            // ── Tombol aksi ───────────────────────────────────────────────────
            .addAction(
                R.drawable.ic_stop_red,
                "Stop",
                stopPendingIntent
            )
            // ── Perilaku ──────────────────────────────────────────────────────
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .build()
    }

    private fun stopAlarm() {
        synchronized(this) {
            if (isStopping) {
                Log.e("DRINKUP_DEBUG", "SERVICE STOP SKIPPED - already stopping")
                return
            }
            isStopping = true
            isRunning  = false
        }

        Log.e("DRINKUP_DEBUG", "SERVICE STOPPED")

        activeMediaPlayer?.release()
        activeMediaPlayer = null

        activeVibrator?.cancel()
        activeVibrator = null

        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("DRINKUP_DEBUG", "SERVICE onDestroy called")

        if (!isStopping) {
            activeMediaPlayer?.release()
            activeMediaPlayer = null
            activeVibrator?.cancel()
            activeVibrator = null
            isRunning  = false
            isStopping = true
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
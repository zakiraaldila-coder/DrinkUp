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
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator    : Vibrator?   = null
    private val handler     = Handler(Looper.getMainLooper())
    private var startRunnable: Runnable?  = null

    companion object {
        // AtomicBoolean agar thread-safe — tidak bisa double-set
        private val started = AtomicBoolean(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // ── STOP ACTION ──────────────────────────────────────────────────────
        if (intent?.action == "STOP") {
            stopAlarm()
            return START_NOT_STICKY
        }

        // ── AtomicBoolean: kalau sudah true, compareAndSet return false → skip ─
        if (!started.compareAndSet(false, true)) {
            // Sudah ada instance yang jalan, abaikan request ini
            return START_NOT_STICKY
        }

        // ── Buat Notification Channel ─────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "DrinkUp Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)    // suara dari MediaPlayer, bukan sistem
                enableVibration(false)  // vibrasi dari kode, bukan sistem
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        // ── Foreground Notification dengan tombol STOP ────────────────────────
        val stopIntent = Intent(this, AlarmService::class.java).apply { action = "STOP" }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("💧 Waktunya Minum Air!")
            .setContentText("DrinkUp mengingatkanmu untuk tetap terhidrasi.")
            .setSmallIcon(R.drawable.ic_notification_drop)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Jangan lupa minum air! Tubuhmu butuh hidrasi yang cukup setiap hari. 🚰")
                    .setBigContentTitle("💧 Waktunya Minum Air!")
                    .setSummaryText("DrinkUp")
            )
            .setColor(0xFF1565C0.toInt())
            .setColorized(true)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_stop_red,
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

        // startForeground HARUS dipanggil sebelum MediaPlayer.start()
        startForeground(1, notification)

        // ── Setup MediaPlayer — start SETELAH foreground siap ────────────────
        try {
            val afd = resources.openRawResourceFd(R.raw.drink_reminder)
            mediaPlayer = MediaPlayer().apply {
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
            }

            // Delay 500ms agar sistem audio stabil setelah foreground dimulai
            startRunnable = Runnable {
                try {
                    if (started.get()) mediaPlayer?.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            handler.postDelayed(startRunnable!!, 500)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ── Vibration ─────────────────────────────────────────────────────────
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 800, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }

        return START_NOT_STICKY
    }

    private fun stopAlarm() {
        // Reset atomic flag agar bisa distart lagi nanti
        started.set(false)

        // Cancel pending delayed start kalau belum sempat mulai
        startRunnable?.let { handler.removeCallbacks(it) }
        startRunnable = null

        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
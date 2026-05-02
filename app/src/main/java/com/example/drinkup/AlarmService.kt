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
        // ✅ MediaPlayer disimpan di companion object (level class, bukan instance)
        //    sehingga sekalipun Android membuat instance Service baru setelah crash,
        //    MediaPlayer lama tetap terdeteksi → tidak dobel.
        private var activeMediaPlayer: MediaPlayer? = null
        private var activeVibrator: Vibrator? = null

        @Volatile
        var isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // 🔴 STOP ACTION
        if (intent?.action == "STOP") {
            stopAlarm()
            return START_NOT_STICKY
        }

        // ✅ FIX 1: startForeground() PERTAMA sebelum apapun.
        // Menutup celah Android FGS 5-detik timeout yang jadi penyebab
        // service crash berkali-kali dan intent menumpuk di queue.
        startForeground(1, buildNotification())

        // ✅ FIX 2: Guard berlapis — cek isRunning DAN apakah MediaPlayer
        // benar-benar sedang playing. Ini menangani kasus di mana isRunning
        // ter-reset akibat service di-kill lalu restart oleh Android.
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
        // Bersihkan instance lama jika ada (safety net)
        activeMediaPlayer?.apply {
            try { if (isPlaying) stop() } catch (_: Exception) {}
            try { reset() } catch (_: Exception) {}
            try { release() } catch (_: Exception) {}
        }
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
                setVolume(1.0f, 1.0f)

                setOnPreparedListener { mp ->
                    if (isRunning) mp.start()
                }

                // Non-blocking — tidak ada jeda yang bisa dimanfaatkan intent duplikat
                prepareAsync()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                activeVibrator?.vibrate(
                    effect,
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                )
            } else {
                activeVibrator?.vibrate(effect)
            }
        } else {
            @Suppress("DEPRECATION")
            activeVibrator?.vibrate(pattern, 0)
        }
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, AlarmService::class.java).apply { action = "STOP" }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("💧 Waktunya Minum Air!")
            .setContentText("DrinkUp mengingatkanmu untuk tetap terhidrasi.")
            .setSubText("DrinkUp Reminder")
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
    }

    private fun stopAlarm() {
        isRunning = false

        activeMediaPlayer?.apply {
            try { if (isPlaying) stop() } catch (_: Exception) {}
            try { reset() } catch (_: Exception) {}
            try { release() } catch (_: Exception) {}
        }
        activeMediaPlayer = null

        activeVibrator?.cancel()
        activeVibrator = null

        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
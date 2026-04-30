package com.example.drinkup

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // ── Baca extras dari Intent ──────────────────────────────────────────
        val reminderId = intent.getIntExtra("reminder_id", 0)
        val hour       = intent.getIntExtra("hour", 0)
        val minute     = intent.getIntExtra("minute", 0)
        val days       = intent.getStringArrayListExtra("days") ?: arrayListOf()
        val label      = intent.getStringExtra("label") ?: "Waktunya Minum Air!"
        val sound      = intent.getStringExtra("sound") ?: "flowing"
        val vibration  = intent.getBooleanExtra("vibration", true)

        val soundUri: Uri = try {
            val soundUri: Uri = try {
                val rawRes = if (sound == "flowing") R.raw.flowing_stream else R.raw.gentle_drop
                Uri.parse("android.resource://${context.packageName}/${rawRes}")
            } catch (e: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }

        val timeStr = String.format("%02d:%02d", hour, minute)

        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val notification = NotificationCompat.Builder(context, "drink_channel")
            .setContentTitle("$label  💧  $timeStr")
            .setContentText("Waktunya minum air! Jaga hidrasi kamu ya 😊")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            // Getaran diatur lewat channel saat app pertama kali dibuat,
            // tapi kita override per-notifikasi di sini:
            .apply {
                if (vibration) {
                    setVibrate(longArrayOf(0, 400, 200, 400))
                } else {
                    setVibrate(null)
                }
            }
            .build()

        val notifId = reminderId * 100 + (System.currentTimeMillis() % 100).toInt()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifId, notification)

        // ── Getaran manual (lebih reliable dari channel vibration) ───────────
        if (vibration) {
            triggerVibration(context)
        }

        // ── Reschedule alarm untuk minggu depan ──────────────────────────────
        // setExactAndAllowWhileIdle tidak repeat otomatis, jadi kita jadwalkan
        // ulang ke 7 hari ke depan dari hari ini.
        val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        AlarmHelper.rescheduleNextWeek(
            context     = context,
            reminderId  = reminderId,
            hour        = hour,
            minute      = minute,
            dayOfWeek   = todayDow,
            allDays     = days.toList(),
            label       = label,
            sound       = sound,
            vibration   = vibration
        )
    }

    // ── Vibration helper ─────────────────────────────────────────────────────
    private fun triggerVibration(context: Context) {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(
                VibrationEffect.createWaveform(pattern, -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        }
    }
}
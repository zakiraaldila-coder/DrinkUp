package com.example.drinkup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "drink_channel_v2"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", 0)
        val hour       = intent.getIntExtra("hour", 0)
        val minute     = intent.getIntExtra("minute", 0)
        val days       = intent.getStringArrayListExtra("days") ?: arrayListOf()
        val label      = intent.getStringExtra("label") ?: "Minum Air"
        val vibration  = intent.getBooleanExtra("vibration", true)

        val soundUri: Uri = try {
            Uri.parse("android.resource://${context.packageName}/${R.raw.drink_reminder}")
        } catch (e: Exception) {
            android.media.RingtoneManager.getDefaultUri(
                android.media.RingtoneManager.TYPE_NOTIFICATION
            )
        }

        val timeStr = String.format("%02d:%02d", hour, minute)

        val messages = listOf(
            "Tubuhmu butuh air sekarang! Yuk minum 💙",
            "Hidrasi itu kunci energimu hari ini! 🌊",
            "Jangan tunggu haus, minum dulu yuk! 💧",
            "Sudah minum air belum? Saatnya sekarang! 😊",
            "Satu tegukan = satu langkah lebih sehat! 🏃",
            "Air putih dulu, semangat terus! ✨"
        )
        val randomMessage = messages.random()

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(randomMessage)
            .setBigContentTitle("💧 $label  •  $timeStr")
            .setSummaryText("DrinkUp — Pengingat Minum Air")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("💧 $label  •  $timeStr")
            .setContentText(randomMessage)
            .setSmallIcon(R.drawable.ic_notification_drop)
            .setColor(0xFF0D47A1.toInt())
            .setColorized(true)
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setLights(Color.CYAN, 1000, 500)
            .apply {
                if (vibration) setVibrate(longArrayOf(0, 400, 200, 400))
                else setVibrate(null)
            }
            .build()

        val notifId = reminderId * 100 + (System.currentTimeMillis() % 100).toInt()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(notifId, notification)

        if (vibration) triggerVibration(context)

        val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        AlarmHelper.rescheduleNextWeek(
            context    = context,
            reminderId = reminderId,
            hour       = hour,
            minute     = minute,
            dayOfWeek  = todayDow,
            allDays    = days.toList(),
            label      = label,
            vibration  = vibration
        )
    }

    private fun createNotificationChannel(context: Context, soundUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Drink Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat minum air DrinkUp"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                enableLights(true)
                lightColor = Color.CYAN
            }

            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun triggerVibration(context: Context) {
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
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
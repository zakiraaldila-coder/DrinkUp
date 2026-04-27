package com.example.drinkup

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // Tampilkan notifikasi
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "drink_channel")
            .setContentTitle("DrinkUp 💧")
            .setContentText("Waktunya minum air! Jaga hidrasi kamu ya 😊")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)

        // ⚠️ PENTING: setExactAndAllowWhileIdle tidak repeating otomatis
        // Harus schedule ulang secara manual setiap kali notifikasi muncul
        val prefs = context.getSharedPreferences("drinkup_prefs", Context.MODE_PRIVATE)
        val intervalMillis = prefs.getLong("reminder_interval", 0L)

        if (intervalMillis > 0L) {
            AlarmHelper.setRepeatingAlarm(context, intervalMillis)
        }
    }
}
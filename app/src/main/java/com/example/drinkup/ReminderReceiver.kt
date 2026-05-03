package com.example.drinkup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // ✅ Log ada DI DALAM onReceive (bukan di luar)
        Log.e("DRINKUP_DEBUG", "=== RECEIVER HIT === time=${System.currentTimeMillis()}")

        val reminderId = intent.getIntExtra("reminder_id", 0)
        val hour       = intent.getIntExtra("hour", 0)
        val minute     = intent.getIntExtra("minute", 0)
        val days       = intent.getStringArrayListExtra("days") ?: arrayListOf()
        val label      = intent.getStringExtra("label") ?: "Minum Air"
        val vibration  = intent.getBooleanExtra("vibration", true)
        val dayOfWeek  = intent.getIntExtra("day_of_week", -1)

        Log.e("DRINKUP_DEBUG", "reminderId=$reminderId | $hour:$minute | day=$dayOfWeek")

        // ✅ Langsung start service — TIDAK ada pengecekan isRunning di sini
        // karena pengecekan di sini rawan race condition.
        // AlarmService sendiri yang tolak via synchronized block + time-based lock.
        val serviceIntent = Intent(context, AlarmService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        Log.e("DRINKUP_DEBUG", "Service start requested")

        // ✅ Reschedule ke minggu depan
        if (dayOfWeek != -1) {
            AlarmHelper.rescheduleNextWeek(
                context,
                reminderId,
                hour,
                minute,
                dayOfWeek,
                days.toList(),
                label,
                vibration
            )
            Log.e("DRINKUP_DEBUG", "Rescheduled next week for day=$dayOfWeek")
        } else {
            Log.e("DRINKUP_DEBUG", "WARNING: day_of_week tidak ada di intent — reschedule skip!")
        }
    }
}
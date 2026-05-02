package com.example.drinkup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {
        // ✅ DEBUG — lihat berapa kali onReceive dipanggil
        Log.e("DRINKUP_DEBUG", "=== ReminderReceiver.onReceive() dipanggil === thread=${Thread.currentThread().name}")
        Log.e("DRINKUP_DEBUG", "intent extras: reminderId=${intent.getIntExtra("reminder_id", -999)}, hour=${intent.getIntExtra("hour", -1)}, minute=${intent.getIntExtra("minute", -1)}")

        val serviceIntent = Intent(context, AlarmService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val reminderId = intent.getIntExtra("reminder_id", 0)
        val hour       = intent.getIntExtra("hour", 0)
        val minute     = intent.getIntExtra("minute", 0)
        val days       = intent.getStringArrayListExtra("days") ?: arrayListOf()
        val label      = intent.getStringExtra("label") ?: "Minum Air"
        val vibration  = intent.getBooleanExtra("vibration", true)

        val todayDow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        Log.e("DRINKUP_DEBUG", "Memanggil rescheduleNextWeek untuk reminderId=$reminderId, dayOfWeek=$todayDow")

        AlarmHelper.rescheduleNextWeek(
            context,
            reminderId,
            hour,
            minute,
            todayDow,
            days.toList(),
            label,
            vibration
        )
    }
}
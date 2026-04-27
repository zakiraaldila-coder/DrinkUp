package com.example.drinkup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmHelper {

    private const val REQUEST_CODE = 1001

    fun setRepeatingAlarm(context: Context, intervalMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Simpan interval ke SharedPreferences supaya ReminderReceiver bisa reschedule
        context.getSharedPreferences("drinkup_prefs", Context.MODE_PRIVATE)
            .edit()
            .putLong("reminder_interval", intervalMillis)
            .apply()

        val pendingIntent = buildPendingIntent(context)
        val triggerTime = System.currentTimeMillis() + intervalMillis

        // Android 12+ wajib check permission SCHEDULE_EXACT_ALARM dulu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Fallback: pakai setAndAllowWhileIdle (kurang presisi tapi tetap jalan)
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context))

        context.getSharedPreferences("drinkup_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("reminder_interval")
            .apply()
    }

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
package com.example.drinkup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmHelper {

    fun scheduleReminder(
        context    : Context,
        reminderId : Int,
        hour       : Int,
        minute     : Int,
        days       : List<String>,
        label      : String,
        vibration  : Boolean
    ) {

        cancelReminder(context, reminderId, days)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val expandedDays = expandDays(days)

        expandedDays.forEach { dayOfWeek ->
            val triggerMillis = nextTriggerMillis(hour, minute, dayOfWeek)
            val requestCode   = buildRequestCode(reminderId, dayOfWeek)
            val pendingIntent = buildPendingIntent(
                context, requestCode, reminderId, hour, minute,
                days, label, vibration
            )
            scheduleExact(alarmManager, triggerMillis, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, reminderId: Int, days: List<String>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        expandDays(days).forEach { dayOfWeek ->
            val requestCode   = buildRequestCode(reminderId, dayOfWeek)
            val pendingIntent = buildPendingIntent(
                context, requestCode, reminderId, 0, 0,
                emptyList(), "", false
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun rescheduleNextWeek(
        context    : Context,
        reminderId : Int,
        hour       : Int,
        minute     : Int,
        dayOfWeek  : Int,
        allDays    : List<String>,
        label      : String,
        vibration  : Boolean
    ) {
        val alarmManager  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerMillis = nextTriggerMillis(hour, minute, dayOfWeek, skipToNextWeek = true)
        val requestCode   = buildRequestCode(reminderId, dayOfWeek)
        val pendingIntent = buildPendingIntent(
            context, requestCode, reminderId, hour, minute,
            allDays, label, vibration
        )
        scheduleExact(alarmManager, triggerMillis, pendingIntent)
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun scheduleExact(
        alarmManager  : AlarmManager,
        triggerMillis : Long,
        pendingIntent : PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent
            )
        }
    }

    private fun nextTriggerMillis(
        hour           : Int,
        minute         : Int,
        dayOfWeek      : Int,
        skipToNextWeek : Boolean = false
    ): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayDow  = cal.get(Calendar.DAY_OF_WEEK)
        var daysAhead = (dayOfWeek - todayDow + 7) % 7

        if (skipToNextWeek) {
            daysAhead += 7
        } else if (daysAhead == 0 && cal.timeInMillis <= System.currentTimeMillis()) {
            daysAhead = 7
        }
        cal.add(Calendar.DAY_OF_YEAR, daysAhead)
        return cal.timeInMillis
    }

    fun expandDays(days: List<String>): List<Int> {
        val map = mapOf(
            "SUN" to Calendar.SUNDAY,
            "MON" to Calendar.MONDAY,
            "TUE" to Calendar.TUESDAY,
            "WED" to Calendar.WEDNESDAY,
            "THU" to Calendar.THURSDAY,
            "FRI" to Calendar.FRIDAY,
            "SAT" to Calendar.SATURDAY
        )
        return when {
            days.contains("EVERYDAY") -> listOf(
                Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
                Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
            )
            days.contains("WEEKENDS") -> listOf(Calendar.SATURDAY, Calendar.SUNDAY)
            else -> days.mapNotNull { map[it] }
        }
    }

    private fun buildRequestCode(reminderId: Int, dayOfWeek: Int) = reminderId * 10 + dayOfWeek

    private fun buildPendingIntent(
        context    : Context,
        requestCode: Int,
        reminderId : Int,
        hour       : Int,
        minute     : Int,
        days       : List<String>,
        label      : String,
        vibration  : Boolean
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("hour",        hour)
            putExtra("minute",      minute)
            putStringArrayListExtra("days", ArrayList(days))
            putExtra("label",       label)
            putExtra("vibration",   vibration)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
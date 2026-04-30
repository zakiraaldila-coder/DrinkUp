package com.example.drinkup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmHelper {

    /**
     * Schedule alarm untuk satu reminder pada semua hari yang dipilih.
     *
     * @param reminderId  ID unik reminder (dipakai sebagai base request code)
     * @param hour        Jam dalam format 24-jam (0-23)
     * @param minute      Menit (0-59)
     * @param days        List hari: "SUN","MON","TUE","WED","THU","FRI","SAT"
     *                    atau listOf("EVERYDAY") / listOf("WEEKENDS")
     * @param label       Nama reminder yang muncul di notifikasi
     * @param sound       "flowing" = suara air mengalir, "drop" = tetesan air
     * @param vibration   true = getaran aktif
     */
    fun scheduleReminder(
        context: Context,
        reminderId: Int,
        hour: Int,
        minute: Int,
        days: List<String>,
        label: String,
        sound: String,
        vibration: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val expandedDays = expandDays(days)

        expandedDays.forEach { dayOfWeek ->
            val triggerMillis = nextTriggerMillis(hour, minute, dayOfWeek)
            val requestCode   = buildRequestCode(reminderId, dayOfWeek)
            val pendingIntent = buildPendingIntent(
                context, requestCode, reminderId, hour, minute,
                expandedDays, label, sound, vibration
            )

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
    }

    /**
     * Batalkan semua alarm untuk satu reminder (semua harinya).
     */
    fun cancelReminder(context: Context, reminderId: Int, days: List<String>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val expandedDays = expandDays(days)

        expandedDays.forEach { dayOfWeek ->
            val requestCode   = buildRequestCode(reminderId, dayOfWeek)
            val pendingIntent = buildPendingIntent(
                context, requestCode, reminderId, 0, 0,
                emptyList(), "", "", false
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Reschedule alarm ke 7 hari ke depan (dipanggil dari ReminderReceiver
     * setelah alarm berbunyi agar reminder tetap repeat mingguan).
     */
    fun rescheduleNextWeek(
        context: Context,
        reminderId: Int,
        hour: Int,
        minute: Int,
        dayOfWeek: Int,         // Calendar.MONDAY, dst
        allDays: List<String>,  // semua hari reminder ini (untuk extras)
        label: String,
        sound: String,
        vibration: Boolean
    ) {
        val alarmManager  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerMillis = nextTriggerMillis(hour, minute, dayOfWeek, skipToNextWeek = true)
        val requestCode   = buildRequestCode(reminderId, dayOfWeek)
        val pendingIntent = buildPendingIntent(
            context, requestCode, reminderId, hour, minute,
            allDays, label, sound, vibration
        )

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

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Hitung waktu trigger berikutnya (hari ini atau minggu depan jika sudah lewat).
     * @param dayOfWeek  Calendar.SUNDAY (1) … Calendar.SATURDAY (7)
     */
    private fun nextTriggerMillis(
        hour: Int,
        minute: Int,
        dayOfWeek: Int,
        skipToNextWeek: Boolean = false
    ): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Geser ke hari yang tepat dalam minggu ini
        val todayDow = cal.get(Calendar.DAY_OF_WEEK)
        var daysAhead = (dayOfWeek - todayDow + 7) % 7

        if (skipToNextWeek) {
            daysAhead += 7
        } else if (daysAhead == 0 && cal.timeInMillis <= System.currentTimeMillis()) {
            // Hari ini tapi waktunya sudah lewat → jadwalkan minggu depan
            daysAhead = 7
        }

        cal.add(Calendar.DAY_OF_YEAR, daysAhead)
        return cal.timeInMillis
    }

    /**
     * Konversi nama hari ke Calendar day-of-week int.
     * EVERYDAY → semua 7 hari, WEEKENDS → SAT+SUN.
     */
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
            days.contains("EVERYDAY") ->
                listOf(
                    Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
                    Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
                )
            days.contains("WEEKENDS") ->
                listOf(Calendar.SATURDAY, Calendar.SUNDAY)
            else -> days.mapNotNull { map[it] }
        }
    }

    /**
     * Request code unik per (reminderId × dayOfWeek).
     * reminderId max ~300 agar tidak overflow int.
     */
    private fun buildRequestCode(reminderId: Int, dayOfWeek: Int): Int =
        reminderId * 10 + dayOfWeek

    private fun buildPendingIntent(
        context: Context,
        requestCode: Int,
        reminderId: Int,
        hour: Int,
        minute: Int,
        days: List<String>,
        label: String,
        sound: String,
        vibration: Boolean
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id",  reminderId)
            putExtra("hour",         hour)
            putExtra("minute",       minute)
            putStringArrayListExtra("days", ArrayList(days))
            putExtra("label",        label)
            putExtra("sound",        sound)
            putExtra("vibration",    vibration)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
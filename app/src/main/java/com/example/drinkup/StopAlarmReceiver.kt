package com.example.drinkup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("DRINKUP_DEBUG", "StopAlarmReceiver - user pressed STOP")
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = "STOP"
        }
        context.startService(serviceIntent)
    }
}
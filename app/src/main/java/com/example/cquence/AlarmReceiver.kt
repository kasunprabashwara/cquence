package com.example.cquence

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.cquence.activities.RunSequenceActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wakeLock: PowerManager.WakeLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmReceiver::lock")
        wakeLock.acquire(10*60*1000L /*10 minutes*/)

        // Create an Intent to stop the alarm
        val stopIntent = Intent(context, RunSequenceActivity::class.java)
        stopIntent.putExtra("sequenceId", intent.getIntExtra("sequenceId", -1))
        stopIntent.putExtra("startAt", intent.getIntExtra("startAt", 0))
        stopIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent= PendingIntent.getActivity(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        pendingIntent.send()

    }
}
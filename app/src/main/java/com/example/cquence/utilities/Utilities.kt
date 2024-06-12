package com.example.cquence.utilities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.cquence.AlarmReceiver
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


fun convertToDate(dateInMillies: Long): LocalDate {
    return Instant.ofEpochMilli(dateInMillies).atZone(ZoneId.systemDefault()).toLocalDate()
}
fun convertToDateTime(dateInMillies: Long): LocalDateTime {
    return Instant.ofEpochMilli(dateInMillies).atZone(ZoneId.systemDefault()).toLocalDateTime()
}
@SuppressLint("ScheduleExactAlarm")
fun setAlarm(context: Context, timeInMillis: Long, sequenceId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("sequenceId", sequenceId)
    intent.putExtra("startAt", 0)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    Toast.makeText(context, "Alarm is scheduled ",Toast.LENGTH_SHORT).show()
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
}
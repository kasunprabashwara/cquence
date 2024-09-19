package com.example.cquence.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import com.example.cquence.AlarmReceiver
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
fun convertToTime(millis: Long): String {
    val minutes = millis / 60000
    val seconds = (millis % 60000) / 1000
    return "${minutes}:${seconds}"
}
@SuppressLint("ScheduleExactAlarm")
fun setAlarm(context: Context, timeInMillis: Long, sequenceId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("sequenceId", sequenceId)
    intent.putExtra("skipTo",0L)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    Toast.makeText(context, "Alarm is scheduled ",Toast.LENGTH_SHORT).show()
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
}
fun getFileName(context: Context, uri: Uri): String {
    var fileName = ""
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex >= 0) {
                fileName = it.getString(displayNameIndex)
            }
        }
    }
    return fileName
}
fun getAudioDuration(context: Context, audioUri: Uri): Long {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, audioUri)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        duration?.toLong() ?: -1L
    } catch (e: Exception) {
        e.printStackTrace()
        -1L
    } finally {
        retriever.release()
    }
}
fun saveAudioToFile(context: Context, audioUri: Uri, fileName:String): Uri {
    val inputStream: InputStream? = context.contentResolver.openInputStream(audioUri)
    val audioDirectory = File(context.filesDir, "audio")
    if (!audioDirectory.exists()) {
        audioDirectory.mkdirs()
    }
    val audioFile = File(audioDirectory, fileName)
    val outputStream = FileOutputStream(audioFile)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return Uri.fromFile(audioFile)
}
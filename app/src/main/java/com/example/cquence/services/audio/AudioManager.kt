package com.example.cquence.services.audio

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import java.util.concurrent.ConcurrentHashMap

class AudioManager(private val context: Context) {

    private val ringtoneCache = ConcurrentHashMap<Uri, Ringtone>()

    fun playRingtone(ringtoneUri: Uri) {
        try {
            val ringtone = ringtoneCache.getOrPut(ringtoneUri) {
                RingtoneManager.getRingtone(context, ringtoneUri)
            }
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isPlaying(ringtoneUri: Uri): Boolean {
        return ringtoneCache[ringtoneUri]?.isPlaying ?: false
    }
    fun stopRingtone(ringtoneUri: Uri) {
        ringtoneCache[ringtoneUri]?.stop()
    }

    fun stopAllRingtones() {
        ringtoneCache.values.forEach { it.stop() }
    }
}

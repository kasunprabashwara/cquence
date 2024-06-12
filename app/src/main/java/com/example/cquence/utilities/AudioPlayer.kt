package com.example.cquence.utilities

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class AudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(audioUri: Uri) {
        try {
            mediaPlayer?.release()  // Release any existing MediaPlayer instance

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, audioUri)
                setOnPreparedListener {
                    it.start()
                }
                setOnCompletionListener {
                    // Handle completion, if needed
                }
                setOnErrorListener { mp, what, extra ->
                    // Handle error, if needed
                    true
                }
                prepareAsync()  // Prepare asynchronously to not block the UI thread
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }
}

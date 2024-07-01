package com.example.cquence.data_types

import android.app.Notification
import kotlinx.serialization.Serializable

@Serializable
data class Action (
    var name: String,
    var isAudioPlayed: Boolean=true,
    var audioURI: String="",
    var audioName: String="",
    var isVibration: Boolean=true,
    var isNotification: Boolean=false,
    var notificationText : String = "",
    var isRepeated: Boolean = false,
    var repeatTimes: Int = 0,
    var repeatInterval: Long = 0,
    var initialDelay : Long = 0,
    val id: Int,
)
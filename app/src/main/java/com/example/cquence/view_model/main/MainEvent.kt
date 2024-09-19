package com.example.cquence.view_model.main

import android.content.Context
import com.example.cquence.data_types.Sequence
import com.example.cquence.data_types.Alarm

sealed interface MainEvent {
    data class AddAlarm(val alarm: Alarm) : MainEvent
    data class AddSequence(val sequence: Sequence) : MainEvent
    data class SelectSequence(val sequence: Sequence?) : MainEvent
    data class DeleteSequence(val sequence: Sequence) : MainEvent
    data class SetAlarmActive(val alarm: Alarm, val context: Context) : MainEvent
    data class StartSequence(val sequenceId: Int, val skipTo: Long = 0L) : MainEvent
}
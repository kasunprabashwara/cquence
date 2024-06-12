package com.example.cquence.view_model.main


import com.example.cquence.data_types.Alarm
import com.example.cquence.data_types.Sequence

data class MainState (
    val alarms: List<Alarm> = emptyList(),
    val sequences: List<Sequence> = emptyList(),
    val selectedAlarm: Alarm? = null,
    val selectedSequence: Sequence? = null,
)
package com.example.cquence.view_model.scheduler

import com.example.cquence.data_types.Action

data class SchedulerState (
    var sequenceName : String = "",
    var scheduledActionList: List<ScheduledAction> = emptyList(),
    var nextScheduledActionIndex : Int = 0,
    var actionHappening : Boolean = false,
    var startTime: Long = System.currentTimeMillis(),
    var hour : String = "00",
    var minute : String = "00",
    var second : String = "00",
)
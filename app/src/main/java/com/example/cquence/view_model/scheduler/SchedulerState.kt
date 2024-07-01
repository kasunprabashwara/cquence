package com.example.cquence.view_model.scheduler


data class SchedulerState (
    var sequenceName : String = "",
    var isRunning : Boolean = false,
    var scheduledActionList: List<ScheduledAction> = emptyList(),
    var nextScheduledActionIndex : Int = 0,
    var actionHappening : Boolean = false,
    var startTime: Long = System.currentTimeMillis(),
    var hour : String = "00",
    var minute : String = "00",
    var second : String = "00",
)
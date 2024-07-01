package com.example.cquence.view_model.scheduler

sealed interface SchedulerEvent {
    data class StartSequence(val sequenceId: Int,val startAt: Long, val skipTo: Long, val playImmediately: Boolean = false) : SchedulerEvent
    data object StopSequence : SchedulerEvent
    data object PauseSequence : SchedulerEvent
    data object PlaySequence : SchedulerEvent
    data class ChangeTime(val time: Long) : SchedulerEvent
}
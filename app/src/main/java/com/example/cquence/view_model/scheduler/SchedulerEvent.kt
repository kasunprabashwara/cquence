package com.example.cquence.view_model.scheduler

sealed interface SchedulerEvent {
    data class StartSequence(val sequenceId: Int, val startAt: Int) : SchedulerEvent
    data object StopSequence : SchedulerEvent
}
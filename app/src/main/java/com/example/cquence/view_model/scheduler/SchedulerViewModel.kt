package com.example.cquence.view_model.scheduler

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cquence.data_types.Action
import com.example.cquence.room_db.SequenceDao
import com.example.cquence.utilities.AudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


class SchedulerViewModel (
    private val appContext : Context,
    private val sequenceDao: SequenceDao) : ViewModel()
{

    private var startTime: Long = System.currentTimeMillis()
    private var actionList: List<Action>? = null
    private var processingActions: List<ProcessingAction> = emptyList()

    // action actuators
    private val audioPlayer = AudioPlayer(appContext)
    private val vibrator = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // this part is to display the time since start of the sequence
    private var timer: Timer? = null
    private var duration: Duration = Duration.ZERO
    private val _hours = MutableStateFlow("00")
    private val _minutes = MutableStateFlow("00")
    private val _seconds = MutableStateFlow("00")

    private fun updateTimer() {
        duration = duration.plus(1.seconds)
        duration.toComponents { hours, minutes, seconds, _ ->
            _seconds.value = seconds.toString()
            _minutes.value = minutes.toString()
            _hours.value = hours.toInt().toString()
        }
    }

    // define state values for the action list
    private val _sequenceName = MutableStateFlow("")
    private var _scheduledActions = MutableStateFlow(emptyList<ScheduledAction>())
    private val _nextScheduledActionIndex = MutableStateFlow(0)
    private val _actionHappening = MutableStateFlow(false)
    val state = combine(
        _scheduledActions,
        _nextScheduledActionIndex,
        _hours,
        _minutes,
        _seconds
    ) { scheduledAction, nextScheduledActionIndex, hours, minutes, seconds ->
        SchedulerState(
            scheduledActionList = scheduledAction,
            nextScheduledActionIndex = nextScheduledActionIndex,
            hour = hours,
            minute = minutes,
            second = seconds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SchedulerState()
    )


    // schedule first 10 actions
//    private fun init(sequenceId: Int) {
//        actionList = sequenceDao.getSequenceById(sequenceId).actionList
//        startTime = System.currentTimeMillis()
//        for (action in actionList!!) {
//            processingAction = processingAction + ProcessingAction(action, action.initialDelay, 0)
//        }
//        schedule(10)
//    }
    private fun schedule(size: Int = 1): Int {
        var scheduledCount = 0
        while (_scheduledActions.value.size <= size) {
            processingActions= processingActions.filter { !it.isCompleted }
            if (processingActions.isEmpty()) {
                break
            }
            processingActions.sortedBy { it.time }
            val nextAction = processingActions.first()
            _scheduledActions.value += ScheduledAction(nextAction.action, nextAction.time + startTime)
            scheduledCount++
            nextAction.repeatedTimes++
            nextAction.time += nextAction.action.repeatInterval
            if (nextAction.repeatedTimes == nextAction.action.repeatTimes || !nextAction.action.isRepeated) {
                nextAction.isCompleted = true
            }
        }
        return scheduledCount
    }

    private fun executeAction(action: Action) {
        if (action.isAudioPlayed) {
            audioPlayer.stopAudio()
            audioPlayer.playAudio(Uri.parse(action.audioURI))
        }
        if (action.isVibration) {
        }
        if (action.isNotification) {
            val notification = Notification.Builder(appContext, "Cquence")
                .setContentTitle(action.name)
                .setContentText(action.notificationText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
            notificationManager.notify(action.id, notification)
        }
    }

    private fun waitOnNextAction() {
        val currentTime = System.currentTimeMillis()
        if (_nextScheduledActionIndex.value < _scheduledActions.value.size) {
            val nextScheduledAction = _scheduledActions.value[_nextScheduledActionIndex.value]
            val timeUntilNextAction = nextScheduledAction.time - currentTime
            if (timeUntilNextAction <= 600000) { // 600 seconds in milliseconds
                // Keep the screen on and wait
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    // Trigger the next action
                    //todo turn off when the action is completed
                    _actionHappening.value = true
                    executeAction(nextScheduledAction.action)
                    schedule(1)
                    _nextScheduledActionIndex.value++
                    waitOnNextAction()
                }, timeUntilNextAction)
//            } else {
//                // Set an alarm to wake up the device
//                val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//                val alarmIntent = Intent(appContext, AlarmReceiver::class.java).let { intent ->
//                    PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//                }
//                // Put the device to sleep
//                // This part depends on the specific requirements and device capabilities
//            }
            }
        }
    }
    private fun startSequence(startAt: Int) {
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            updateTimer()
        }
        _nextScheduledActionIndex.value = startAt
        waitOnNextAction()

    }
    fun onSchedulerEvent(event: SchedulerEvent) {
        when (event) {
            is SchedulerEvent.StartSequence -> {
                viewModelScope.launch{
                    val  sequence = sequenceDao.getSequenceById(event.sequenceId)
                    _sequenceName.value = sequence.name
                    actionList = sequence.actionList
                    startTime = System.currentTimeMillis()+1000
                    for (action in actionList!!) {
                        processingActions = processingActions + ProcessingAction(action, action.initialDelay, 0)
                    }
                    schedule(10)
                    startSequence(event.startAt)
                }
            }

            SchedulerEvent.StopSequence -> {
                viewModelScope.launch{
                    timer?.cancel()
                    audioPlayer.stopAudio()
                    vibrator.cancel()
                    notificationManager.cancelAll()
                }
            }
        }
    }
}

data class ScheduledAction(
    val action: Action,
    val time: Long,
)
data class ProcessingAction(
    val action: Action,
    var time: Long,
    var repeatedTimes : Int,
    var isCompleted : Boolean = false
)


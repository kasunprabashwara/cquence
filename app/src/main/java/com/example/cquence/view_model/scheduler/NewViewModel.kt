package com.example.cquence.view_model.scheduler

import android.app.Notification
import android.app.NotificationChannel
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
import com.example.cquence.services.audio.AudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SchedulerViewModel(
    private val appContext : Context,
    private val sequenceDao: SequenceDao
) : ViewModel()
{
    private var actionList: List<Action>? = null
    private var processingActions: List<ProcessingAction> = emptyList()
    // action actuators
    private val audioManager = AudioManager(appContext)
    private val vibrator = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    init {
        val notificationChannel = NotificationChannel("Cquence", "Cquence", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Cquence notifications"
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private var timer: Timer? = null
    private var duration: Duration = Duration.ZERO
    private val _hours = MutableStateFlow("0")
    private val _minutes = MutableStateFlow("0")
    private val _seconds = MutableStateFlow("0")

    private val _sequenceName = MutableStateFlow("")
    private val _isRunning = MutableStateFlow(false)
    private var _scheduledActions = MutableStateFlow(emptyList<ScheduledAction>())
    private val _nextScheduledActionIndex = MutableStateFlow(0)
    private val _actionHappening = MutableStateFlow(false)

    val state = combine(
        _sequenceName,
        _isRunning,
        _scheduledActions,
        _nextScheduledActionIndex,
        _actionHappening,
        _hours,
        _minutes,
        _seconds
    ) { flows ->
        SchedulerState(
            sequenceName = flows[0] as String,
            isRunning = flows[1] as Boolean,
            scheduledActionList = flows[2] as List<ScheduledAction>,
            nextScheduledActionIndex = flows[3] as Int,
            actionHappening = flows[4] as Boolean,
            hour = flows[5] as String,
            minute = flows[6] as String,
            second = flows[7] as String
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SchedulerState()
    )

    private fun updateState() {
        duration = duration.plus(1.seconds)
        updateTimeState()
    }
    private fun updateTimeState() {
        duration.toComponents { hours, minutes, seconds, _ ->
            _hours.value = hours.toString()
            _minutes.value = minutes.toString()
            _seconds.value = seconds.toString()
        }
    }
    private fun executeAction(scheduledAction: ScheduledAction, skipTo: Long = 0L) {
        _nextScheduledActionIndex.value = scheduledAction.index + 1
        _actionHappening.value = true
        val action = scheduledAction.action
        if (action.isAudioPlayed) {
            audioManager.playAudio(Uri.parse(action.audioURI))
            if(skipTo!=0L) audioManager.seekTo(skipTo.toInt())
        }
        if (action.isVibration) {
            vibrator.defaultVibrator.vibrate(1000)
        }
        if (action.isNotification) {
            val notification = Notification.Builder(appContext, "Cquence")
                .setContentTitle(action.name)
                .setContentText(action.notificationText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
            notificationManager.notify(scheduledAction.time.toInt(), notification)
        }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if(_actionHappening.value && _scheduledActions.value[_nextScheduledActionIndex.value-1]==scheduledAction){
                audioManager.stopAudio()
                _actionHappening.value = false
                if(_nextScheduledActionIndex.value >= _scheduledActions.value.size){
                    stopSequence()
                }
            }
        }, action.audioTimeout - skipTo)
    }
    private fun waitForNextAction() {
        if(_nextScheduledActionIndex.value < _scheduledActions.value.size){
            val handler = Handler(Looper.getMainLooper())
            val nextScheduledAction = _scheduledActions.value[_nextScheduledActionIndex.value]
            handler.postDelayed({
                executeAction(nextScheduledAction)
                waitForNextAction()
            }, nextScheduledAction.time - duration.inWholeMilliseconds)
        }
    }
    private fun playSequence(skipTo: Long = 0) {
        if(_isRunning.value){
            stopSequence()
        }
        if(_scheduledActions.value.last().time < skipTo){
            return
        }
        duration=skipTo.toDuration(DurationUnit.MILLISECONDS)
        _isRunning.value = true
        for(scheduledAction in _scheduledActions.value){
            if(scheduledAction.time < skipTo){
                if(scheduledAction.time + scheduledAction.action.audioTimeout > skipTo){
                    val timeElapsed = (skipTo - scheduledAction.time)
                    executeAction(scheduledAction, timeElapsed)
                    break
                }
                else {
                    _nextScheduledActionIndex.value = scheduledAction.index + 1
                }
            }
            else break
        }
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            updateState()
        }
        waitForNextAction()
    }
    private fun cleanScheduleAll() {
        _scheduledActions.value = emptyList()
        _nextScheduledActionIndex.value = 0
        for (action in actionList!!) {
            processingActions = processingActions + ProcessingAction(action, action.initialDelay, 0)
        }
        //todo for now we only support 250 actions
        schedule(250)
    }
    private fun schedule(size: Int = 1): Int {
        var scheduledCount = 0
        while (_scheduledActions.value.size <= size) {
            processingActions = processingActions.filter { !it.isCompleted }
            if (processingActions.isEmpty()) {
                break
            }
            processingActions = processingActions.sortedWith(compareBy({ it.time }, { it.action.repeatInterval }))
            val nextAction = processingActions.first()
            _scheduledActions.value += ScheduledAction(_scheduledActions.value.size,nextAction.action, nextAction.time)
            scheduledCount++
            nextAction.repeatedTimes++
            nextAction.time += nextAction.action.repeatInterval
            if (nextAction.repeatedTimes == nextAction.action.repeatTimes || !nextAction.action.isRepeated) {
                nextAction.isCompleted = true
            }
        }
        return scheduledCount
    }
    private fun stopSequence() {
        timer?.cancel()
        audioManager.stopAudio()
        vibrator.cancel()
        notificationManager.cancelAll()
        _isRunning.value = false
        _actionHappening.value = false
        _nextScheduledActionIndex.value = 0
        duration = Duration.ZERO
        updateTimeState()
    }
    private fun pauseSequence() {
        timer?.cancel()
        audioManager.stopAudio()
        vibrator.cancel()
        notificationManager.cancelAll()
        _isRunning.value = false
        _actionHappening.value = false
    }
    fun onSchedulerEvent(event: SchedulerEvent) {
        when(event){
            is SchedulerEvent.StartSequence -> {
                viewModelScope.launch {
                    val sequence = sequenceDao.getSequenceById(event.sequenceId)
                    _sequenceName.value = sequence.name
                    actionList = sequence.actionList
                    cleanScheduleAll()
                    if(event.playImmediately){
                        playSequence(event.skipTo)
                    }
                }
            }
            is SchedulerEvent.StopSequence -> {
                viewModelScope.launch {
                    stopSequence()
                }
            }
            is SchedulerEvent.PauseSequence -> {
                viewModelScope.launch {
                    pauseSequence()
                }
            }
            is SchedulerEvent.PlaySequence -> {
                viewModelScope.launch {
                    playSequence(duration.inWholeMilliseconds)
                }
            }
            is SchedulerEvent.ChangeTime -> {
                viewModelScope.launch {
                    playSequence(event.time)
                }
            }
        }
    }
}
data class ScheduledAction(
    val index: Int,
    val action: Action,
    val time: Long,
)
data class ProcessingAction(
    val action: Action,
    var time: Long,
    var repeatedTimes : Int,
    var isCompleted : Boolean = false
)
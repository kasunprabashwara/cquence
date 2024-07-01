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


class SchedulerViewModel (
    private val appContext : Context,
    private val sequenceDao: SequenceDao) : ViewModel()
{

    private var startTime: Long = System.currentTimeMillis()
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

    // this part is to display the time since start of the sequence
    private var timer: Timer? = null
    private var duration: Duration = Duration.ZERO
    private val _hours = MutableStateFlow("00")
    private val _minutes = MutableStateFlow("00")
    private val _seconds = MutableStateFlow("00")


    // define state values for the action list
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
        val currentIndex = _nextScheduledActionIndex.value-1
        if(currentIndex>=0){
            val currentAction = _scheduledActions.value[currentIndex]
            if (currentAction.action.isAudioPlayed){
                val isPlaying =audioManager.isPlaying(Uri.parse(currentAction.action.audioURI))
                if (!isPlaying){
                    _actionHappening.value = false
                }
            }
        }
        duration = duration.plus(1.seconds)
        updateTimeState()
    }
    private fun stopSequence() {
        timer?.cancel()
        audioManager.stopAllRingtones()
        vibrator.cancel()
        notificationManager.cancelAll()
        _isRunning.value = false
        _actionHappening.value = false
        duration = Duration.ZERO
        updateTimeState()
    }
    private fun pauseSequence() {
        timer?.cancel()
        audioManager.stopAllRingtones()
        vibrator.cancel()
        notificationManager.cancelAll()
        _isRunning.value = false
        _actionHappening.value = false
    }
    private fun cleanScheduleAll() {
        startTime = System.currentTimeMillis()
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

    private fun updateTimeState() {
        duration.toComponents { hours, minutes, seconds, _ ->
            _hours.value = hours.toString()
            _minutes.value = minutes.toString()
            _seconds.value = seconds.toString()
        }
    }
    private fun executeAction(action: Action) {
        if (action.isAudioPlayed) {
            audioManager.stopAllRingtones()
            audioManager.playRingtone(Uri.parse(action.audioURI))
        }
        if (action.isVibration) {
            //todo complete this
            vibrator.defaultVibrator.vibrate(1000)
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
        } else {
            // todo stop the sequence
        }
    }
    private fun startSequence(startAt: Long, skipTo:Long) {
        if(_isRunning.value){
            return
        }
        if(_scheduledActions.value.last().time < System.currentTimeMillis()){
            return
        }
        if(_scheduledActions.value.last().time < skipTo){
            return
        }
        for(action in _scheduledActions.value){
            if(action.time < skipTo){
                _nextScheduledActionIndex.value++
            }
            else break
        }
        duration= (skipTo-startAt).toDuration(DurationUnit.MILLISECONDS)
        updateTimeState()
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            updateState()
        }
        _isRunning.value = true
        waitOnNextAction()
    }
    fun onSchedulerEvent(event: SchedulerEvent) {
        when (event) {
            is SchedulerEvent.StartSequence -> {
                viewModelScope.launch{
                    val  sequence = sequenceDao.getSequenceById(event.sequenceId)
                    _sequenceName.value = sequence.name
                    actionList = sequence.actionList
                    cleanScheduleAll()
                    if(event.playImmediately){
                        startSequence(event.startAt,event.skipTo)
                    }
                }
            }

            SchedulerEvent.StopSequence -> {
                viewModelScope.launch{
                    stopSequence()
                }
            }
            SchedulerEvent.PauseSequence -> {
                viewModelScope.launch{
                    pauseSequence()
                }

            }
            SchedulerEvent.PlaySequence -> {
                viewModelScope.launch{
                    startSequence(System.currentTimeMillis(),System.currentTimeMillis())
                }

            }
            is SchedulerEvent.ChangeTime -> {
                viewModelScope.launch{
                    timer?.cancel()
                    duration = event.time.toDuration(DurationUnit.MILLISECONDS)
                    updateTimeState()
                    timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
                        updateState()
                    }
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


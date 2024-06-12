package com.example.cquence.view_model.main


import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cquence.activities.RunSequenceActivity
import com.example.cquence.room_db.AlarmDao
import com.example.cquence.room_db.SequenceDao
import com.example.cquence.utilities.setAlarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class MainViewModel(
    private val alarmDao: AlarmDao,
    private val sequenceDao: SequenceDao,
    private val appContext: Context
) : ViewModel() {
    private val _alarms = alarmDao.getAlarms().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )
    private val _sequences = sequenceDao.getSequences().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )
    private val _state = MutableStateFlow(MainState())
    val state = combine(
        _alarms,
        _sequences
    ) { alarms, sequences ->
        MainState(
            alarms = alarms,
            sequences = sequences
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainState()
    )

    fun onEvent(event: MainEvent) : Boolean {
        when (event) {
            is MainEvent.AddAlarm -> {
                viewModelScope.launch { alarmDao.upsertAlarm(event.alarm) }
            }

            is MainEvent.AddSequence -> {
                viewModelScope.launch {
                    sequenceDao.upsertSequence(event.sequence) }
            }
            is MainEvent.SetAlarmActive -> {
                viewModelScope.launch {
                    val updatedAlarm = event.alarm.copy(isActive = true)
                    alarmDao.upsertAlarm(updatedAlarm)
                    val timeToday = LocalDateTime.now().withHour(updatedAlarm.hour).withMinute(updatedAlarm.minute).withSecond(updatedAlarm.second)
                    setAlarm(event.context, timeToday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), updatedAlarm.sequenceId)
                }
            }

            is MainEvent.StartSequence -> {
                val intent = Intent(appContext, RunSequenceActivity::class.java)
                intent.putExtra("sequenceId", event.sequenceId)
                intent.putExtra("startAt", event.startAt)
                appContext.startActivity(intent)
            }
        }
        return true
    }
}
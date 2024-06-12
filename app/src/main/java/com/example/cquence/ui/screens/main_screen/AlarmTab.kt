package com.example.cquence.ui.screens.main_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cquence.data_types.Alarm
import com.example.cquence.view_model.main.MainEvent

@Composable
fun AlarmCard(
    alarm: Alarm,
    onEvent: (MainEvent) -> Boolean
) {
    var isActive by remember { mutableStateOf(alarm.isActive) }
    val context = LocalContext.current
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = alarm.hour.toString()+" : "+alarm.minute.toString(), style = MaterialTheme.typography.labelLarge)
                Text(text = alarm.date, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = isActive,
                onCheckedChange = {
                    isActive= it
                    if(it){
                        onEvent(MainEvent.SetAlarmActive(alarm,context))
                    }
                }
            )
        }
    }
}
@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    onEvent: (MainEvent) -> Boolean
) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
    ) {
        items(alarms) { alarm ->
            AlarmCard(alarm = alarm, onEvent)
        }
    }
}
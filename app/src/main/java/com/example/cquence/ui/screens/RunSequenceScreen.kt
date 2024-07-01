package com.example.cquence.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cquence.R
import com.example.cquence.data_types.Action
import com.example.cquence.services.convertToDateTime
import com.example.cquence.view_model.scheduler.ScheduledAction
import com.example.cquence.view_model.scheduler.SchedulerEvent
import com.example.cquence.view_model.scheduler.SchedulerState

@Composable
fun RunSequenceScreen(
    state : SchedulerState,
    onEvent: (SchedulerEvent) -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    val actions = state.scheduledActionList
    val nextActionIndex = state.nextScheduledActionIndex

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.sequenceName,
                fontSize = 30.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = "${state.hour}:${state.minute}:${state.second}",
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(actions.size) { index ->
                    val action = actions[index]
                    val isHighlighted = if (state.actionHappening) {
                        index == nextActionIndex - 1
                    } else {
                        index == nextActionIndex
                    }
                    val highlightColor = if (state.actionHappening && isHighlighted) {
                        Color.Green
                    } else if (!state.actionHappening && isHighlighted) {
                        Color.Yellow
                    } else {
                        Color.Transparent
                    }
                    ScheduledActionCard(highlightColor, action)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ){
                Button(
                    onClick = {
                        // Handle change time logic here
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    ),
                ) {
                    Icon(painterResource(id = R.drawable.baseline_fast_forward_24), contentDescription = "Change Time")
                }
                Button(
                    onClick = {
                        if(state.isRunning){
                            onEvent(SchedulerEvent.StopSequence)
                        }
                        else{
                            onEvent(SchedulerEvent.PlaySequence)
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        if(state.isRunning) Color.Red else Color.Green,
                        contentColor = Color.White
                    )
                ) {
                    if(state.isRunning){
                        Icon(
                            painterResource(id = R.drawable.baseline_stop),
                            contentDescription = "Stop"
                        )
                    }
                    else{
                        Icon(
                            painterResource(id = R.drawable.baseline_play_arrow_24),
                            contentDescription = "Play"
                        )
                    }
                }
                Button(
                    onClick = {
                        onEvent(SchedulerEvent.PauseSequence)
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    ),
                ) {
                    Icon(painterResource(id = R.drawable.baseline_pause_24), contentDescription = "Pause")
                }
            }
        }
    }
}

@Composable
private fun ScheduledActionCard(
    highlightColor: Color,
    action: ScheduledAction
) {
    Card(
        border = BorderStroke(2.dp, highlightColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = action.action.name, fontSize = 20.sp)
            Text(text = "Scheduled time: ${convertToDateTime(action.time)}", fontSize = 16.sp)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    RunSequenceScreen(
        state = SchedulerState(
            sequenceName = "Test Sequence",
            hour = "0",
            minute = "0",
            second = "0",
            actionHappening = false,
            isRunning = false,
            scheduledActionList = listOf(
                ScheduledAction(
                    action = Action("Test Action", id = 1),
                    time = System.currentTimeMillis()
                )),
            nextScheduledActionIndex = 0,),
        onEvent = {},
    )

}
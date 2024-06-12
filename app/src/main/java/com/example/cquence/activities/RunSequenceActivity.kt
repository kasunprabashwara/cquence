package com.example.cquence.activities

import android.media.Ringtone
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cquence.R
import com.example.cquence.ui.theme.CquenceTheme
import com.example.cquence.utilities.convertToDate
import com.example.cquence.utilities.convertToDateTime
import com.example.cquence.view_model.scheduler.ScheduledAction
import com.example.cquence.view_model.scheduler.SchedulerEvent
import com.example.cquence.view_model.scheduler.SchedulerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RunSequenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sequenceId = intent.getIntExtra("sequenceId", -1)
        if (sequenceId == -1) {
            Toast.makeText(this, "Invalid sequence id", Toast.LENGTH_SHORT).show()
            finish()
        }
        val startAt = intent.getIntExtra("startAt", 0)
        val viewModel: SchedulerViewModel by viewModel()
        viewModel.onSchedulerEvent(SchedulerEvent.StartSequence(sequenceId, startAt))
        setContent {
            CquenceTheme {
                Scaffold(
                    content = {padding->
                        RunSequenceScreen(viewModel, padding = padding)
                    }
                )
            }
        }
    }

    @Composable
    fun RunSequenceScreen(
        viewModel: SchedulerViewModel,
        modifier: Modifier = Modifier,
        padding: PaddingValues = PaddingValues(0.dp)
    ) {
        val state by viewModel.state.collectAsState()
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
                Text(
                    text = state.sequenceName,
                    fontSize = 24.sp,
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
                        Text("Change Time", fontSize = 16.sp)
                        Icon(painterResource(id = R.drawable.baseline_fast_forward_24), contentDescription = "Change Time")
                    }
                    Button(
                        onClick = {
                            viewModel.onSchedulerEvent(SchedulerEvent.StopSequence)
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Stop", fontSize = 20.sp)
                    }
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
                        Text("Pause", fontSize = 16.sp)
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
}
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    RunSequenceActivity().RunSequenceScreen(viewModel = viewModel())
}
package com.example.cquence.activities

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
import com.example.cquence.services.convertToDateTime
import com.example.cquence.ui.screens.RunSequenceScreen
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
        val startAt = intent.getLongExtra("startAt", System.currentTimeMillis())
        val skipTo = intent.getLongExtra("skipTo",System.currentTimeMillis())
        val viewModel: SchedulerViewModel by viewModel()
        viewModel.onSchedulerEvent(SchedulerEvent.StartSequence(sequenceId, startAt,skipTo,true))
        setContent {
            CquenceTheme {
                Scaffold(
                    content = {padding->
                        val state by viewModel.state.collectAsState()
                        RunSequenceScreen(
                            state,
                            onEvent = viewModel::onSchedulerEvent,
                            padding = padding
                        )
                    }
                )
            }
        }
    }
}


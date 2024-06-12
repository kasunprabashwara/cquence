package com.example.cquence.ui.screens.main_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cquence.data_types.Action
import com.example.cquence.data_types.Sequence
import com.example.cquence.view_model.main.MainEvent

@Preview
@Composable
fun SequenceCardPreview() {
    val sequence = Sequence("Sequence 1", listOf(Action("Action 1", true,"audio", audioName = "audio",id=1)), id = 1)
    SequenceCard(sequence = sequence, onEvent = {true})
}

@Composable
fun SequenceCard(
    sequence: Sequence,
    onEvent: (MainEvent) -> Boolean
) {
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
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){
            Column{
                Text(text = sequence.name, style = MaterialTheme.typography.labelLarge)
                Text(text = sequence.actionList[0].name, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier
                    .size(30.dp)
                    .clickable {}
            )
            Button(
                onClick = {
                    onEvent(MainEvent.StartSequence(sequence.id!!, 0))
                }
            ) {
                Text("Start")
            }
        }
    }
}
@Composable
fun SequencesScreen(
    sequences: List<Sequence>,
    onEvent: (MainEvent) -> Boolean
    ) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(sequences) { sequence ->
            SequenceCard(sequence = sequence, onEvent = onEvent)
        }
    }
}
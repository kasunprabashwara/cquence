package com.example.cquence.ui.screens.main_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cquence.data_types.Action
import com.example.cquence.data_types.Sequence
import com.example.cquence.view_model.main.MainEvent


@Preview(showBackground = true)
@Composable
fun PreviewSequenceCard() {
    val fakeNavController = rememberNavController()
    SequenceCard(
        sequence = Sequence(
            name = "Sequence 1",
            actionList = listOf(
                Action(name = "Action 1",true,"dfa","dsf",id=1),
            ),
            id = 1
        ),
        onEvent = {true},
        navController = fakeNavController
    )

}
@Composable
fun SequenceCard(
    sequence: Sequence,
    onEvent: (MainEvent) -> Boolean,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Delete Sequence") },
            text = { Text("Are you sure you want to delete this sequence?") },
            confirmButton = {
                Button(
                    onClick = {
                        //todo implement this
                        showDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = sequence.name, style = MaterialTheme.typography.titleMedium)
                    if (sequence.actionList.isNotEmpty()){
                        Text(
                            text = sequence.actionList[0].name,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if(expanded){
                            for (i in 1 until sequence.actionList.size){
                                Text(
                                    text = sequence.actionList[i].name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            onEvent(MainEvent.SelectSequence(sequence))
                            navController.navigate("add-edit-sequence")
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = { showDialog = true }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(
                        onClick = {
                            onEvent(MainEvent.StartSequence(sequence.id!!, System.currentTimeMillis()))
                        }
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                    }
                }
            }
        }
    }
}
@Composable
fun SequencesScreen(
    sequences: List<Sequence>,
    onEvent: (MainEvent) -> Boolean,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(sequences) { sequence ->
            SequenceCard(sequence = sequence, onEvent = onEvent, navController = navController)
        }
    }
}
package com.example.cquence.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cquence.view_model.main.MainEvent
import com.example.cquence.data_types.Action
import com.example.cquence.data_types.Sequence
import com.example.cquence.ui.dialogs.ActionDialog

@Preview(showBackground = true)
@Composable
fun PreviewAddSequenceDialog() {
    AddEditSequencesPage(
        onEvent = {
            true
        },
        navController = rememberNavController()
    )
}

@Preview
@Composable
fun ActionCardPreview() {
    ActionCard(Action("Action 1", true,"audio", audioName = "audio",id=1), {})
}

@Composable
fun ActionCard(action: Action, onActionChange: (Action) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { showDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = action.name,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = action.audioName,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
    }

    if (showDialog) {
        ActionDialog(
            action = action,
            onDismiss = { showDialog = false },
            onActionChange = {
                onActionChange(it)
                showDialog = false
            },
            isEditing = true
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSequencesPage(
    onEvent: (MainEvent) -> Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var sequenceName by remember { mutableStateOf(TextFieldValue("")) }
    var actions by remember { mutableStateOf(listOf<Action>()) }
    var showDialog by remember { mutableStateOf(false) }
    var editingAction by remember { mutableStateOf<Action?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Add Sequence") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = sequenceName,
                onValueChange = { sequenceName = it },
                label = { Text("Sequence Name") }
            )
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                items(actions) { action ->
                    ActionCard(action = action) { updatedAction ->
                        actions = actions.map {
                            if (it == action) updatedAction else it
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    editingAction = null
                    showDialog = true
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("Add New Action")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            )
            {
                Button(onClick = {
                    navController.popBackStack()
                },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Cancel")
                }
                Spacer(modifier =Modifier.width(16.dp))
                Button(onClick = {
                    if (sequenceName.text.isEmpty() || actions.isEmpty()) {
                        // Show an error to the user
                    } else {
                        val isSuccess = onEvent(
                            MainEvent.AddSequence(
                                Sequence(
                                    name = sequenceName.text,
                                    actionList = actions,
                                    id = null
                                )
                            )
                        )
                        if (isSuccess) {
                            navController.popBackStack()
                        }
                    }
                },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        if (showDialog) {
            ActionDialog(
                action = editingAction ?: Action("", true,"",id= actions.size + 1),
                onDismiss = { showDialog = false },
                onActionChange = {
                    if (editingAction == null) {
                        actions = actions + it
                    } else {
                        actions = actions.map { action ->
                            if (action == editingAction) it else action
                        }
                    }
                    showDialog = false
                    editingAction = null
                },
                isEditing = editingAction != null
            )
        }
    }
}


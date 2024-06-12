package com.example.cquence.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cquence.view_model.main.MainEvent
import com.example.cquence.data_types.Sequence
import com.example.cquence.data_types.Alarm
import com.example.cquence.utilities.convertToDate

@Preview(showBackground = true)
@Composable
fun PreviewAddAlarmPage() {
    AddEditAlarmPage(
        sequences = emptyList(),
        onEvent = { true },
        navController = rememberNavController()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmPage(
    sequences: List<Sequence>,
    onEvent: (MainEvent) -> Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var alarmName by remember { mutableStateOf(TextFieldValue("")) }
    val timePickerState = rememberTimePickerState(is24Hour = true)
    var datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val date by remember { derivedStateOf { datePickerState.selectedDateMillis?.let {
        convertToDate(it) } } }
    val isActive by remember { mutableStateOf(true) }
    var selectedSequenceId by remember { mutableStateOf<Int?>(null) }
    var timePickerDialog by remember { mutableStateOf(false) }
    var dateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Add Alarm") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (timePickerDialog) {
                TimePickerDialog(
                    timePickerState = timePickerState,
                    onDismiss = { timePickerDialog = false }
                )
            }
            if (dateDialog) {
                DatePickerDialog(
                    onDismissRequest = {
                        dateDialog = false
                    },
                    confirmButton = {
                        dateDialog = false
                    },
                    content = {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            DatePicker(
                                state = datePickerState,
                            )
                        }
                    }
                )
            }
            OutlinedTextField(
                value = alarmName,
                onValueChange = { alarmName = it },
                label = { Text("Alarm Name(Optional)") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.clickable(
                    onClick = {
                        timePickerDialog = true
                    }
                ),
                horizontalArrangement = Arrangement.Center
            ){
                Column{
                    Text(
                        text = "Hour",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = timePickerState.hour.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(100.dp),
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column{
                    Text(
                        text = "Minute",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(100.dp)
                    )
                    Text(
                        text = timePickerState.minute.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(100.dp),
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = date?.year.toString() + "-" + date?.month.toString() + "-" + date?.dayOfMonth.toString(),
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.clickable(
                    onClick = {
                        dateDialog = true
                    }
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            var expanded by remember { mutableStateOf(false) }
            var selectedText by remember { mutableStateOf("Select Sequence") }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange =  { expanded = it }){
                OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .clickable(
                            onClick = { expanded = true }
                        )
                        .menuAnchor(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Arrow"
                        )
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    sequences.forEach { sequence ->
                        DropdownMenuItem(
                            onClick = {
                                selectedText = sequence.name
                                selectedSequenceId = sequence.id
                                expanded = false
                            },
                            text = {
                                Text(sequence.name)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    navController.popBackStack()
                },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    if ( selectedSequenceId == null) {
                        // todo: show error
                    } else {
                        val isSuccess = onEvent(
                            MainEvent.AddAlarm(
                                Alarm(
                                    name = alarmName.text,
                                    sequenceId = selectedSequenceId!!,
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                    date = date.toString(),
                                    isActive = isActive,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    timePickerState: TimePickerState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Select Time") },
        text = {
            TimePicker(timePickerState)
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("OK")
            }
        }
    )
}



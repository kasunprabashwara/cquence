package com.example.cquence.ui.dialogs


import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cquence.data_types.Action
import com.example.cquence.services.audio.AudioRecordContract
import com.example.cquence.services.getAudioDuration
import com.example.cquence.services.getFileName
import com.example.cquence.services.saveAudioToFile



@Preview
@Composable
fun ActionDialogPreview() {
    ActionDialog(Action("Action 1",true,"audio",id=1), {}, {}, false)
}

@Composable
fun ActionDialog(
    action: Action,
    onDismiss: () -> Unit,
    onActionChange: (Action) -> Unit,
    isEditing: Boolean
) {
    val nameState = remember { mutableStateOf(action.name) }
    var audioUri by remember { mutableStateOf<Uri?>(Uri.parse(action.audioURI)) }
    val isAudioPlayedState = remember { mutableStateOf(action.isAudioPlayed) }
    var trackDuration by remember { mutableLongStateOf(0L) }
    var audioTimeout by remember { mutableStateOf(action.audioTimeout.toString()) }
    var tempAudioTimeout by remember { mutableStateOf("") }
    val isVibrationState = remember { mutableStateOf(action.isVibration) }
    val isNotificationState = remember { mutableStateOf(action.isNotification) }
    val notificationTextState = remember { mutableStateOf(action.notificationText) }
    val isRepeatedState = remember { mutableStateOf(action.isRepeated) }
    val repeatTimesState = remember { mutableStateOf(action.repeatTimes.toString()) }
    val repeatIntervalState = remember { mutableStateOf(action.repeatInterval.toString()) }
    val initialDelayState = remember { mutableStateOf(action.initialDelay.toString()) }
    val errorState = remember { mutableStateOf("") }
    val context = LocalContext.current

    // Activity Result Launcher to pick audio file
    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            //todo change this to use file from original location instead of copying it
            audioUri= saveAudioToFile(context, it, getFileName(context, it))
            trackDuration = getAudioDuration(context, audioUri!!)
            audioTimeout = trackDuration.toString()
            tempAudioTimeout = trackDuration.toString()
        }
    }
    val audioRecorderLauncher = rememberLauncherForActivityResult(
        AudioRecordContract()
    ) { uri ->
        uri?.let {
            audioUri = saveAudioToFile(context, it, getFileName(context, it))
            trackDuration = getAudioDuration(context, audioUri!!)
            audioTimeout = trackDuration.toString()
        }
    }
    val audioName by remember {
        derivedStateOf {
            audioUri?.let { getFileName(context, it) } ?: action.audioName
        }
    }
    AlertDialog(
        shape = MaterialTheme.shapes.small,
        onDismissRequest = { onDismiss() },
        title = { Text(if (isEditing) "Edit Action" else "Add Action") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text("Action Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SwitchWithLabel(
                    label = "Play Audio",
                    state = isAudioPlayedState
                )
                if (isAudioPlayedState.value) {
                    Button(onClick = {
                        audioPickerLauncher.launch(arrayOf("audio/*"))
                    },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Pick a Soundtrack")
                    }
                    Button(onClick = {
                        audioRecorderLauncher.launch(Unit)
                    },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Record a Soundtrack")

                    }
                    if (audioName.isNotEmpty()) {
                        Text("Selected Audio: $audioName")
                    }
                    OutlinedTextField(
                        value = tempAudioTimeout,
                        onValueChange = {
                            tempAudioTimeout = it
                        },
                        label = { Text("Audio Timeout (ms)") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // Check the value when the user presses the "Done" action
                                val timeout = tempAudioTimeout.toIntOrNull()
                                if (timeout != null && timeout > 0 && timeout <= trackDuration) {
                                    audioTimeout = tempAudioTimeout
                                }
                                else{
                                    tempAudioTimeout = audioTimeout
                                }
                            }
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                }
                SwitchWithLabel(
                    label = "Enable Vibration",
                    state = isVibrationState
                )
                SwitchWithLabel(
                    label = "Show Notification",
                    state = isNotificationState
                )
                if (isNotificationState.value) {
                    OutlinedTextField(
                        value = notificationTextState.value.ifEmpty { nameState.value },
                        onValueChange = { notificationTextState.value = it },
                        label = { Text("Notification Text") }
                    )
                }
                SwitchWithLabel(
                    label = "Repeat Action",
                    state = isRepeatedState
                )
                if (isRepeatedState.value) {
                    OutlinedTextField(
                        value = repeatTimesState.value,
                        onValueChange = { repeatTimesState.value = it },
                        label = { Text("Repeat Times") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = repeatIntervalState.value,
                        onValueChange = { repeatIntervalState.value = it },
                        label = { Text("Repeat Interval (ms)") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = initialDelayState.value,
                        onValueChange = { initialDelayState.value = it },
                        label = { Text("Initial Delay (ms)") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
                if (errorState.value.isNotEmpty()) {
                    Text(errorState.value, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    //todo add validation
                    action.name = nameState.value
                    action.audioName = audioName
                    action.audioURI = audioUri.toString()
                    action.isAudioPlayed = isAudioPlayedState.value
                    action.audioTimeout = audioTimeout.toLongOrNull() ?: -1L
                    action.isVibration = isVibrationState.value
                    action.isNotification = isNotificationState.value
                    action.notificationText = notificationTextState.value
                    action.isRepeated = isRepeatedState.value
                    action.repeatTimes = repeatTimesState.value.toIntOrNull() ?: 0
                    action.repeatInterval = repeatIntervalState.value.toLongOrNull() ?: 0L
                    action.initialDelay = initialDelayState.value.toLongOrNull() ?: 0L

                    var error = ""
                    if ( isRepeatedState.value )  {
                        if ( action.repeatTimes == 0 ) {
                            error += "Repeat Times must be a number greater than 0\n"
                        }
                        if ( action.repeatInterval == 0L ) {
                            error += "Repeat Interval must be a number greater than 0\n"
                        }
                    }
                    if ( !(action.isVibration || action.isNotification || action.isAudioPlayed) ) {
                        error += "At least one of Vibration, Notification, or Audio must be selected\n"
                    }
                    if ( action.isAudioPlayed && audioUri.toString().isEmpty() ) {
                        error += "Audio must be selected\n"
                    }
                    if ( error.isNotEmpty() ) {
                        errorState.value = error
                    } else {
                        onActionChange(action)
                        onDismiss()
                    }

                },
                shape = MaterialTheme.shapes.small
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = MaterialTheme.shapes.small
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SwitchWithLabel(label: String, state: MutableState<Boolean>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(
            checked = state.value,
            onCheckedChange = { state.value = it }
        )
    }
}
package com.example.cquence.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cquence.koin.appModule
import com.example.cquence.ui.dialogs.CheckExactAlarmPermission
import com.example.cquence.ui.dialogs.RequestNotificationPermission
import com.example.cquence.ui.screens.AddEditAlarmPage
import com.example.cquence.ui.screens.AddEditSequencesPage
import com.example.cquence.ui.screens.main_screen.MainScreen
import com.example.cquence.ui.theme.CquenceTheme
import com.example.cquence.view_model.main.MainViewModel
import com.example.cquence.data_types.Sequence
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startKoin{
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule)
        }
        val viewModel: MainViewModel by viewModel()
        enableEdgeToEdge()
        setContent {
            CquenceTheme {
                val navController = rememberNavController()
                val state by viewModel.state.collectAsState()
                val sdkVersion = Build.VERSION.SDK_INT
                if (sdkVersion >= Build.VERSION_CODES.S ) {
                    CheckExactAlarmPermission()
                }
                if (sdkVersion >= Build.VERSION_CODES.TIRAMISU) {
                    RequestNotificationPermission()
                }
                NavHost(
                    navController = navController,
                    startDestination = "tab-layout",
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { 1000 })
                    },
                    exitTransition = {
                        slideOutHorizontally { -1000 }
                    }
                ) {
                    composable("tab-layout") {
                        MainScreen(
                            state = state,
                            onEvent = viewModel::onEvent,
                            navController = navController
                        )
                    }
                    composable("add-edit-sequence") {
                        AddEditSequencesPage(
                            sequence = state.selectedSequence ?: Sequence( name = "", actionList = listOf(),null),
                            onEvent = viewModel::onEvent,
                            navController = navController
                        )
                    }
                    composable("add-edit-alarm") {
                        AddEditAlarmPage(
                            sequences = state.sequences,
                            onEvent = viewModel::onEvent,
                            navController = navController
                        )
                    }
                }
            }
        }
    }

}



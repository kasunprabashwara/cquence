package com.example.cquence.ui.screens.main_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.cquence.view_model.main.MainEvent
import com.example.cquence.view_model.main.MainState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state : MainState,
    onEvent : (MainEvent) -> Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cquence") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (pagerState.currentPage == 1) {
                        navController.navigate("add-edit-sequence")
                    }
                    else if (pagerState.currentPage == 0) {
                        navController.navigate("add-edit-alarm")
                    }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) {innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                ) {
                    Tab(
                        text = { Text("Alarms") },
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                    Tab(
                        text = { Text("Sequences") },
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)

                            }
                        }
                    )
                }
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> AlarmsScreen(state.alarms, onEvent)
                        1 -> SequencesScreen(state.sequences,onEvent)
                    }
                }
            }
        }

    }
}
package com.example.cquence.koin

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.example.cquence.room_db.AlarmDao
import com.example.cquence.room_db.RoomDB
import com.example.cquence.room_db.SequenceDao
import com.example.cquence.view_model.main.MainViewModel
import com.example.cquence.view_model.scheduler.SchedulerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@RequiresApi(Build.VERSION_CODES.S)
val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            RoomDB::class.java,
            "room-db"
        ).build()
    }
    single<AlarmDao> { get<RoomDB>().alarmDao() }
    single<SequenceDao> { get<RoomDB>().sequenceDao() }
    viewModel<MainViewModel>{
        MainViewModel(
            get<AlarmDao>(),
            get<SequenceDao>(),
            androidContext()
        )
    }
    viewModel<SchedulerViewModel>{
        SchedulerViewModel(
            androidContext(),
            get<SequenceDao>()
        )
    }
}
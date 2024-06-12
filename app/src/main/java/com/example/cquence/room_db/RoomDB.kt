package com.example.cquence.room_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cquence.data_types.Sequence
import com.example.cquence.data_types.Alarm

@Database(
    entities = [Alarm::class, Sequence::class],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun sequenceDao(): SequenceDao
}
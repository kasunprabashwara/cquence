package com.example.cquence.room_db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.cquence.data_types.Alarm

import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Upsert
    suspend fun upsertAlarm(alarm: Alarm)
    @Query("SELECT * FROM Alarm")
    fun getAlarms(): Flow<List<Alarm>>
    @Query("DELETE FROM Alarm WHERE sequenceId = :sequenceId")
    suspend fun deleteAlarmsBySequenceId(sequenceId: Int)
}
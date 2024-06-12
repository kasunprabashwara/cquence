package com.example.cquence.room_db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.cquence.data_types.Sequence
import kotlinx.coroutines.flow.Flow

@Dao
interface SequenceDao {
    @Upsert
    suspend fun upsertSequence(sequence: Sequence)
    @Query("SELECT * FROM Sequence")
    fun getSequences(): Flow<List<Sequence>>
    @Query("SELECT * FROM Sequence WHERE id = :id")
    suspend fun getSequenceById(id: Int): Sequence
}
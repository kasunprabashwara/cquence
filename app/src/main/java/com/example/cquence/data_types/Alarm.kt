package com.example.cquence.data_types

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.cquence.data_types.Sequence

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Sequence::class,
            parentColumns = ["id"],
            childColumns = ["sequenceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Alarm (
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "sequenceId") val sequenceId: Int,
    @ColumnInfo(name= "hour") val hour: Int,
    @ColumnInfo(name = "minute") val minute: Int,
    @ColumnInfo(name = "date" )val date: String,
    @ColumnInfo( name = "isActive") val isActive : Boolean,
    @ColumnInfo(name = "second") val second: Int = 0,
    @PrimaryKey val id: Int?
)
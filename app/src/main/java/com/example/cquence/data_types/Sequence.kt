package com.example.cquence.data_types

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sequence (
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "actionList")
    val actionList: List<Action>,
    @PrimaryKey(autoGenerate = true) val id: Int?
)

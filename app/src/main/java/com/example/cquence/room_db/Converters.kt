package com.example.cquence.room_db

import androidx.room.TypeConverter
import com.example.cquence.data_types.Action
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun jsonToActionList(value: String): List<Action> {
        return Json.decodeFromString(value)
    }
    @TypeConverter
    fun actionListToJson(value: List<Action>): String {
        return Json.encodeToString(value)
    }
}
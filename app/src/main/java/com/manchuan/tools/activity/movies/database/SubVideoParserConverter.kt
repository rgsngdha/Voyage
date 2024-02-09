package com.manchuan.tools.activity.movies.database

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SubVideoParserConverter {
    @TypeConverter
    fun objectToString(list: MutableList<SubVideoParser?>?): String {
        return GsonInstance.instance?.gson?.toJson(list).toString()
    }

    @TypeConverter
    fun stringToObject(json: String?): MutableList<SubVideoParser>? {
        val listType: Type = object : TypeToken<MutableList<SubVideoParser?>?>() {}.type
        return GsonInstance.instance?.gson?.fromJson(json, listType)
    }
}
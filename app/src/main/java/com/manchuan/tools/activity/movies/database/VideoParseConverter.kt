package com.manchuan.tools.activity.movies.database

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class VideoParseConverter {
    @TypeConverter
    fun objectToString(list: MutableList<VideoParse?>?): String {
        return GsonInstance.instance?.gson?.toJson(list).toString()
    }

    @TypeConverter
    fun stringToObject(json: String?): MutableList<VideoParse>? {
        val listType: Type = object : TypeToken<MutableList<VideoParse?>?>() {}.type
        return GsonInstance.instance?.gson?.fromJson(json, listType)
    }
}
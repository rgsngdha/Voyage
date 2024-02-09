package com.manchuan.tools.activity.movies.database

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type

class SubscribeConverter {
    @TypeConverter
    fun objectToString(list: MutableList<SubscribeList?>?): String {
        return GsonInstance.instance?.gson?.toJson(list).toString()
    }

    @TypeConverter
    fun stringToObject(json: String?): MutableList<SubscribeList> {
        val listType: Type = object : TypeToken<MutableList<SubscribeList?>?>() {}.type
        return GsonInstance.instance?.gson?.fromJson(json, listType)!!
    }
}
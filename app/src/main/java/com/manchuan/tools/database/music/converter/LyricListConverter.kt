package com.manchuan.tools.database.music.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.manchuan.tools.activity.audio.model.KuwoSongInfo
import com.manchuan.tools.activity.movies.database.GsonInstance
import java.lang.reflect.Type

class LyricListConverter {

    @TypeConverter
    fun objectToString(list: MutableList<KuwoSongInfo.Data.Lrclist?>?): String {
        return GsonInstance.instance?.gson?.toJson(list).toString()
    }

    @TypeConverter
    fun stringToObject(json: String?): MutableList<KuwoSongInfo.Data.Lrclist>? {
        val listType: Type = object : TypeToken<MutableList<KuwoSongInfo.Data.Lrclist?>?>() {}.type
        return GsonInstance.instance?.gson?.fromJson(json, listType)
    }
}
package com.manchuan.tools.database.music.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.manchuan.tools.activity.audio.model.KuwoSongInfo
import com.manchuan.tools.activity.audio.model.NewKuwoMusicModel
import com.manchuan.tools.activity.game.models.KuwoMusicModel
import com.manchuan.tools.activity.movies.database.GsonInstance
import java.lang.reflect.Type

class AbsListConverter {

    @TypeConverter
    fun objectToString(list: NewKuwoMusicModel.Abslist?): String {
        return GsonInstance.instance?.gson?.toJson(list).toString()
    }

    @TypeConverter
    fun stringToObject(json: String?): NewKuwoMusicModel.Abslist? {
        val listType: Type = object : TypeToken<NewKuwoMusicModel.Abslist?>() {}.type
        return GsonInstance.instance?.gson?.fromJson(json, listType)
    }
}
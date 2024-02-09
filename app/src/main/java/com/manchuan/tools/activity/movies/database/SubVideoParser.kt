package com.manchuan.tools.activity.movies.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


/**
 * @param name 解析关键词
 * @param videoParse 当前关键词的接口
 */
@Serializable
@Parcelize
data class SubVideoParser(var name: String, var videoParse: MutableList<VideoParse>) : Parcelable

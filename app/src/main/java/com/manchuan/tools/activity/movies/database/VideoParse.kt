package com.manchuan.tools.activity.movies.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


/**
 * @author 川懿
 * @description 站源解析接口
 * @param name 解析接口名称
 * @param url 解析接口链接
 * @param ua 解析接口用户标识符 (User-Agent)
 * @param whiteList 解析白名单
 * @param blackList 解析黑名单
 * @param type 接口类型，参数为[VideoParseType.JSON]或[VideoParseType.SNIFFING]，前后分别代表JSON和嗅探
 */
@Serializable
@Parcelize
data class VideoParse(
    var name: String = "",
    var url: String = "",
    var ua: String = "",
    var whiteList: MutableList<String> = mutableListOf(),
    var blackList: MutableList<String> = mutableListOf(),
    var type: VideoParseType = VideoParseType.JSON,
) : Parcelable

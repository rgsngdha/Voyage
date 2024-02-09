package com.manchuan.tools.activity.movies.model

import android.os.Parcelable
import com.manchuan.tools.activity.movies.database.SourceType
import com.manchuan.tools.activity.movies.database.SubVideoParser
import com.manchuan.tools.activity.movies.database.SubscribeList
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @param name 名称
 * @param image 图片链接
 * @param playUrl 播放链接
 * @param category 分类
 * @param json 返回的JSON
 * @param searchUrl 搜索URL
 * @param remarks 描述
 * @param sourceType 源类型
 * @param videoUrl 视频链接
 * @param subscribeModel 订阅模型
 * @param videoParser 解析列表
 */
@Parcelize
@Serializable
data class MovieCount(
    var subscribeName: String = "",
    var sourceName: String = "",
    var name: String = "",
    var image: String = "",
    var playUrl: String = "",
    var id: Int = 0,
    var category: String = "",
    var json: String = "",
    var searchUrl: String = "",
    var remarks: String = "",
    var sourceType: SourceType = SourceType.SITE,
    var videoUrl: String = "",
    var subscribeModel: SubscribeList? = null,
    var videoParser: MutableList<SubVideoParser>? = null,
) : Parcelable, java.io.Serializable
package com.manchuan.tools.activity.movies.database

import android.os.Parcelable
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * @author 航
 */

/**
 * @param name 源名称
 * @param superInterface 解析接口
 * @param playerUa 播放UA
 * @param poster 海报
 * @param playUrl 直播播放链接
 * @param searchUa 搜索UA
 * @param lineNameRule 线路名规则
 * @param movieIntroduce 影片简介规则
 * @param movieNameRule 影片名规则
 * @param postParam POST参数
 * @param actorRule 演员获取规则
 * @param detailMovieStatusRule 详情影片状态规则
 * @param videoUrlRule 视频链接规则
 * @param searchParam 搜索参数
 * @param posterRule 海报规则
 * @param sourceType 源类型
 * @param detailPageUrlRule 详情页规则
 * @param listRule 列表规则
 * @param snifferWhiteList 嗅探白名单
 * @param searchApi 搜索API
 * @param searchUrl 搜索地址
 * @param episodeNameRule 集数名规则
 * @param snifferBlackList 嗅探黑名单
 * @param selectEpisodeUrl 选集链接
 * @param movieStatusRule 影片状态规则
 */

@Serializable
@Parcelize
@TypeConverters(SubscribeConverter::class)
data class SubscribeList(
    var name: String = "",
    var playUrl: String = "",
    var poster: String = "",
    var superInterface: String = "",
    var playerUa: String = "",
    var searchUa: String = "",
    var searchParam: String = "",
    var lineNameRule: String = "",
    var movieIntroduce: String = "",
    var movieNameRule: String = "",
    var postParam: String = "",
    var selectEpisodeRule: String = "",
    var selectEpisodeUrl: String = "",
    var actorRule: String = "",
    var detailMovieStatusRule: String = "",
    var videoUrlRule: String = "",
    var posterRule: String = "",
    var sourceType: SourceType = SourceType.SITE,
    var detailPageUrlRule: String = "",
    var listRule: String = "",
    var snifferWhiteList: String = "",
    var searchApi: String = "",
    var searchUrl: String = "",
    var episodeNameRule: String = "",
    var snifferBlackList: String = "",
    var movieStatusRule: String = "",
) : java.io.Serializable, Parcelable

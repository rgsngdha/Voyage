package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AHModel(
    @SerialName("POST参数")
    var pOST参数: String,
    @SerialName("列表规则")
    var 列表规则: String,
    @SerialName("嗅探白名单")
    var 嗅探白名单: String,
    @SerialName("嗅探黑名单")
    var 嗅探黑名单: String,
    @SerialName("影片名规则")
    var 影片名规则: String,
    @SerialName("影片状态规则")
    var 影片状态规则: String,
    @SerialName("影片简介")
    var 影片简介: String,
    @SerialName("搜索API")
    var 搜索API: String,
    @SerialName("搜索URL")
    var 搜索URL: String,
    @SerialName("海报规则")
    var 海报规则: String,
    @SerialName("源名字")
    var 源名字: String,
    @SerialName("源类型")
    var 源类型: String,
    @SerialName("演员获取规则")
    var 演员获取规则: String,
    @SerialName("线路名规则")
    var 线路名规则: String,
    @SerialName("网页选集链接")
    var 网页选集链接: String,
    @SerialName("视频链接规则")
    var 视频链接规则: String,
    @SerialName("详情影片状态规则")
    var 详情影片状态规则: String,
    @SerialName("详情页链接规则")
    var 详情页链接规则: String,
    @SerialName("集数名规则")
    var 集数名规则: String
)
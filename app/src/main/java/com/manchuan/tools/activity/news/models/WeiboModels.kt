package com.manchuan.tools.activity.news.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeiboModels(
    @SerialName("data")
    var `data`: List<Data>,
    @SerialName("success")
    var success: Boolean,
    @SerialName("time")
    var time: String
) {
    @Serializable
    data class Data(
        @SerialName("hot")
        var hot: String,
        @SerialName("title")
        var title: String,
        @SerialName("url")
        var url: String
    )
}
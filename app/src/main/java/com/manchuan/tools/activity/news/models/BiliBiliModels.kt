package com.manchuan.tools.activity.news.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BiliBiliModels(
    @SerialName("data")
    var `data`: List<Data>,
    @SerialName("subtitle")
    var subtitle: String,
    @SerialName("success")
    var success: Boolean,
    @SerialName("title")
    var title: String,
    @SerialName("update_time")
    var updateTime: String
) {
    @Serializable
    data class Data(
        @SerialName("desc")
        var desc: String,
        @SerialName("hot")
        var hot: String,
        @SerialName("index")
        var index: Int,
        @SerialName("mobilUrl")
        var mobilUrl: String,
        @SerialName("pic")
        var pic: String,
        @SerialName("title")
        var title: String,
        @SerialName("url")
        var url: String
    )
}
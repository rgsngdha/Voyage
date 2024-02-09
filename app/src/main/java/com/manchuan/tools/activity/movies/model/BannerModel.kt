package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BannerModel(
    @SerialName("code")
    var code: String,
    @SerialName("data")
    var `data`: List<Data>,
    @SerialName("msg")
    var msg: String,
) {
    @Serializable
    data class Data(
        @SerialName("clickUrl")
        var clickUrl: String,
        @SerialName("image")
        var image: String,
        @SerialName("subtitle")
        var subtitle: String,
        @SerialName("title")
        var title: String,
    )
}
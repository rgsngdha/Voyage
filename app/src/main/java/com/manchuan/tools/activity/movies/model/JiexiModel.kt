package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JiexiModel(
    @SerialName("code")
    var code: Int,
    @SerialName("data")
    var `data`: Data,
    @SerialName("msg")
    var msg: String,
) {
    @Serializable
    data class Data(
        @SerialName("from")
        var from: String,
        @SerialName("ipv4")
        var ipv4: String,
        @SerialName("time")
        var time: String,
        @SerialName("url")
        var url: String,
    )
}
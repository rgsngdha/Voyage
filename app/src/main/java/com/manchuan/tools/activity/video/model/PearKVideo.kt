package com.manchuan.tools.activity.video.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PearKVideo(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: String = "",
    @SerialName("data")
    var `data`: Data = Data(),
    @SerialName("api_source")
    var apiSource: String = ""
) {
    @Serializable
    data class Data(
        @SerialName("url")
        var url: String = ""
    )
}
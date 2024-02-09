package com.manchuan.tools.activity.video.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShortVideo(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("success")
    var success: Int = 0,
    @SerialName("url")
    var url: String = "",
    @SerialName("type")
    var type: String = "",
    @SerialName("player")
    var player: String = "",
    @SerialName("msg")
    var msg: String = ""
)
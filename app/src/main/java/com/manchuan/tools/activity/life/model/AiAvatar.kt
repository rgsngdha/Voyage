package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiAvatar(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: String = "",
    @SerialName("prompt")
    var prompt: String = "",
    @SerialName("imgurl")
    var imgurl: String = "",
    @SerialName("api_source")
    var apiSource: String = ""
)
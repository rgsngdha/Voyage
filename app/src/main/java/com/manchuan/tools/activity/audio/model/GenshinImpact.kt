package com.manchuan.tools.activity.audio.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenshinImpact(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("text")
    var text: String = "",
    @SerialName("msg")
    var msg: String = "",
    @SerialName("music")
    var music: String = ""
)
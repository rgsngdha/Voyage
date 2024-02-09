package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiPaintModel(
    @SerialName("code")
    var code: Int,
    @SerialName("msg")
    var msg: String,
    @SerialName("url")
    var url: String
)
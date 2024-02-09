package com.manchuan.tools.activity.video.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dog(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: String = "",
    @SerialName("data")
    var `data`: String = ""
)
package com.manchuan.tools.user.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QQBindModel(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: String = "",
    @SerialName("time")
    var time: Int = 0
)
package com.manchuan.tools.user.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterModel(
    @SerialName("code")
    var code: Int,
    @SerialName("msg")
    var msg: String,
    @SerialName("time")
    var time: Int
)
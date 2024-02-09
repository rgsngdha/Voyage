package com.manchuan.tools.user.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoById(
    @SerialName("code") var code: Int = 0,
    @SerialName("msg") var msg: Msg = Msg(),
    @SerialName("time") var time: Int = 0
) {
    @Serializable
    data class Msg(
        @SerialName("id") var id: String = "",
        @SerialName("pic") var pic: String = "",
        @SerialName("name") var name: String = "",
        @SerialName("vip") var vip: String = "",
        @SerialName("fen") var fen: String = "",
        @SerialName("reg_time") var regTime: Long = 0
    )
}
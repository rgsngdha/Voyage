package com.manchuan.tools.user.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoModel(
    @SerialName("code") var code: Int,
    @SerialName("msg") var msg: Msg,
    @SerialName("time") var time: Int,
) {
    @Serializable
    data class Msg(
        @SerialName("email") var email: String?,
        @SerialName("fen") var fen: String,
        @SerialName("id") var id: String,
        @SerialName("inv") var inv: String,
        @SerialName("name") var name: String,
        @SerialName("openid_qq") var openidQq: String? = "",
        @SerialName("openid_wx") var openidWx: String? = "",
        @SerialName("phone") var phone: String? = "",
        @SerialName("pic") var pic: String,
        @SerialName("diary") var diary: String,
        @SerialName("reg_time") var regTime: String,
        @SerialName("user") var user: String? = "",
        @SerialName("vip") var vip: String,
    )
}
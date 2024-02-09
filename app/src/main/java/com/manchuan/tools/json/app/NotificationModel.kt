package com.manchuan.tools.json.app


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class NotificationModel(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: List<Msg> = listOf(),
    @SerialName("time")
    var time: Int = 0
) : Parcelable {
    @Serializable
    @Parcelize
    data class Msg(
        @SerialName("content")
        var content: String = "",
        @SerialName("date")
        var date: String = "",
        @SerialName("name")
        var name: String = ""
    ) : Parcelable
}
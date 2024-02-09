package com.manchuan.tools.activity.game.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Serializable
@Parcelize
data class McServersModel(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("server")
    var server: String = "",
    @SerialName("status")
    var status: String = "",
    @SerialName("ip")
    var ip: String = "",
    @SerialName("port")
    var port: Int = 0,
    @SerialName("players")
    var players: String = "",
    @SerialName("img")
    var img: String = "",
    @SerialName("motd")
    var motd: String = "",
    @SerialName("motd2")
    var motd2: String = ""
) : Parcelable
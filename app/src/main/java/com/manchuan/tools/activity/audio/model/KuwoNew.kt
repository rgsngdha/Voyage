package com.manchuan.tools.activity.audio.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KuwoNew(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("locationid")
    var locationid: String = "",
    @SerialName("data")
    var `data`: Data = Data(),
    @SerialName("msg")
    var msg: String = ""
) {
    @Serializable
    data class Data(
        @SerialName("bitrate")
        var bitrate: Int = 0,
        @SerialName("user")
        var user: String = "",
        @SerialName("sig")
        var sig: String = "",
        @SerialName("type")
        var type: String = "",
        @SerialName("format")
        var format: String = "",
        @SerialName("p2p_audiosourceid")
        var p2pAudiosourceid: String = "",
        @SerialName("rid")
        var rid: Int = 0,
        @SerialName("source")
        var source: String = "",
        @SerialName("url")
        var url: String = ""
    )
}
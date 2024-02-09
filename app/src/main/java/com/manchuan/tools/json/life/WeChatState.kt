package com.manchuan.tools.json.life


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeChatState(
    @SerialName("code")
    var code: Int,
    @SerialName("data")
    var `data`: List<Data>,
    @SerialName("msg")
    var msg: String
) {
    @Serializable
    data class Data(
        @SerialName("album")
        var album: String,
        @SerialName("cover")
        var cover: String,
        @SerialName("singer")
        var singer: String,
        @SerialName("song")
        var song: String
    )
}
package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewImageParagrah(
    @SerialName("code")
    var code: String = "",
    @SerialName("msg")
    var msg: String = "",
    @SerialName("info")
    var info: String = "",
    @SerialName("data")
    var `data`: Data = Data()
) {
    @Serializable
    data class Data(
        @SerialName("url")
        var url: String? = null,
        @SerialName("cover")
        var cover: String = "",
        @SerialName("images")
        var images: List<String> = listOf(),
        @SerialName("pics")
        var pics: List<String> = listOf(),
        @SerialName("title")
        var title: String = "",
        @SerialName("down")
        var down: String? = null,
        @SerialName("mp3")
        var mp3: String? = null,
        @SerialName("bigFile")
        var bigFile: Boolean = false,
        @SerialName("download_image")
        var downloadImage: String = ""
    )
}
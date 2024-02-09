package com.manchuan.tools.activity.json


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageParagraph(
    @SerialName("code")
    var code: Int,
    @SerialName("data")
    var `data`: Data,
    @SerialName("msg")
    var msg: String,
    @SerialName("parsing_type")
    var parsingType: String
) {
    @Serializable
    data class Data(
        @SerialName("images_link")
        var imagesLink: List<String>
    )
}
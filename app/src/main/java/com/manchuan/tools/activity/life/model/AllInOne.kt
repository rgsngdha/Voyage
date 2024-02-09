package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllInOne(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("succ")
    var succ: Boolean = false,
    @SerialName("msg")
    var msg: String? = null,
    @SerialName("data")
    var `data`: Data = Data()
) {
    @Serializable
    data class Data(
        @SerialName("text")
        var text: String = "",
        @SerialName("medias")
        var medias: List<Media> = listOf(),
        @SerialName("overseas")
        var overseas: Int = 0
    ) {
        @Serializable
        data class Media(
            @SerialName("media_type")
            var mediaType: String = "",
            @SerialName("resource_url")
            var resourceUrl: String = "",
            @SerialName("preview_url")
            var previewUrl: String = ""
        )
    }
}
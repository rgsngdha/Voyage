package com.manchuan.tools.model.banner


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BannerModel(
    @SerialName("code")
    var code: String = "",
    @SerialName("data")
    var `data`: List<Data> = listOf()
) {
    @Serializable
    data class Data(
        @SerialName("id")
        var id: String = "",
        @SerialName("title")
        var title: String = "",
        @SerialName("image")
        var image: String = "",
        @SerialName("clickType")
        var clickType: String = "",
        @SerialName("clickUrl")
        var clickUrl: String = ""
    )
}
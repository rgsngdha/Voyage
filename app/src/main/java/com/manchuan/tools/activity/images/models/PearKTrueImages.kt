package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PearKTrueImages(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: String = "",
    @SerialName("count")
    var count: Int = 0,
    @SerialName("images")
    var images: List<String> = listOf(),
    @SerialName("api_source")
    var apiSource: String = ""
)
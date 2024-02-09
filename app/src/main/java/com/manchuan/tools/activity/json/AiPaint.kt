package com.manchuan.tools.activity.json


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiPaint(
    @SerialName("code")
    var code: Int,
    @SerialName("data")
    var `data`: Data
) {
    @Serializable
    data class Data(
        @SerialName("artist")
        var artist: String,
        @SerialName("image")
        var image: String,
        @SerialName("progress")
        var progress: Int,
        @SerialName("status")
        var status: Int,
        @SerialName("style")
        var style: String,
        @SerialName("tips")
        var tips: String
    )
}
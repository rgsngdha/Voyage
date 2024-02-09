package com.manchuan.tools.activity.json


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChinaMobile(
    @SerialName("code")
    var code: Int,
    @SerialName("data")
    var `data`: Data,
    @SerialName("msg")
    var msg: String
) {
    @Serializable
    data class Data(
        @SerialName("download_link")
        var downloadLink: String,
        @SerialName("file_author")
        var fileAuthor: String,
        @SerialName("file_name")
        var fileName: String,
        @SerialName("file_size")
        var fileSize: String,
        @SerialName("file_time")
        var fileTime: String,
        @SerialName("file_views")
        var fileViews: String
    )
}
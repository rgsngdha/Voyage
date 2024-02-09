package com.manchuan.tools.json.app


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateModel(
    @SerialName("message")
    var message: String,
    @SerialName("must")
    var must: Int,
    @SerialName("time")
    var time: Int,
    @SerialName("title")
    var title: String,
    @SerialName("url")
    var url: String,
    @SerialName("versionCode")
    var versionCode: Int,
    @SerialName("ycTemplate")
    var ycTemplate: Int
)
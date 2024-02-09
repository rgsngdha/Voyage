package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DefaultSourcesModel(
    @SerialName("code")
    var code: String,
    @SerialName("data")
    var `data`: List<Data>,
    @SerialName("name")
    var name: String,
    @SerialName("version")
    var version: Int,
) {
    @Serializable
    data class Data(
        @SerialName("id")
        var id: Int,
        @SerialName("internal")
        var `internal`: Int,
        @SerialName("name")
        var name: String,
        @SerialName("playUrl")
        var playUrl: String,
        @SerialName("playUa")
        var playUa: String,
        @SerialName("searchUa")
        var searchUa: String,
        @SerialName("searchUrl")
        var searchUrl: String,
        @SerialName("sourceSwitch")
        var sourceSwitch: Int,
    )
}
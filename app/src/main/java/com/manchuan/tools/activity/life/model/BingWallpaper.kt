package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BingWallpaper(
    @SerialName("code") var code: Int = 0, @SerialName("data") var `data`: Data = Data()
) {
    @Serializable
    data class Data(
        @SerialName("current") var current: Int = 0,
        @SerialName("next") var next: Int = 0,
        @SerialName("size") var size: Int = 0,
        @SerialName("pages") var pages: Int = 0,
        @SerialName("total") var total: Int = 0,
        @SerialName("list") var list: List<WallPaper> = listOf()
    ) {
        @Serializable
        data class WallPaper(
            @SerialName("startdate") var startdate: String = "",
            @SerialName("fullstartdate") var fullstartdate: String = "",
            @SerialName("enddate") var enddate: String = "",
            @SerialName("url") var url: String = "",
            @SerialName("urlbase") var urlbase: String = "",
            @SerialName("copyright") var copyright: String = "",
            @SerialName("copyrightlink") var copyrightlink: String = "",
            @SerialName("title") var title: String = "",
            @SerialName("quiz") var quiz: String = "",
            @SerialName("wp") var wp: Boolean = false,
            @SerialName("hsh") var hsh: String = "",
            @SerialName("drk") var drk: Int = 0,
            @SerialName("top") var top: Int = 0,
            @SerialName("bot") var bot: Int = 0,
            @SerialName("hs") var hs: List<Int> = listOf()
        )
    }
}
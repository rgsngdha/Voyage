package com.manchuan.tools.activity.movies.fragments.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountWallpaper(
    @SerialName("images") var images: List<Image>,
    @SerialName("tooltips") var tooltips: Tooltips,
) {
    @Serializable
    data class Image(
        @SerialName("bot") var bot: Int,
        @SerialName("copyright") var copyright: String,
        @SerialName("copyrightlink") var copyrightlink: String,
        @SerialName("drk") var drk: Int,
        @SerialName("enddate") var enddate: String,
        @SerialName("fullstartdate") var fullstartdate: String,
        @SerialName("hs") var hs: List<String>,
        @SerialName("hsh") var hsh: String,
        @SerialName("quiz") var quiz: String,
        @SerialName("startdate") var startdate: String,
        @SerialName("title") var title: String,
        @SerialName("top") var top: Int,
        @SerialName("url") var url: String,
        @SerialName("urlbase") var urlbase: String,
        @SerialName("wp") var wp: Boolean,
    )

    @Serializable
    data class Tooltips(
        @SerialName("loading") var loading: String,
        @SerialName("next") var next: String,
        @SerialName("previous") var previous: String,
        @SerialName("walle") var walle: String,
        @SerialName("walls") var walls: String,
    )
}
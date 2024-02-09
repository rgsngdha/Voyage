package com.manchuan.tools.json.news


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalNews(
    @SerialName("code") var code: Int,
    @SerialName("data") var `data`: List<Data>,
    @SerialName("msg") var msg: String,
) {
    @Serializable
    data class Data(
        @SerialName("commentCount") var commentCount: Int?,
        @SerialName("digest") var digest: String?,
        @SerialName("docid") var docid: String?,
        @SerialName("hasImg") var hasImg: Int?,
        @SerialName("imgsrc") var imgsrc: String?,
        @SerialName("imgsrc3gtype") var imgsrc3gtype: String?,
        @SerialName("modelmode") var modelmode: String? = "",
        @SerialName("priority") var priority: Int?,
        @SerialName("ptime") var ptime: String?,
        @SerialName("skipType") var skipType: String? = "",
        @SerialName("skipURL") var skipURL: String? = "",
        @SerialName("source") var source: String?,
        @SerialName("stitle") var stitle: String?,
        @SerialName("title") var title: String?,
        @SerialName("url") var url: String?,
    )
}
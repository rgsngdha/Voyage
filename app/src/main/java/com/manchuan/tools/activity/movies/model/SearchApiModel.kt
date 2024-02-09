package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchApiModel(
    @SerialName("code") var code: Int,
    @SerialName("limit") var limit: Int,
    @SerialName("list") var list: List<SearchResult>,
    @SerialName("msg") var msg: String,
    @SerialName("page") var page: Int,
    @SerialName("pagecount") var pagecount: Int,
    @SerialName("total") var total: Int,
    @SerialName("url") var url: String,
) {
    @Serializable
    data class SearchResult(
        @SerialName("en") var en: String,
        @SerialName("id") var id: Int,
        @SerialName("name") var name: String,
        @SerialName("pic") var pic: String,
    )
}
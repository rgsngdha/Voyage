package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HotQuery(
    @SerialName("data")
    var `data`: List<Data>,
    @SerialName("is_empty")
    var isEmpty: Boolean,
) {
    @Serializable
    data class Data(
        @SerialName("ab_percent")
        var abPercent: Int,
        @SerialName("actions")
        var actions: String,
        @SerialName("bucket")
        var bucket: String,
        @SerialName("click_count")
        var clickCount: Int,
        @SerialName("data")
        var `data`: String,
        @SerialName("impression_count")
        var impressionCount: Int,
        @SerialName("order")
        var order: Int,
        @SerialName("query")
        var query: String,
        @SerialName("query_source_type")
        var querySourceType: Int,
        @SerialName("search_trend")
        var searchTrend: Int,
    )
}
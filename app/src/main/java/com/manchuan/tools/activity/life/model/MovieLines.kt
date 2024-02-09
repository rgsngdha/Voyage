package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieLines(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: String = "",
    @SerialName("word")
    var word: String = "",
    @SerialName("count")
    var count: String = "",
    @SerialName("now_page")
    var nowPage: String = "",
    @SerialName("last_page")
    var lastPage: String = "",
    @SerialName("data")
    var `data`: List<Data> = listOf(),
    @SerialName("api_source")
    var apiSource: String = ""
) {
    @Serializable
    data class Data(
        @SerialName("local_img")
        var localImg: String = "",
        @SerialName("update_time")
        var updateTime: String = "",
        @SerialName("title")
        var title: String = "",
        @SerialName("area")
        var area: String = "",
        @SerialName("tags")
        var tags: String = "",
        @SerialName("directors")
        var directors: String = "",
        @SerialName("actors")
        var actors: String = "",
        @SerialName("zh_word")
        var zhWord: String = "",
        @SerialName("all_zh_word")
        var allZhWord: List<String> = listOf(),
        @SerialName("en_word")
        var enWord: String = "",
        @SerialName("all_en_word")
        var allEnWord: List<String> = listOf()
    )
}
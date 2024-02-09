package com.manchuan.tools.activity.video.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TraceMoe(
    @SerialName("frameCount")
    var frameCount: Int = 0,
    @SerialName("error")
    var error: String = "",
    @SerialName("result")
    var result: List<Result> = listOf()
) {
    @Serializable
    data class Result(
        @SerialName("anilist")
        var anilist: Anilist = Anilist(),
        @SerialName("filename")
        var filename: String = "",
        @SerialName("episode")
        var episode: Int = 0,
        @SerialName("from")
        var from: Double = 0.0,
        @SerialName("to")
        var to: Double = 0.0,
        @SerialName("similarity")
        var similarity: Double = 0.0,
        @SerialName("video")
        var video: String = "",
        @SerialName("image")
        var image: String = ""
    ) {
        @Serializable
        data class Anilist(
            @SerialName("id")
            var id: Int = 0,
            @SerialName("idMal")
            var idMal: Int = 0,
            @SerialName("title")
            var title: Title = Title(),
            @SerialName("synonyms")
            var synonyms: List<String> = listOf(),
            @SerialName("isAdult")
            var isAdult: Boolean = false
        ) {
            @Serializable
            data class Title(
                @SerialName("native")
                var native: String = "",
                @SerialName("romaji")
                var romaji: String = "",
                @SerialName("english")
                var english: String = ""
            )
        }
    }
}
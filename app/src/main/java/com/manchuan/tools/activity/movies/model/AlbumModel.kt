package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumModel(
    @SerialName("code") var code: Int,
    @SerialName("data") var `data`: Data,
    @SerialName("message") var message: String,
) {
    @Serializable
    data class Data(
        @SerialName("albumId") var albumId: Int,
        @SerialName("albumTitle") var albumTitle: String,
        @SerialName("banner") var banner: String?,
        @SerialName("flow") var flow: String?,
        @SerialName("videoCount") var videoCount: Int,
        @SerialName("videoList") var videoList: List<Video>,
    ) {
        @Serializable
        data class Video(
            @SerialName("imgurl") var imgurl: String,
            @SerialName("title") var title: String,
            @SerialName("videoId") var videoId: Int,
            @SerialName("xx") var xx: String,
        )
    }
}
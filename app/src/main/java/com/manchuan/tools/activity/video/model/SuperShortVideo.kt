package com.manchuan.tools.activity.video.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuperShortVideo(
    @SerialName("accessToken") var accessToken: String = "",
    @SerialName("code") var code: String = "",
    @SerialName("msg") var msg: String = "",
    @SerialName("content") var content: Content = Content(),
    @SerialName("timestamp") var timestamp: Long = 0,
) {
    @Serializable
    data class Content(
        @SerialName("author") var author: String = "",
        @SerialName("avatar") var avatar: String = "",
        @SerialName("cover") var cover: String = "",
        @SerialName("coverList") var coverList: List<String> = listOf(),
        @SerialName("headUrl") var headUrl: String = "",
        @SerialName("imageList") var imageList: List<String> = listOf(),
        @SerialName("likeNum") var likeNum: Int = 0,
        @SerialName("msg") var msg: String = "",
        @SerialName("originText") var originText: String = "",
        @SerialName("shortUrl") var shortUrl: String = "",
        @SerialName("shuiYinPlatform") var shuiYinPlatform: String = "",
        @SerialName("success") var success: Boolean = false,
        @SerialName("title") var title: String = "",
        @SerialName("type") var type: String = "",
        @SerialName("url") var url: String = "",
        @SerialName("videoList") var videoList: List<String> = listOf(),
    )
}
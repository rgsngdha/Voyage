package com.manchuan.tools.activity.site.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sites(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: List<Msg> = listOf(),
    @SerialName("time")
    var time: Int = 0
) {
    @Serializable
    data class Msg(
        @SerialName("id")
        var id: String = "",
        @SerialName("name")
        var name: String = "",
        @SerialName("image")
        var image: String? = "",
        @SerialName("description")
        var description: String = "",
        @SerialName("url")
        var url: String = "",
        @SerialName("author")
        var author: String = "",
        @SerialName("author_email")
        var authorEmail: String = "",
        @SerialName("author_id")
        var authorId: String = "",
        @SerialName("avatar")
        var avatar: String = ""
    )
}
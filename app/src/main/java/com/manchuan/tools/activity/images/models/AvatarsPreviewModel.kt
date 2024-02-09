package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarsPreviewModel(
    @SerialName("code")
    var code: Int,
    @SerialName("msg")
    var msg: String,
    @SerialName("res")
    var res: Res
) {
    @Serializable
    data class Res(
        @SerialName("avatar")
        var avatar: List<Avatar>
    ) {
        @Serializable
        data class Avatar(
            @SerialName("atime")
            var atime: Double,
            @SerialName("cr")
            var cr: List<String>?,
            @SerialName("desc")
            var desc: String,
            @SerialName("favs")
            var favs: Int,
            @SerialName("id")
            var id: String,
            @SerialName("name")
            var name: String,
            @SerialName("ncos")
            var ncos: Int,
            @SerialName("rank")
            var rank: Int,
            @SerialName("tag")
            var tag: List<String>,
            @SerialName("thumb")
            var thumb: String,
            @SerialName("url")
            var url: List<String>?,
            @SerialName("user")
            var user: User?,
            @SerialName("view")
            var view: Int?,
            @SerialName("vip")
            var vip: Boolean
        ) {
            @Serializable
            data class User(
                @SerialName("auth")
                var auth: String,
                @SerialName("avatar")
                var avatar: String,
                @SerialName("follower")
                var follower: Int,
                @SerialName("id")
                var id: String,
                @SerialName("isvip")
                var isvip: Boolean,
                @SerialName("name")
                var name: String,
                @SerialName("viptime")
                var viptime: Int
            )
        }
    }
}
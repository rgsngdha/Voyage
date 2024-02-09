package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HorizontalWall(
    @SerialName("msg")
    var msg: String = "",
    @SerialName("res")
    var res: Res = Res(),
    @SerialName("code")
    var code: Int = 0
) {
    @Serializable
    data class Res(
        @SerialName("wallpaper")
        var wallpaper: List<Wallpaper> = listOf()
    ) {
        @Serializable
        data class Wallpaper(
            @SerialName("desc")
            var desc: String = "",
            @SerialName("ncos")
            var ncos: Int = 0,
            @SerialName("thumb")
            var thumb: String = "",
            @SerialName("img")
            var img: String = "",
            @SerialName("cid")
            var cid: List<String> = listOf(),
            @SerialName("url")
            var url: List<String> = listOf(),
            @SerialName("atime")
            var atime: Double = 0.0,
            @SerialName("views")
            var views: Int = 0,
            @SerialName("ivip")
            var ivip: Boolean = false,
            @SerialName("rule")
            var rule: String = "",
            @SerialName("tag")
            var tag: List<String> = listOf(),
            @SerialName("rank")
            var rank: Int = 0,
            @SerialName("wp")
            var wp: String = "",
            @SerialName("xr")
            var xr: Boolean = false,
            @SerialName("rule_new")
            var ruleNew: String = "",
            @SerialName("favs")
            var favs: Int = 0,
            @SerialName("preview")
            var preview: String = "",
            @SerialName("cr")
            var cr: Boolean = false,
            @SerialName("id")
            var id: String = "",
            @SerialName("store")
            var store: String = "",
            @SerialName("user")
            var user: User = User()
        ) {
            @Serializable
            data class User(
                @SerialName("name")
                var name: String = "",
                @SerialName("viptime")
                var viptime: Double = 0.0,
                @SerialName("auth")
                var auth: String = "",
                @SerialName("follower")
                var follower: Int = 0,
                @SerialName("avatar")
                var avatar: String = "",
                @SerialName("isvip")
                var isvip: Boolean = false,
                @SerialName("id")
                var id: String = ""
            )
        }
    }
}
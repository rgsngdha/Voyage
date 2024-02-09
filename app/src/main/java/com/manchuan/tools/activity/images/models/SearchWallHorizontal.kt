package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchWallHorizontal(
    @SerialName("msg") var msg: String = "",
    @SerialName("res") var res: Res = Res(),
    @SerialName("code") var code: Int = 0
) {
    @Serializable
    data class Res(
        @SerialName("album") var album: List<Album> = listOf(),
        @SerialName("total") var total: Int = 0,
        @SerialName("type") var type: Int = 0,
        @SerialName("wallpaper") var wallpaper: List<Wallpaper> = listOf(),
        @SerialName("title") var title: String = ""
    ) {
        @Serializable
        data class Album(
            @SerialName("status") var status: String = "",
            @SerialName("ename") var ename: String = "",
            @SerialName("hide") var hide: Boolean = false,
            @SerialName("uid") var uid: String = "",
            @SerialName("oid") var oid: String = "",
            @SerialName("cover") var cover: String = "",
            @SerialName("count") var count: Int = 0,
            @SerialName("name") var name: String = "",
            @SerialName("tag") var tag: List<String> = listOf(),
            @SerialName("user") var user: User = User(),
            @SerialName("recommend") var recommend: Boolean = false,
            @SerialName("lcover") var lcover: String = "",
            @SerialName("favs") var favs: Int = 0,
            @SerialName("atime") var atime: String = "",
            @SerialName("type") var type: Int = 0,
            @SerialName("id") var id: String = "",
            @SerialName("desc") var desc: String = "",
            @SerialName("nickname") var nickname: String = "",
            @SerialName("email") var email: String = "",
            @SerialName("top") var top: Int = 0,
            @SerialName("subname") var subname: String = ""
        ) {
            @Serializable
            data class User(
                @SerialName("avatar") var avatar: String = "",
                @SerialName("id") var id: String = "",
                @SerialName("auth") var auth: String = "",
                @SerialName("name") var name: String = ""
            )
        }

        @Serializable
        data class Wallpaper(
            @SerialName("uid") var uid: String = "",
            @SerialName("cfg") var cfg: Int = 0,
            @SerialName("ncos") var ncos: Int = 0,
            @SerialName("rank") var rank: Int = 0,
            @SerialName("tag") var tag: List<String?> = listOf(),
            @SerialName("vfobjs") var vfobjs: Vfobjs = Vfobjs(),
            @SerialName("xr") var xr: Boolean = false,
            @SerialName("cr") var cr: Boolean = false,
            @SerialName("id") var id: String = "",
            @SerialName("is_ms") var isMs: Boolean = false,
            @SerialName("ratio") var ratio: Ratio = Ratio(),
            @SerialName("thumb") var thumb: String = "",
            @SerialName("img") var img: String = "",
            @SerialName("fobjs") var fobjs: Fobjs = Fobjs(),
            @SerialName("fsize") var fsize: Fsize = Fsize(),
            @SerialName("recommend") var recommend: Boolean = false,
            @SerialName("rec") var rec: Boolean = false,
            @SerialName("preview") var preview: String = "",
            @SerialName("store") var store: String = "",
            @SerialName("sid") var sid: List<String> = listOf(),
            @SerialName("form") var form: Form = Form(),
            @SerialName("oid") var oid: String = "",
            @SerialName("user") var user: User = User(),
            @SerialName("wp") var wp: String = "",
            @SerialName("favs") var favs: Int = 0,
            @SerialName("atime") var atime: Double = 0.0,
            @SerialName("desc") var desc: String = "",
            @SerialName("cdown") var cdown: Boolean = false,
            @SerialName("cid") var cid: List<String?> = listOf(),
            @SerialName("url") var url: List<String?> = listOf(),
            @SerialName("cov") var cov: Boolean = false,
            @SerialName("rule") var rule: String = "",
            @SerialName("dirid") var dirid: List<String?> = listOf(),
            @SerialName("qiniuobjs") var qiniuobjs: Qiniuobjs = Qiniuobjs(),
            @SerialName("ceco") var ceco: List<String?> = listOf(),
            @SerialName("view") var view: Int = 0
        ) {
            @Serializable
            class Vfobjs

            @Serializable
            data class Ratio(
                @SerialName("y") var y: Int = 0, @SerialName("x") var x: Int = 0
            )

            @Serializable
            data class Fobjs(
                @SerialName("origin") var origin: String = ""
            )

            @Serializable
            data class Fsize(
                @SerialName("preview") var preview: Preview = Preview(),
                @SerialName("img") var img: Img = Img(),
                @SerialName("wp") var wp: Wp = Wp()
            ) {
                @Serializable
                data class Preview(
                    @SerialName("h") var h: Int = 0, @SerialName("w") var w: Int = 0
                )

                @Serializable
                data class Img(
                    @SerialName("h") var h: Int = 0, @SerialName("w") var w: Int = 0
                )

                @Serializable
                data class Wp(
                    @SerialName("h") var h: Int = 0, @SerialName("w") var w: Int = 0
                )
            }

            @Serializable
            data class Form(
                @SerialName("name") var name: String = "",
                @SerialName("target") var target: String = ""
            )

            @Serializable
            data class User(
                @SerialName("avatar") var avatar: String = "",
                @SerialName("id") var id: String = "",
                @SerialName("auth") var auth: String = "",
                @SerialName("name") var name: String = ""
            )

            @Serializable
            class Qiniuobjs
        }
    }
}
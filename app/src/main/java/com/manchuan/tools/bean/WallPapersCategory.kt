package com.manchuan.tools.bean


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WallPapersCategory(
    @SerialName("code")
    var code: Int,
    @SerialName("msg")
    var msg: String,
    @SerialName("res")
    var res: Res,
) {
    @Serializable
    data class Res(
        @SerialName("category")
        var category: List<Category>,
    ) {
        @Serializable
        data class Category(
            @SerialName("atime")
            var atime: Double? = 0.0,
            @SerialName("count")
            var count: Int,
            @SerialName("cover")
            var cover: String,
            @SerialName("cover_temp")
            var coverTemp: String?,
            @SerialName("ename")
            var ename: String,
            @SerialName("filter")
            var filter: List<String>?,
            @SerialName("icover")
            var icover: String?,
            @SerialName("id")
            var id: String,
            @SerialName("name")
            var name: String,
            @SerialName("picasso_cover")
            var picassoCover: String,
            @SerialName("rank")
            var rank: Int,
            @SerialName("rname")
            var rname: String,
            @SerialName("sn")
            var sn: Int,
            @SerialName("type")
            var type: Int,
        )
    }
}
package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AvatarsModel(
    @SerialName("code")
    var code: Int,
    @SerialName("msg")
    var msg: String,
    @SerialName("res")
    var res: Res
) {
    @Serializable
    data class Res(
        @SerialName("category")
        var category: List<Category>
    ) {
        @Serializable
        data class Category(
            @SerialName("_id")
            var id: String,
            @SerialName("img")
            var img: String,
            @SerialName("name")
            var name: String
        )
    }
}
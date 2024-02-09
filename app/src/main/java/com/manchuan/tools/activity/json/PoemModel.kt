package com.manchuan.tools.activity.json


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PoemModel(
    @SerialName("code") var code: Int,
    @SerialName("list") var list: List<PoemList>,
) {
    @Serializable
    data class PoemList(
        @SerialName("author") var author: String,
        @SerialName("chaodai") var chaodai: String,
        @SerialName("cont") var cont: String,
        @SerialName("title") var title: String,
    )
}
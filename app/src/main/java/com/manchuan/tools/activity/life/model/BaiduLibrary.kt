package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaiduLibrary(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: String = "",
    @SerialName("data")
    var `data`: Data = Data(),
    @SerialName("api_source")
    var apiSource: String = ""
) {
    @Serializable
    data class Data(
        @SerialName("question")
        var question: String = "",
        @SerialName("options")
        var options: List<String> = listOf(),
        @SerialName("answer")
        var answer: String? = ""
    )
}
package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StepModel(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("msg")
    var msg: String = "",
    @SerialName("type")
    var type: String = "",
    @SerialName("data")
    var `data`: Data = Data(),
    @SerialName("api_source")
    var apiSource: String = ""
) {
    @Serializable
    data class Data(
        @SerialName("state")
        var state: String = ""
    )
}
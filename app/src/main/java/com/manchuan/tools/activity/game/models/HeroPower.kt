package com.manchuan.tools.activity.game.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeroPower(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("data")
    var `data`: Data = Data(),
    @SerialName("msg")
    var msg: String = "",
    @SerialName("docs")
    var docs: String = ""
) {
    @Serializable
    data class Data(
        @SerialName("name")
        var name: String = "",
        @SerialName("alias")
        var alias: String = "",
        @SerialName("area")
        var area: String = "",
        @SerialName("areaPower")
        var areaPower: String = "",
        @SerialName("city")
        var city: String = "",
        @SerialName("cityPower")
        var cityPower: String = "",
        @SerialName("province")
        var province: String = "",
        @SerialName("provincePower")
        var provincePower: String = "",
        @SerialName("platform")
        var platform: String = "",
        @SerialName("stamp")
        var stamp: Int = 0,
        @SerialName("updatetime")
        var updatetime: String = ""
    )
}
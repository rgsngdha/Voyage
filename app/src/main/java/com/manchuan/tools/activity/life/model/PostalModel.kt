package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostalModel(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("data")
    var `data`: Data = Data()
) {
    @Serializable
    data class Data(
        @SerialName("ret_code")
        var retCode: Int = 0,
        @SerialName("code")
        var code: String = "",
        @SerialName("msg")
        var msg: String = "",
        @SerialName("contentlist")
        var contentlist: List<Contentlist> = listOf(),
        @SerialName("maxResult")
        var maxResult: Int = 0,
        @SerialName("allNum")
        var allNum: Int = 0,
        @SerialName("allPages")
        var allPages: Int = 0,
        @SerialName("currentPage")
        var currentPage: Int = 0
    ) {
        @Serializable
        data class Contentlist(
            @SerialName("area")
            var area: String = "",
            @SerialName("county")
            var county: String = "",
            @SerialName("city")
            var city: String = "",
            @SerialName("province")
            var province: String = "",
            @SerialName("code")
            var code: String = "",
            @SerialName("areacode")
            var areacode: String = ""
        )
    }
}
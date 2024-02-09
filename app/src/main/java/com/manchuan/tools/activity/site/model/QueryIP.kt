package com.manchuan.tools.activity.site.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QueryIP(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("data")
    var `data`: Data = Data()
) {
    @Serializable
    data class Data(
        @SerialName("showapi_res_code")
        var showapiResCode: Int = 0,
        @SerialName("showapi_res_body")
        var showapiResBody: ShowapiResBody = ShowapiResBody(),
        @SerialName("showapi_res_error")
        var showapiResError: String = ""
    ) {
        @Serializable
        data class ShowapiResBody(
            @SerialName("remark")
            var remark: String = "",
            @SerialName("isp")
            var isp: String = "",
            @SerialName("ip")
            var ip: String = "",
            @SerialName("region")
            var region: String = "",
            @SerialName("lnt")
            var lnt: String = "",
            @SerialName("county")
            var county: String = "",
            @SerialName("en_name_short")
            var enNameShort: String = "",
            @SerialName("lat")
            var lat: String = "",
            @SerialName("city")
            var city: String = "",
            @SerialName("city_code")
            var cityCode: String = "",
            @SerialName("country")
            var country: String = "",
            @SerialName("continents")
            var continents: String = "",
            @SerialName("en_name")
            var enName: String = "",
            @SerialName("ret_code")
            var retCode: Int = 0
        )
    }
}
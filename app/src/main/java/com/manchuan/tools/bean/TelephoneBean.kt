package com.manchuan.tools.bean


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Serializable
@Parcelize
data class TelephoneBean(
    @SerialName("code")
    var code: Int = 0,
    @SerialName("phone")
    var phone: String = "",
    @SerialName("msg")
    var msg: String = "",
    @SerialName("data")
    var `data`: Data = Data()
) : Parcelable {
    @Serializable
    @Parcelize
    data class Data(
        @SerialName("id")
        var id: String = "",
        @SerialName("prefix")
        var prefix: String = "",
        @SerialName("provice_simple")
        var proviceSimple: String = "",
        @SerialName("city_county_simple")
        var cityCountySimple: String = "",
        @SerialName("isp")
        var isp: String = "",
        @SerialName("zone_description")
        var zoneDescription: String = "",
        @SerialName("zip_code")
        var zipCode: String = "",
        @SerialName("area_code")
        var areaCode: String = ""
    ) : Parcelable
}
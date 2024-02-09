package com.manchuan.tools.activity.json


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DomainFiling(
    @SerialName("code") var code: Int,
    @SerialName("data") var `data`: Data,
    @SerialName("msg") var msg: String,
) {
    @Serializable
    data class Data(
        @SerialName("icp") var icp: String,
        @SerialName("limitAccess") var limitAccess: String,
        @SerialName("name") var name: String,
        @SerialName("nature") var nature: String,
        @SerialName("siteindex") var siteindex: String,
        @SerialName("sitename") var sitename: String? = "",
        @SerialName("time") var time: String,
    )
}
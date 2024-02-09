package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocialQuery(
    @SerialName("ERR")
    var eRR: String = "",
    @SerialName("时间")
    var 时间: String = "",
    @SerialName("AAZ500")
    var aAZ500: String = "",
    @SerialName("AAC002")
    var aAC002: String = "",
    @SerialName("AAC003")
    var aAC003: String = "",
    @SerialName("CARDTYPE")
    var cARDTYPE: String = "",
    @SerialName("TRANSACTTYPE")
    var tRANSACTTYPE: String = "",
    @SerialName("BATCHNO")
    var bATCHNO: String = "",
    @SerialName("AAB301")
    var aAB301: String = "",
    @SerialName("ORGANID")
    var oRGANID: String = "",
    @SerialName("AAE008")
    var aAE008: String = "",
    @SerialName("KS")
    var kS: String = "",
    @SerialName("APPLYTIME")
    var aPPLYTIME: String = "",
    @SerialName("BANKTIME0")
    var bANKTIME0: String = "",
    @SerialName("BANKFINISHTIME0")
    var bANKFINISHTIME0: String = "",
    @SerialName("INSURETIME")
    var iNSURETIME: String = "",
    @SerialName("INSUREFINISHTIME0")
    var iNSUREFINISHTIME0: String = "",
    @SerialName("INSUREFINISHTIME")
    var iNSUREFINISHTIME: String = "",
    @SerialName("PROVINCETIME")
    var pROVINCETIME: String = "",
    @SerialName("CITYTIME")
    var cITYTIME: String = "",
    @SerialName("GETTIME")
    var gETTIME: String = "",
    @SerialName("GETTIME1")
    var gETTIME1: List<String> = listOf(),
    @SerialName("REMARKS")
    var rEMARKS: String = "",
    @SerialName("VALIDTAG")
    var vALIDTAG: String = ""
)
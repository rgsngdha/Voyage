package com.manchuan.tools.activity.vivo.json


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VivoTheme(
    @SerialName("category") var category: Int,
    @SerialName("hasNext") var hasNext: Int,
    @SerialName("msg") var msg: String,
    @SerialName("resList") var resList: List<Res>,
    @SerialName("stat") var stat: Int,
) {
    @Serializable
    data class Res(
        @SerialName("category") var category: Int,
        @SerialName("comprehession") var comprehession: Double,
        @SerialName("description") var description: String? = null,
        @SerialName("edition") var edition: Int,
        @SerialName("fileSize") var fileSize: Int,
        @SerialName("landscapeThumbPath") var landscapeThumbPath: String? = null,
        @SerialName("name") var name: String,
        @SerialName("packageId") var packageId: String,
        @SerialName("pointDeduct") var pointDeduct: Int,
        @SerialName("prePrice") var prePrice: Int,
        @SerialName("previewUris") var previewUris: String? = null,
        @SerialName("price") var price: Int,
        @SerialName("priceEndTime") var priceEndTime: Int? = null,
        @SerialName("resAuthor") var resAuthor: String,
        @SerialName("resId") var resId: String,
        @SerialName("resPrice") var resPrice: Int,
        @SerialName("resPriceType") var resPriceType: Int,
        @SerialName("resourcediversionflag") var resourcediversionflag: Int,
        @SerialName("s_score") var sScore: String? = null,
        @SerialName("s_type") var sType: String? = null,
        @SerialName("score") var score: Double,
        @SerialName("style") var style: String,
        @SerialName("thumbPath") var thumbPath: String,
        @SerialName("traceInfo") var traceInfo: String,
        @SerialName("unfoldInfoVO") var unfoldInfoVO: String? = null,
        @SerialName("vipFreeUse") var vipFreeUse: String? = null,
    )
}
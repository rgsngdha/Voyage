package com.manchuan.tools.activity.life.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchSoftware(
    @SerialName("msg") var msg: String = "",
    @SerialName("gameapps") var gameapps: List<Gameapp> = listOf(),
    @SerialName("categorytitle") var categorytitle: String = "",
    @SerialName("recommends") var recommends: List<String> = listOf(),
    @SerialName("more") var more: Int = 0,
    @SerialName("start") var start: String = "",
    @SerialName("status") var status: Int = 0,
) {
    @Serializable
    data class Gameapp(
        @SerialName("appEname") var appEname: String = "",
        @SerialName("packageType") var packageType: Int = 0,
        @SerialName("buttonColor") var buttonColor: String = "",
        @SerialName("appTypes") var appTypes: List<AppType> = listOf(),
        @SerialName("isOnlineOrAdvert") var isOnlineOrAdvert: Int = 0,
        @SerialName("appPrivacyPolicy") var appPrivacyPolicy: String = "",
        @SerialName("policyType") var policyType: Int = 0,
        @SerialName("appPermissions") var appPermissions: List<AppPermission> = listOf(),
        @SerialName("isTeenagers") var isTeenagers: Int = 0,
        @SerialName("hasCoupon") var hasCoupon: Boolean = false,
        @SerialName("totalDecrease") var totalDecrease: Int = 0,
        @SerialName("giftCount") var giftCount: Int = 0,
        @SerialName("pageUrl") var pageUrl: String = "",
        @SerialName("gameType") var gameType: Int = 0,
        @SerialName("cdnUrls3") var cdnUrls3: String = "",
        @SerialName("apptitle") var apptitle: String = "",
        @SerialName("gameShell") var gameShell: String = "",
        @SerialName("appdesc") var appdesc: String = "",
        @SerialName("appcrackdesc") var appcrackdesc: String = "",
        @SerialName("appsize") var appsize: String = "",
        @SerialName("applogo") var applogo: String = "",
        @SerialName("apptags") var apptags: String = "",
        @SerialName("applanguage") var applanguage: String = "",
        @SerialName("appversion") var appversion: String = "",
        @SerialName("categoryname") var categoryname: String = "",
        @SerialName("categoryalias") var categoryalias: String = "",
        @SerialName("categoryColor") var categoryColor: String = "",
        @SerialName("appauthorization") var appauthorization: String = "",
        @SerialName("apptype") var apptype: String = "",
        @SerialName("packname") var packname: String = "",
        @SerialName("appcrc") var appcrc: String = "",
        @SerialName("isGift") var isGift: Int = 0,
        @SerialName("shareurl") var shareurl: String = "",
        @SerialName("system") var system: String = "",
        @SerialName("imageresource") var imageresource: String = "",
        @SerialName("clouddownlist") var clouddownlist: List<String> = listOf(),
        @SerialName("onlineurl") var onlineurl: String = "",
        @SerialName("viewCustomized") var viewCustomized: Int = 0,
        @SerialName("localurl") var localurl: Localurl = Localurl(),
        @SerialName("shortdesc") var shortdesc: String = "",
        @SerialName("filename") var filename: String = "",
        @SerialName("onlineurllist") var onlineurllist: List<String> = listOf(),
        @SerialName("isTVSeries") var isTVSeries: Int = 0,
        @SerialName("fontColor1st") var fontColor1st: String = "",
        @SerialName("fontColor2nd") var fontColor2nd: String = "",
        @SerialName("separatorColor") var separatorColor: String = "",
        @SerialName("backgroundColorQuote") var backgroundColorQuote: String = "",
        @SerialName("dataDownUrl") var dataDownUrl: String = "",
        @SerialName("imageResourceDirection") var imageResourceDirection: Int = 0,
        @SerialName("imageParams") var imageParams: String = "",
        @SerialName("releaseNotes") var releaseNotes: String = "",
        @SerialName("localUrls") var localUrls: List<String> = listOf(),
        @SerialName("cdnUrls") var cdnUrls: List<String> = listOf(),
        @SerialName("cdnUrls2") var cdnUrls2: List<String> = listOf(),
        @SerialName("cmsEnabled") var cmsEnabled: Int = 0,
        @SerialName("isLocationBased") var isLocationBased: Int = 0,
        @SerialName("openMode") var openMode: Int = 0,
        @SerialName("incompatibleAndroid") var incompatibleAndroid: String = "",
        @SerialName("checksumEnabled") var checksumEnabled: Int = 0,
        @SerialName("orderTitle") var orderTitle: String = "",
        @SerialName("orderType") var orderType: Int = 0,
        @SerialName("ranking") var ranking: Int = 0,
        @SerialName("appBook") var appBook: String = "",
        @SerialName("isWap") var isWap: Int = 0,
        @SerialName("backgroundColor") var backgroundColor: String = "",
        @SerialName("username") var username: String = "",
        @SerialName("createTime") var createTime: Int = 0,
        @SerialName("updateTime") var updateTime: Int = 0,
        @SerialName("md5") var md5: String = "",
        @SerialName("coverImage") var coverImage: String = "",
        @SerialName("share") var share: Boolean = false,
        @SerialName("tooldown") var tooldown: Int = 0,
        @SerialName("versionCode") var versionCode: Int = 0,
        @SerialName("storagePath") var storagePath: Int = 0,
        @SerialName("businessType") var businessType: Int = 0,
        @SerialName("pageSize") var pageSize: Int = 0,
        @SerialName("cloudStatus") var cloudStatus: Int = 0,
        @SerialName("downFileType") var downFileType: Int = 0,
        @SerialName("openCloudType") var openCloudType: Int = 0,
        @SerialName("encode") var encode: Int = 0,
        @SerialName("checksum") var checksum: String = "",
        @SerialName("appid") var appid: Int = 0,
        @SerialName("opentype") var opentype: Int = 0,
        @SerialName("category") var category: Int = 0,
    ) {
        @Serializable
        data class AppType(
            @SerialName("type_name") var typeName: String = "",
            @SerialName("type_id") var typeId: Int = 0,
        )

        @Serializable
        data class AppPermission(
            @SerialName("desc") var desc: String = "",
            @SerialName("title") var title: String = "",
            @SerialName("seq") var seq: Int = 0,
        )

        @Serializable
        data class Localurl(
            @SerialName("name") var name: String = "",
            @SerialName("url") var url: String = "",
            @SerialName("urlType") var urlType: String = "",
        )
    }
}
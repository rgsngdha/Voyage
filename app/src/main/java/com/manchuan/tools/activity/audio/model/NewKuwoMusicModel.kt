package com.manchuan.tools.activity.audio.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class NewKuwoMusicModel(
    @SerialName("ARTISTPIC")
    var aRTISTPIC: String = "",
    @SerialName("HIT")
    var hIT: String = "",
    @SerialName("HITMODE")
    var hITMODE: String = "",
    @SerialName("HIT_BUT_OFFLINE")
    var hITBUTOFFLINE: String = "",
    @SerialName("MSHOW")
    var mSHOW: String = "",
    @SerialName("NEW")
    var nEW: String = "",
    @SerialName("PN")
    var pN: String = "",
    @SerialName("RN")
    var rN: String = "",
    @SerialName("SHOW")
    var sHOW: String = "",
    @SerialName("TOTAL")
    var tOTAL: String = "",
    @SerialName("UK")
    var uK: String = "",
    @SerialName("abslist")
    var abslist: List<Abslist> = listOf(),
    @SerialName("searchgroup")
    var searchgroup: String = ""
) : Parcelable {
    @Serializable
    @Parcelize
    data class Abslist(
        @SerialName("AARTIST")
        var aARTIST: String = "",
        @SerialName("ALBUM")
        var aLBUM: String = "",
        @SerialName("ALBUMID")
        var aLBUMID: String = "",
        @SerialName("ALIAS")
        var aLIAS: String = "",
        @SerialName("ARTIST")
        var aRTIST: String = "",
        @SerialName("ARTISTID")
        var aRTISTID: String = "",
        @SerialName("CanSetRing")
        var canSetRing: String = "",
        @SerialName("CanSetRingback")
        var canSetRingback: String = "",
        @SerialName("DC_TARGETID")
        var dCTARGETID: String = "",
        @SerialName("DC_TARGETTYPE")
        var dCTARGETTYPE: String = "",
        @SerialName("DURATION")
        var dURATION: String = "",
        @SerialName("FARTIST")
        var fARTIST: String = "",
        @SerialName("FORMAT")
        var fORMAT: String = "",
        @SerialName("FSONGNAME")
        var fSONGNAME: String = "",
        @SerialName("KMARK")
        var kMARK: String = "",
        @SerialName("MINFO")
        var mINFO: String = "",
        @SerialName("MUSICRID")
        var mUSICRID: String = "",
        @SerialName("MVFLAG")
        var mVFLAG: String = "",
        @SerialName("MVPIC")
        var mVPIC: String = "",
        @SerialName("MVQUALITY")
        var mVQUALITY: String = "",
        @SerialName("NAME")
        var nAME: String = "",
        @SerialName("NEW")
        var nEW: String = "",
        @SerialName("N_MINFO")
        var nMINFO: String = "",
        @SerialName("ONLINE")
        var oNLINE: String = "",
        @SerialName("PAY")
        var pAY: String = "",
        @SerialName("PROVIDER")
        var pROVIDER: String = "",
        @SerialName("SONGNAME")
        var sONGNAME: String = "",
        @SerialName("SUBLIST")
        var sUBLIST: List<String> = listOf(),
        @SerialName("SUBTITLE")
        var sUBTITLE: String = "",
        @SerialName("TAG")
        var tAG: String = "",
        @SerialName("ad_subtype")
        var adSubtype: String = "",
        @SerialName("ad_type")
        var adType: String = "",
        @SerialName("allartistid")
        var allartistid: String = "",
        @SerialName("audiobookpayinfo")
        var audiobookpayinfo: Audiobookpayinfo = Audiobookpayinfo(),
        @SerialName("barrage")
        var barrage: String = "",
        @SerialName("cache_status")
        var cacheStatus: String = "",
        @SerialName("content_type")
        var contentType: String = "",
        @SerialName("fpay")
        var fpay: String = "",
        @SerialName("hts_MVPIC")
        var htsMVPIC: String = "",
        @SerialName("info")
        var info: String = "",
        @SerialName("iot_info")
        var iotInfo: String = "",
        @SerialName("isdownload")
        var isdownload: String = "",
        @SerialName("isshowtype")
        var isshowtype: String = "",
        @SerialName("isstar")
        var isstar: String = "",
        @SerialName("mvpayinfo")
        var mvpayinfo: Mvpayinfo = Mvpayinfo(),
        @SerialName("nationid")
        var nationid: String = "",
        @SerialName("opay")
        var opay: String = "",
        @SerialName("originalsongtype")
        var originalsongtype: String = "",
        @SerialName("overseas_copyright")
        var overseasCopyright: String = "",
        @SerialName("overseas_pay")
        var overseasPay: String = "",
        @SerialName("payInfo")
        var payInfo: PayInfo = PayInfo(),
        @SerialName("react_type")
        var reactType: String = "",
        @SerialName("spPrivilege")
        var spPrivilege: String = "",
        @SerialName("subsStrategy")
        var subsStrategy: String = "",
        @SerialName("subsText")
        var subsText: String = "",
        @SerialName("terminal")
        var terminal: String = "",
        @SerialName("tme_musician_adtype")
        var tmeMusicianAdtype: String = "",
        @SerialName("tpay")
        var tpay: String = "",
        @SerialName("web_albumpic_short")
        var webAlbumpicShort: String = "",
        @SerialName("web_artistpic_short")
        var webArtistpicShort: String = "",
        @SerialName("web_timingonline")
        var webTimingonline: String = ""
    ) : Parcelable {
        @Serializable
        @Parcelize
        data class Audiobookpayinfo(
            @SerialName("download")
            var download: String = "",
            @SerialName("play")
            var play: String = ""
        ) : Parcelable

        @Serializable
        @Parcelize
        data class Mvpayinfo(
            @SerialName("download")
            var download: String = "",
            @SerialName("play")
            var play: String = "",
            @SerialName("vid")
            var vid: String = ""
        ) : Parcelable

        @Serializable
        @Parcelize
        data class PayInfo(
            @SerialName("cannotDownload")
            var cannotDownload: String = "",
            @SerialName("cannotOnlinePlay")
            var cannotOnlinePlay: String = "",
            @SerialName("download")
            var download: String = "",
            @SerialName("feeType")
            var feeType: FeeType = FeeType(),
            @SerialName("limitfree")
            var limitfree: String = "",
            @SerialName("listen_fragment")
            var listenFragment: String = "",
            @SerialName("local_encrypt")
            var localEncrypt: String = "",
            @SerialName("ndown")
            var ndown: String = "",
            @SerialName("nplay")
            var nplay: String = "",
            @SerialName("overseas_ndown")
            var overseasNdown: String = "",
            @SerialName("overseas_nplay")
            var overseasNplay: String = "",
            @SerialName("paytagindex")
            var paytagindex: Paytagindex = Paytagindex(),
            @SerialName("play")
            var play: String = "",
            @SerialName("refrain_end")
            var refrainEnd: String = "",
            @SerialName("refrain_start")
            var refrainStart: String = "",
            @SerialName("tips_intercept")
            var tipsIntercept: String = ""
        ) : Parcelable {
            @Serializable
            @Parcelize
            data class FeeType(
                @SerialName("album")
                var album: String = "",
                @SerialName("bookvip")
                var bookvip: String = "",
                @SerialName("song")
                var song: String = "",
                @SerialName("vip")
                var vip: String = ""
            ) : Parcelable

            @Serializable
            @Parcelize
            data class Paytagindex(
                @SerialName("AR501")
                var aR501: Int = 0,
                @SerialName("DB")
                var dB: Int = 0,
                @SerialName("F")
                var f: Int = 0,
                @SerialName("H")
                var h: Int = 0,
                @SerialName("HR")
                var hR: Int = 0,
                @SerialName("L")
                var l: Int = 0,
                @SerialName("S")
                var s: Int = 0,
                @SerialName("ZP")
                var zP: Int = 0,
                @SerialName("ZPGA201")
                var zPGA201: Int = 0,
                @SerialName("ZPGA501")
                var zPGA501: Int = 0,
                @SerialName("ZPLY")
                var zPLY: Int = 0
            ) : Parcelable
        }
    }
}
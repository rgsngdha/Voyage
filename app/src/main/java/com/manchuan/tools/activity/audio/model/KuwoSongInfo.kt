package com.manchuan.tools.activity.audio.model


import android.os.Parcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KuwoSongInfo(
    @SerialName("data") var `data`: Data = Data(),
    @SerialName("msg") var msg: String = "",
    @SerialName("msgs") var msgs: String? = null,
    @SerialName("profileid") var profileid: String = "",
    @SerialName("reqid") var reqid: String = "",
    @SerialName("status") var status: Int = 0
) {
    @Serializable
    data class Data(
        @SerialName("lrclist") var lrclist: List<Lrclist> = listOf(),
        @SerialName("simpl") var simpl: Simpl = Simpl(),
        @SerialName("songinfo") var songinfo: Songinfo = Songinfo()
    ) {
        @Serializable
        @kotlinx.parcelize.Parcelize
        data class Lrclist(
            @SerialName("lineLyric") var lineLyric: String = "",
            @SerialName("time") var time: String = ""
        ) : Parcelable

        @Serializable
        data class Simpl(
            @SerialName("musiclist") var musiclist: List<Musiclist> = listOf(),
            @SerialName("playlist") var playlist: List<Playlist> = listOf()
        ) {
            @Serializable
            data class Musiclist(
                @SerialName("album") var album: String = "",
                @SerialName("albumId") var albumId: Int = 0,
                @SerialName("artist") var artist: String = "",
                @SerialName("artistId") var artistId: Long = 0,
                @SerialName("contentType") var contentType: String? = null,
                @SerialName("coopFormats") var coopFormats: List<String> = listOf(),
                @SerialName("copyRight") var copyRight: String? = null,
                @SerialName("duration") var duration: Long = 0,
                @SerialName("formats") var formats: String = "",
                @SerialName("hasEcho") var hasEcho: Long = 0,
                @SerialName("hasMv") var hasMv: Long = 0,
                @SerialName("id") var id: String = "",
                @SerialName("isExt") var isExt: String = "",
                @SerialName("isNew") var isNew: String = "",
                @SerialName("isPoint") var isPoint: String = "",
                @SerialName("isbatch") var isbatch: String = "",
                @SerialName("isdownload") var isdownload: String = "",
                @SerialName("isstar") var isstar: String = "",
                @SerialName("mkvNsig1") var mkvNsig1: String = "",
                @SerialName("mkvNsig2") var mkvNsig2: String = "",
                @SerialName("mkvRid") var mkvRid: String = "",
                @SerialName("mp3Nsig1") var mp3Nsig1: Long = 0,
                @SerialName("mp3Nsig2") var mp3Nsig2: Long = 0,
                @SerialName("mp3Rid") var mp3Rid: String = "",
                @SerialName("mp3Size") var mp3Size: String = "",
                @SerialName("mp4sig1") var mp4sig1: String = "",
                @SerialName("mp4sig2") var mp4sig2: String = "",
                @SerialName("musicrId") var musicrId: String = "",
                @SerialName("mutiVer") var mutiVer: Long = 0,
                @SerialName("mvpayinfo") var mvpayinfo: String = "",
                @SerialName("mvpic") var mvpic: String = "",
                @SerialName("nsig1") var nsig1: String = "",
                @SerialName("nsig2") var nsig2: String = "",
                @SerialName("online") var online: String = "",
                @SerialName("params") var params: String = "",
                @SerialName("pay") var pay: String = "",
                @SerialName("pic") var pic: String = "",
                @SerialName("playCnt") var playCnt: String = "",
                @SerialName("rankChange") var rankChange: String = "",
                @SerialName("reason") var reason: String = "",
                @SerialName("score") var score: String = "",
                @SerialName("score100") var score100: String = "",
                @SerialName("songName") var songName: String = "",
                @SerialName("songTimeMinutes") var songTimeMinutes: String = "",
                @SerialName("tpay") var tpay: String = "",
                @SerialName("trend") var trend: String = "",
                @SerialName("upTime") var upTime: Long = 0,
                @SerialName("uploader") var uploader: String = ""
            )

            @Serializable
            data class Playlist(
                @SerialName("digest") var digest: String = "",
                @SerialName("disname") var disname: String = "",
                @SerialName("extend") var extend: String = "",
                @SerialName("info") var info: String = "",
                @SerialName("isnew") var isnew: String = "",
                @SerialName("name") var name: String = "",
                @SerialName("newcount") var newcount: String = "",
                @SerialName("nodeid") var nodeid: String = "",
                @SerialName("pic") var pic: String = "",
                @SerialName("playcnt") var playcnt: String = "",
                @SerialName("source") var source: String = "",
                @SerialName("sourceid") var sourceid: String = "",
                @SerialName("tag") var tag: String = ""
            )
        }

        @Serializable
        data class Songinfo(
            @SerialName("album") var album: String = "",
            @SerialName("albumId") var albumId: String = "",
            @SerialName("artist") var artist: String = "",
            @SerialName("artistId") var artistId: String = "",
            @SerialName("contentType") var contentType: String = "",
            @SerialName("coopFormats") var coopFormats: List<String> = listOf(),
            @SerialName("copyRight") var copyRight: String = "",
            @SerialName("duration") var duration: String = "",
            @SerialName("formats") var formats: String = "",
            @SerialName("hasEcho") var hasEcho: String = "",
            @SerialName("hasMv") var hasMv: String = "",
            @SerialName("id") var id: String = "",
            @SerialName("isExt") var isExt: Long = 0,
            @SerialName("isNew") var isNew: Long = 0,
            @SerialName("isPoint") var isPoint: String = "",
            @SerialName("isbatch") var isbatch: Long = 0,
            @SerialName("isdownload") var isdownload: String = "",
            @SerialName("isstar") var isstar: String = "",
            @SerialName("mkvNsig1") var mkvNsig1: String = "",
            @SerialName("mkvNsig2") var mkvNsig2: String = "",
            @SerialName("mkvRid") var mkvRid: String = "",
            @SerialName("mp3Nsig1") var mp3Nsig1: String = "",
            @SerialName("mp3Nsig2") var mp3Nsig2: String = "",
            @SerialName("mp3Rid") var mp3Rid: String = "",
            @SerialName("mp3Size") var mp3Size: String = "",
            @SerialName("mp4sig1") var mp4sig1: String = "",
            @SerialName("mp4sig2") var mp4sig2: String = "",
            @SerialName("musicrId") var musicrId: String = "",
            @SerialName("mutiVer") var mutiVer: String = "",
            @SerialName("mvpayinfo") var mvpayinfo: Long = 0,
            @SerialName("mvpic") var mvpic: Long = 0,
            @SerialName("nsig1") var nsig1: String = "",
            @SerialName("nsig2") var nsig2: String = "",
            @SerialName("online") var online: String = "",
            @SerialName("params") var params: Long = 0,
            @SerialName("pay") var pay: String = "",
            @SerialName("pic") var pic: String = "",
            @SerialName("playCnt") var playCnt: String = "",
            @SerialName("rankChange") var rankChange: Long = 0,
            @SerialName("reason") var reason: Long = 0,
            @SerialName("score") var score: Long = 0,
            @SerialName("score100") var score100: String = "",
            @SerialName("songName") var songName: String = "",
            @SerialName("songTimeMinutes") var songTimeMinutes: String = "",
            @SerialName("tpay") var tpay: Long = 0,
            @SerialName("trend") var trend: Long = 0,
            @SerialName("upTime") var upTime: String = "",
            @SerialName("uploader") var uploader: String = ""
        )
    }
}
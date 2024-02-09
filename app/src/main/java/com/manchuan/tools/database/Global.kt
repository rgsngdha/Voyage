package com.manchuan.tools.database

import android.content.pm.ActivityInfo
import androidx.lifecycle.MutableLiveData
import com.drake.serialize.serialize.serialLazy
import com.drake.serialize.serialize.serialLiveData
import com.google.android.material.color.DynamicColors
import com.lzx.starrysky.control.RepeatMode
import com.manchuan.tools.activity.movies.model.MovieCategorys
import com.manchuan.tools.cache.DiskCache
import com.manchuan.tools.cache.glide.key.GlideKey
import com.manchuan.tools.fragment.model.FunctionModel
import com.manchuan.tools.model.QQInfo
import com.manchuan.tools.user.model.LoginModel
import top.xuqingquan.m3u8downloader.entity.VideoDownloadEntity

object Global {
    var weatherPublicId by serialLazy("0")
    var weatherPrivateKey by serialLazy("0")
    var isEnabledDialogBlur by serialLazy(false)
    var remoteVersion by serialLazy(0)
    var isAcceptPolicy by serialLazy(false)
    var isNeverAsk by serialLazy(false)
    var avatarSignature by serialLazy(GlideKey("avatarSignatures"))
    var isEnabledVideoHDR by serialLazy(false)
    var videoColorMode by serialLazy(ActivityInfo.COLOR_MODE_HDR)
    var diskCacheStrategy by serialLazy(DiskCache.AUTOMATIC)
    var countLaunch: Int by serialLazy(0)
    var isGuideAndFirstLaunch: Boolean by serialLazy(true)
    var downloadVideoList by serialLazy(mutableListOf<VideoDownloadEntity>())
    const val AppId = "0"
    var localMovieCategories: List<MovieCategorys.Data.DataList.Filter> by serialLazy()
    var localSentence: String by serialLazy("永远相信美好的事情即将发生")
    var idVerify: String by serialLazy()
    var userModel: LoginModel? by serialLazy(null)
    var qqUserModel: QQInfo by serialLazy()
    val token by serialLiveData("")
    const val APP_ID = "0"
    const val API_KEY = "0"
    const val SECRET_KEY = "0"
    var baiduAccessToken: String by serialLazy()
    val smallSpeakCache by serialLiveData(true)
    val isAccessLocation by serialLiveData(false)
    val isEnabledDynamicColors by serialLiveData(DynamicColors.isDynamicColorAvailable())
    val isEnabledHideFunction by serialLiveData(false)
    var isTimeScreenVibration by serialLazy(true)
    var isVideoWallpaperMute by serialLazy(false)
    val isCanUserInput by serialLiveData(false)
    var isEnabledLyricBlur by serialLazy(true)
    val favoriteFunctions: MutableLiveData<MutableList<FunctionModel>> by serialLiveData(
        mutableListOf()
    )
    var repeatMode by serialLazy(RepeatMode.REPEAT_MODE_SHUFFLE)
    var defaultVolume by serialLazy(1.00f)
}
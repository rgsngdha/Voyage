package com.manchuan.tools.application

import ando.file.core.FileOperator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.amap.api.location.AMapLocationClient
import com.blankj.utilcode.util.CacheDiskUtils
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.coder.ffmpeg.jni.FFmpegCommand
import com.drake.brv.utils.BRV
import com.drake.channel.sendEvent
import com.drake.engine.utils.GB
import com.drake.logcat.LogCat
import com.drake.net.NetConfig
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDialogFactory
import com.drake.net.okhttp.trustSSLCertificate
import com.drake.statelayout.StateConfig
import com.drake.statelayout.handler.FadeStateChangedHandler
import com.drake.tooltip.dialog.BubbleDialog
import com.dylanc.longan.context
import com.dylanc.longan.externalMusicDirPath
import com.dylanc.longan.fileProviderAuthority
import com.dylanc.longan.handleUncaughtException
import com.dylanc.longan.internalMusicDirPath
import com.dylanc.longan.isOppoRom
import com.dylanc.longan.randomUUIDString
import com.dylanc.longan.toast
import com.github.gzuliyujiang.oaid.DeviceIdentifier
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchFactory
import com.github.panpf.sketch.request.PauseLoadWhenScrollingDrawableDecodeInterceptor
import com.github.panpf.sketch.request.SaveCellularTrafficDisplayInterceptor
import com.google.android.material.color.DynamicColors
import com.hjq.language.MultiLanguages
import com.hjq.toast.ToastUtils
import com.jeffmony.downloader.VideoDownloadConfig
import com.jeffmony.downloader.VideoDownloadManager
import com.jeffmony.downloader.common.DownloadConstants
import com.jeffmony.downloader.listener.DownloadListener
import com.jeffmony.downloader.model.VideoTaskItem
import com.jeffmony.downloader.utils.VideoDownloadUtils
import com.jinrishici.sdk.android.factory.JinrishiciFactory
import com.lxj.androidktx.AndroidKTX
import com.lxj.androidktx.core.doOnceIn
import com.lxj.androidktx.core.startActivity
import com.lzx.starrysky.GlobalPlaybackStageListener
import com.lzx.starrysky.StarrySkyInstall
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.notification.INotification
import com.lzx.starrysky.notification.NotificationConfig
import com.manchuan.tools.BR
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.ErrorActivity
import com.manchuan.tools.auth.baidu.AuthService
import com.manchuan.tools.database.Global
import com.manchuan.tools.database.music.RecentMusicDatabase
import com.manchuan.tools.database.music.SongInfoEntity
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.publicMoviesDirPath
import com.manchuan.tools.extensions.tryWith
import com.manchuan.tools.extensions.uiScope
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.model.appModels
import com.manchuan.tools.theme.untils.ThemeStore
import com.manchuan.tools.utils.ColorUtils
import com.manchuan.tools.utils.SettingsLoader
import com.manchuan.tools.utils.ThemeUtils
import com.manchuan.tools.utils.Utility
import com.qweather.sdk.view.HeConfig
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.skydoves.whatif.whatIfNotNull
import com.tencent.mmkv.MMKV
import com.tencent.tauth.Tencent
import com.umeng.commonsdk.UMConfigure
import dev.DevUtils
import dev.engine.DevEngine
import kotlinx.coroutines.launch
import okhttp3.Cache
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import rikka.sui.Sui
import timber.log.Timber
import timber.log.Timber.Forest.plant
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit


class App : MultiDexApplication(), SketchFactory {

    /**
    # 创建Sketch图片加载框架配置
     * @author 川意
     * @return 加载框架实例
     */
    override fun createSketch(): Sketch {
        return Sketch.Builder(this).apply {
            components {
                addRequestInterceptor(SaveCellularTrafficDisplayInterceptor())
                addDrawableDecodeInterceptor(PauseLoadWhenScrollingDrawableDecodeInterceptor())
            }
        }.build()
    }

    fun isSui(): Boolean {
        return isSui
    }

    private val recentMusicDatabase by lazy {
        RecentMusicDatabase.getInstance(applicationContext)
    }


    //刷新StarrySky配置
    private fun refreshStarry(cache: Boolean, cachePath: String = externalMusicDirPath.toString()) {
        val notificationConfig = NotificationConfig.create {
            targetClass { "$packageName.service.SongNotificationReceiver" }
            targetClassBundle {
                val bundle = Bundle()
                bundle.putString("targetClass", "$packageName.activity.audio.MusicPlayerActivity")
                //参数自带当前音频播放信息，不用自己传
                return@targetClassBundle bundle
            }
            pendingIntentMode { NotificationConfig.MODE_BROADCAST }
        }
        StarrySkyInstall.init(this).setNotificationSwitch(true).setOpenCache(cache)
            .setGlobalPlaybackStageListener(object : GlobalPlaybackStageListener {
                override fun onPlaybackStageChange(stage: PlaybackStage) {
                    when (stage.stage) {
                        PlaybackStage.SWITCH -> {
                            runCatching {
                                val songInfo = stage.songInfo
                                songInfo?.let { info ->
                                    recentMusicDatabase.musicFlowDao().queryAllMusic()
                                        .let { songInfoList ->
                                            if (songInfoList.find { it.songName == info.songName } == null) {
                                                recentMusicDatabase.musicFlowDao().insertMusic(
                                                    SongInfoEntity(
                                                        id = 0,
                                                        songId = info.songId,
                                                        songUrl = info.songUrl,
                                                        songName = info.songName,
                                                        artist = info.artist,
                                                        songCover = info.songCover,
                                                        duration = info.duration,
                                                        decode = info.decode,
                                                        lrclist = info.lrclist,
                                                        abslist = info.abslist,
                                                        lyric = info.lyric,
                                                        translateLyric = info.translateLyric
                                                    )
                                                )
                                                sendEvent(true, "refresh_recent_music")
                                                loge(
                                                    tag = "RecentMusic",
                                                    "添加成功:${info.songName} - ${info.artist}"
                                                )
                                            } else {
                                                loge(
                                                    "RecentMusic_SWITCH",
                                                    "最近播放中已包含该歌曲:${info.songName} - ${info.artist}"
                                                )
                                            }
                                        }
                                }
                            }.onFailure {
                                loge("RecentMusic", "添加失败", it)
                            }
                        }

                        PlaybackStage.PLAYING -> {
                            runCatching {
                                val songInfo = stage.songInfo
                                songInfo?.let { info ->
                                    recentMusicDatabase.musicFlowDao().queryAllMusic()
                                        .let { songInfoList ->
                                            if (songInfoList.find { it.songName == info.songName } == null) {
                                                recentMusicDatabase.musicFlowDao().insertMusic(
                                                    SongInfoEntity(
                                                        id = 0,
                                                        songId = info.songId,
                                                        songUrl = info.songUrl,
                                                        songName = info.songName,
                                                        artist = info.artist,
                                                        songCover = info.songCover,
                                                        duration = info.duration,
                                                        decode = info.decode,
                                                        lrclist = info.lrclist,
                                                        abslist = info.abslist,
                                                        lyric = info.lyric,
                                                        translateLyric = info.translateLyric
                                                    )
                                                )
                                                sendEvent(true, "refresh_recent_music")
                                                loge(
                                                    tag = "RecentMusic",
                                                    "添加成功:${info.songName} - ${info.artist}"
                                                )
                                            } else {
                                                loge(
                                                    "RecentMusic_PLAYING",
                                                    "最近播放中已包含该歌曲:${info.songName} - ${info.artist}"
                                                )
                                            }
                                        }
                                }
                            }.onFailure {
                                loge("RecentMusic", "添加失败", it)
                            }
                        }
                    }
                }

            }).setCacheDestFileDir(internalMusicDirPath.toString()).setCacheMaxBytes(10.GB)
            .setNotificationType(INotification.SYSTEM_NOTIFICATION)
            .setNotificationConfig(notificationConfig).apply()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        toast("当前设备可用内存较小，请释放无用应用进程使用本软件。")
    }

    override fun onCreate() {
        super.onCreate()
        MultiLanguages.init(this)
        isSui = Sui.init(BuildConfig.APPLICATION_ID);
        if (!isSui) {
            // If this is a multi-process application
            //ShizukuProvider.enableMultiProcessSupport( /* is current process the same process of ShizukuProvider's */ );
        }
        LogCat.setDebug(BuildConfig.DEBUG)
        DevUtils.init(this)
        DeviceIdentifier.register(this)
        tencent = Tencent.createInstance("102038002", context, "com.tencent.sample.fileprovider")
        DevEngine.completeInitialize(this)
        ToastUtils.init(this)
        Global.smallSpeakCache.observeForever {
            if (true) {
                refreshStarry(true)
            } else {
                refreshStarry(false)
            }
        }
        UMConfigure.preInit(this, "651c0ac2b2f6fa00ba5d05de", "官方")
        JinrishiciFactory.init(this);
        runCatching {
            FFmpegCommand.setDebug(BuildConfig.DEBUG)
        }
        fileProviderAuthority = "$packageName.provider"
        Companion.context = baseContext
        if (!ThemeStore.isConfigured(this, 3)) {
            ThemeStore.editTheme(this).accentColorRes(R.color.colorAccent)
                .coloredNavigationBar(true).coloredStatusBar(true).commit()
        }
        instance = this
        HeConfig.init(Global.weatherPublicId, Global.weatherPrivateKey)
        HeConfig.switchToDevService()
        val config: VideoDownloadConfig =
            VideoDownloadManager.Build(this).setCacheRoot(publicMoviesDirPath)
                .setTimeOut(DownloadConstants.READ_TIMEOUT, DownloadConstants.CONN_TIMEOUT)
                .setConcurrentCount(24).setIgnoreCertErrors(true).setShouldM3U8Merged(true)
                .buildConfig()
        VideoDownloadUtils.setDownloadConfig(config)
        VideoDownloadManager.getInstance().initConfig(config)
        handleUncaughtException { thread, throwable ->
            uiScope.launch {
                applicationContext.startActivity<ErrorActivity>(
                    flag = Intent.FLAG_ACTIVITY_NEW_TASK, bundle = arrayOf("error" to throwable)
                )
            }
        }
        val mListener: DownloadListener = object : DownloadListener() {
            override fun onDownloadDefault(item: VideoTaskItem?) {
                loge("movie", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                }
            }

            override fun onDownloadPending(item: VideoTaskItem?) {
                loge("movie", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                }
            }

            override fun onDownloadPrepare(item: VideoTaskItem?) {
                loge("movie", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                }
            }

            override fun onDownloadStart(item: VideoTaskItem?) {
                loge("movie", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                    toast(it.title + " 开始下载")
                }
            }

            override fun onDownloadProgress(item: VideoTaskItem?) {
                loge("movie", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                }
            }

            override fun onDownloadSpeed(item: VideoTaskItem?) {
                loge("movie", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                }
            }

            override fun onDownloadPause(item: VideoTaskItem?) {
                loge("movie", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                }
            }

            override fun onDownloadError(item: VideoTaskItem?) {
                loge("movieerror", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                }
            }

            override fun onDownloadSuccess(item: VideoTaskItem?) {
                loge("movie", item)
                item.whatIfNotNull {
                    sendEvent(it, "movie")
                }
            }
        }
        VideoDownloadManager.getInstance().setGlobalDownloadListener(mListener)
        CacheDiskUtils.getInstance(cacheDir)
        ColorUtils.initialize(this)
        SettingsLoader.init(this)
        SettingsLoader.loadAnalytic(this)
        SettingsLoader.loadSettings()
        SettingsLoader.loadDialogConfig(this)
        FileOperator.init(this, BuildConfig.DEBUG)
        SettingsLoader.nightMode?.let { AppCompatDelegate.setDefaultNightMode(it) }
        ThemeUtils.init(this)
        Utility.init(this)
        //
        AndroidKTX.init(this)
        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }
        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(appModels)
        }
        if (Global.isAcceptPolicy && Global.isNeverAsk) {
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
        }
        runCatching {
            if (Global.isEnabledDynamicColors.value == true) {
                DynamicColors.applyToActivitiesIfAvailable(this)
            }
        }.onFailure {
            toast("您的设备不支持动态取色")
        }
        AndroidKTX.init(this)
        BRV.modelId = BR.m
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout -> MaterialHeader(this) }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout -> ClassicsFooter(this) }
        NetConfig.initialize {
            cache(Cache(filesDir, 10.GB))
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(60,TimeUnit.SECONDS)
            writeTimeout(60,TimeUnit.SECONDS)
            trustSSLCertificate()
            setConverter(SerializationConverter())
            setDialogFactory {
                BubbleDialog(it, "加载中")
            }
            addInterceptor(
                ChuckerInterceptor.Builder(this@App).collector(ChuckerCollector(applicationContext))
                    .build()
            )
        }
        StateConfig.apply {
            loadingLayout = R.layout.layout_loading // 配置全局的加载中布局
            errorLayout = R.layout.layout_error
            emptyLayout = R.layout.layout_empty
            stateChangedHandler = FadeStateChangedHandler()
        }
        Global.idVerify = when {
            isOppoRom -> DeviceIdentifier.getAndroidID(this)

            else -> DeviceIdentifier.getOAID(this).ifEmpty {
                DeviceIdentifier.getWidevineID().ifEmpty {
                    DeviceIdentifier.getGUID(this).ifEmpty {
                        DeviceIdentifier.getAndroidID(this).ifEmpty { randomUUIDString }
                    }
                }
            }
        }
        runCatching {
            doOnceIn("getAccessToken", 2592000.seconds.toLong(DurationUnit.SECONDS)) {
                Thread {
                    tryWith {
                        Global.baiduAccessToken = AuthService.getAuth()
                    }
                }.start()
            }
        }
        Global.token.observeForever {
            if (it.isNullOrBlank()) {
                Global.userModel = null
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(MultiLanguages.attach(base))
        xcrash.XCrash.init(base)
        MMKV.initialize(base)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: App

        lateinit var tencent: Tencent

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        private var isSui = false
    }

}
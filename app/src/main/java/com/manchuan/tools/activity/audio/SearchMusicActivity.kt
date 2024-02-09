package com.manchuan.tools.activity.audio

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.graphics.drawable.toBitmap
import com.crazylegend.viewbinding.viewBinding
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.utils.runMain
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.internalPicturesDirPath
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.google.android.material.tabs.TabLayout
import com.kongzue.dialogx.dialogs.PopNotification
import com.lxj.androidktx.FragmentStateAdapter
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.click
import com.lxj.androidktx.core.visible
import com.lxj.androidktx.setupWithViewPager2
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.utils.formatTime
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.DownloadActivity
import com.manchuan.tools.activity.audio.fragments.KuwoMusicFragment
import com.manchuan.tools.activity.audio.fragments.RecentMusicFragment
import com.manchuan.tools.activity.audio.model.MediaInfo
import com.manchuan.tools.activity.audio.model.MusicQualityModel
import com.manchuan.tools.activity.audio.utils.Base64Coder
import com.manchuan.tools.activity.audio.utils.KuwoDES
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivitySearchMusicBinding
import com.manchuan.tools.extensions.glideDrawable
import com.manchuan.tools.extensions.inputDialog
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.publicAudiosDirPath
import com.manchuan.tools.extensions.saveImage
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.substringBetween
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.writeMusicInfo
import com.mcxiaoke.koi.ext.addToMediaStore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.io.File
import java.math.BigDecimal
import java.nio.charset.Charset

class SearchMusicActivity : BaseActivity() {

    private val binding by viewBinding(ActivitySearchMusicBinding::inflate)

    private val titles = listOf("搜索结果", "最近播放")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "音乐搜索"
            subtitle = "免费下载付费音乐"
            setDisplayHomeAsUpEnabled(true)
        }
        runCatching {
            StarrySky.with().setRepeatMode(Global.repeatMode, true)
            StarrySky.with().setVolume(Global.defaultVolume)
        }
        binding.viewpager.adapter =
            FragmentStateAdapter(KuwoMusicFragment(), RecentMusicFragment(), isLazyLoading = true)
        binding.tab.setupWithViewPager2(binding.viewpager,
            autoRefresh = true,
            enableScroll = true,
            tabConfigurationStrategy = { tab: TabLayout.Tab, i: Int ->
                tab.text = titles[i]
            })
        binding.seekbar.valueFrom = -1.1f
        runCatching {
            StarrySky.with().playbackState().observe(this) { it ->
                when (it.stage) {

                    PlaybackStage.SWITCH -> {
                        val songInfoEntity = it.songInfo
                        songInfoEntity?.let { songInfo ->
                            //验证白名单成功
                            val musicQualityList = mutableListOf<MusicQualityModel>()
                            songInfo.abslist?.mINFO?.split(";")?.forEach { s ->
                                musicQualityList.add(
                                    MusicQualityModel(
                                        s.substringBetween("level:", ",bitrate:"),
                                        s.substringBetween("bitrate:", ",format:"),
                                        s.substringBetween("format:", ",size:"),
                                        s.substringAfter("size:")
                                    )
                                )
                            }
                            val qualityTitle = arrayListOf<String>()
                            val qualityUrl = arrayListOf<String>()
                            val encryptInfo =
                                "user=0&android_id=0&prod=kwplayer_ar_8.5.5.0&corp=kuwo&newver=3&vipver=8.5.5.0&source=kwplayer_ar_8.5.5.0_apk_keluze.apk&p2p=1&notrace=0&type=convert_url2&br=%sk%s&format=%s&sig=0&rid=%s&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=down"
                            val url = "http://nmobi.kuwo.cn/mobi.s?f=kuwo&q=%s"
                            musicQualityList.forEach {
                                val lastFormatInfo = encryptInfo.format(
                                    it.bitrate, it.format, it.size, songInfo.songId
                                )
                                when (it.bitrate) {
                                    "2000" -> {
                                        qualityTitle.add("无损音质 (${it.format.uppercase()},${it.size.uppercase()})")
                                        val encryptBytes = KuwoDES.encrypt2(
                                            lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                            lastFormatInfo.length,
                                            KuwoDES.SECRET_KEY,
                                            KuwoDES.SECRET_KEY_LENG
                                        )
                                        val base64Coder =
                                            Base64Coder.encode(encryptBytes).toCharArray()
                                        qualityUrl.add(url.format(String(base64Coder)))
                                    }

                                    "320" -> {
                                        qualityTitle.add("超品音质 (${it.format.uppercase()},${it.size.uppercase()})")
                                        val encryptBytes = KuwoDES.encrypt2(
                                            lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                            lastFormatInfo.length,
                                            KuwoDES.SECRET_KEY,
                                            KuwoDES.SECRET_KEY_LENG
                                        )
                                        val base64Coder =
                                            Base64Coder.encode(encryptBytes).toCharArray()
                                        qualityUrl.add(url.format(String(base64Coder)))
                                    }

                                    "128" -> {
                                        qualityTitle.add("高品音质 (${it.format.uppercase()},${it.size.uppercase()})")
                                        val encryptBytes = KuwoDES.encrypt2(
                                            lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                            lastFormatInfo.length,
                                            KuwoDES.SECRET_KEY,
                                            KuwoDES.SECRET_KEY_LENG
                                        )
                                        val base64Coder =
                                            Base64Coder.encode(encryptBytes).toCharArray()
                                        qualityUrl.add(url.format(String(base64Coder)))
                                    }

                                    "48" -> {
                                        qualityTitle.add("标准音质 (${it.format.uppercase()},${it.size.uppercase()})")
                                        val encryptBytes = KuwoDES.encrypt2(
                                            lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                            lastFormatInfo.length,
                                            KuwoDES.SECRET_KEY,
                                            KuwoDES.SECRET_KEY_LENG
                                        )
                                        val base64Coder =
                                            Base64Coder.encode(encryptBytes).toCharArray()
                                        qualityUrl.add(url.format(String(base64Coder)))
                                    }

                                }
                            }
                            binding.downCard.throttleClick {
                                selector(
                                    qualityTitle.toList(), "选择音质"
                                ) { dialogInterface, s, i ->
                                    scopeNetLife {
                                        val request =
                                            Request.Builder().url(qualityUrl[i]).method("GET", null)
                                                .build()
                                        OkHttpClient().newCall(request).enqueue(object : Callback {
                                            override fun onFailure(call: Call, e: IOException) {
                                                runMain {
                                                    toast(e.message)
                                                }
                                            }

                                            override fun onResponse(
                                                call: Call,
                                                response: Response,
                                            ) {
                                                if (response.isSuccessful) {
                                                    response.body?.let {
                                                        val data = it.string()
                                                        loge(data)
                                                        runMain {
                                                            scopeNetLife {
                                                                PopNotification.show(
                                                                    "下载管理器",
                                                                    "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 开始下载。"
                                                                )
                                                                val getMusicFile = Get<File>(
                                                                    data.substringAfter("url=")
                                                                        .substringBefore("sig=")
                                                                ) {
                                                                    setDownloadDir(
                                                                        publicAudiosDirPath
                                                                    )
                                                                    setDownloadFileNameConflict(true)
                                                                    setDownloadMd5Verify(true)
                                                                    setDownloadFileName("${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format}")
                                                                }
                                                                val resultMusicFile =
                                                                    getMusicFile.await()
                                                                writeMusicInfo(
                                                                    resultMusicFile,
                                                                    songInfo.songName,
                                                                    songInfo.artist,
                                                                    songInfo.artist
                                                                )
                                                                addToMediaStore(resultMusicFile)
                                                                PopNotification.show(
                                                                    "下载管理器",
                                                                    "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 下载成功。"
                                                                )
                                                            }.catch {
                                                                PopNotification.show(
                                                                    "下载管理器",
                                                                    "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 下载失败。"
                                                                )
                                                                it.printStackTrace()
                                                                toast("下载失败")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        }
                    }

                    PlaybackStage.PLAYING -> {
                        runCatching {
                            sendEvent(true, "playing_lrc")
                            binding.seekbar.animateVisible()
                            binding.downCard.visible()
                            it.songInfo?.let { songInfo ->
                                binding.seekbar.valueTo = songInfo.duration.toFloat()
                                binding.nameOne.text = songInfo.songName
                                binding.nameTwo.text = songInfo.artist
                                binding.icon.load(
                                    songInfo.songCover, skipMemory = true, isCrossFade = true
                                )
                            }
                            binding.play.setImageResource(R.drawable.ic_baseline_stop_24)
                            binding.playCard.click {
                                StarrySky.with().pauseMusic()
                            }
                            val songInfoEntity = it.songInfo
                            songInfoEntity?.let { songInfo ->
                                //验证白名单成功
                                val musicQualityList = mutableListOf<MusicQualityModel>()
                                songInfo.abslist?.mINFO?.split(";")?.forEach { s ->
                                    musicQualityList.add(
                                        MusicQualityModel(
                                            s.substringBetween("level:", ",bitrate:"),
                                            s.substringBetween("bitrate:", ",format:"),
                                            s.substringBetween("format:", ",size:"),
                                            s.substringAfter("size:")
                                        )
                                    )
                                }
                                val qualityTitle = arrayListOf<String>()
                                val qualityUrl = arrayListOf<String>()
                                val encryptInfo =
                                    "user=0&android_id=0&prod=kwplayer_ar_8.5.5.0&corp=kuwo&newver=3&vipver=8.5.5.0&source=kwplayer_ar_8.5.5.0_apk_keluze.apk&p2p=1&notrace=0&type=convert_url2&br=%sk%s&format=%s&sig=0&rid=%s&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=down"
                                val url = "http://nmobi.kuwo.cn/mobi.s?f=kuwo&q=%s"
                                musicQualityList.forEach {
                                    val lastFormatInfo = encryptInfo.format(
                                        it.bitrate, it.format, it.size, songInfo.songId
                                    )
                                    when (it.bitrate) {
                                        "2000" -> {
                                            qualityTitle.add("无损音质 (${it.format.uppercase()},${it.size.uppercase()})")
                                            val encryptBytes = KuwoDES.encrypt2(
                                                lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                                lastFormatInfo.length,
                                                KuwoDES.SECRET_KEY,
                                                KuwoDES.SECRET_KEY_LENG
                                            )
                                            val base64Coder =
                                                Base64Coder.encode(encryptBytes).toCharArray()
                                            qualityUrl.add(url.format(String(base64Coder)))
                                        }

                                        "320" -> {
                                            qualityTitle.add("超品音质 (${it.format.uppercase()},${it.size.uppercase()})")
                                            val encryptBytes = KuwoDES.encrypt2(
                                                lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                                lastFormatInfo.length,
                                                KuwoDES.SECRET_KEY,
                                                KuwoDES.SECRET_KEY_LENG
                                            )
                                            val base64Coder =
                                                Base64Coder.encode(encryptBytes).toCharArray()
                                            qualityUrl.add(url.format(String(base64Coder)))
                                        }

                                        "128" -> {
                                            qualityTitle.add("高品音质 (${it.format.uppercase()},${it.size.uppercase()})")
                                            val encryptBytes = KuwoDES.encrypt2(
                                                lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                                lastFormatInfo.length,
                                                KuwoDES.SECRET_KEY,
                                                KuwoDES.SECRET_KEY_LENG
                                            )
                                            val base64Coder =
                                                Base64Coder.encode(encryptBytes).toCharArray()
                                            qualityUrl.add(url.format(String(base64Coder)))
                                        }

                                        "48" -> {
                                            qualityTitle.add("标准音质 (${it.format.uppercase()},${it.size.uppercase()})")
                                            val encryptBytes = KuwoDES.encrypt2(
                                                lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                                lastFormatInfo.length,
                                                KuwoDES.SECRET_KEY,
                                                KuwoDES.SECRET_KEY_LENG
                                            )
                                            val base64Coder =
                                                Base64Coder.encode(encryptBytes).toCharArray()
                                            qualityUrl.add(url.format(String(base64Coder)))
                                        }

                                    }
                                }
                                binding.downCard.throttleClick {
                                    selector(
                                        qualityTitle.toList(), "选择音质"
                                    ) { dialogInterface, s, i ->
                                        scopeNetLife {
                                            val request =
                                                Request.Builder().url(qualityUrl[i]).method("GET", null)
                                                    .build()
                                            OkHttpClient().newCall(request).enqueue(object : Callback {
                                                override fun onFailure(call: Call, e: IOException) {
                                                    runMain {
                                                        toast(e.message)
                                                    }
                                                }

                                                override fun onResponse(
                                                    call: Call,
                                                    response: Response,
                                                ) {
                                                    if (response.isSuccessful) {
                                                        response.body?.let {
                                                            val data = it.string()
                                                            loge(data)
                                                            runMain {
                                                                scopeNetLife {
                                                                    PopNotification.show(
                                                                        "下载管理器",
                                                                        "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 开始下载。"
                                                                    )
                                                                    val getMusicFile = Get<File>(
                                                                        data.substringAfter("url=")
                                                                            .substringBefore("sig=")
                                                                    ) {
                                                                        setDownloadDir(
                                                                            publicAudiosDirPath
                                                                        )
                                                                        setDownloadFileNameConflict(true)
                                                                        setDownloadMd5Verify(true)
                                                                        setDownloadFileName("${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format}")
                                                                    }
                                                                    val resultMusicFile =
                                                                        getMusicFile.await()
                                                                    writeMusicInfo(
                                                                        resultMusicFile,
                                                                        songInfo.songName,
                                                                        songInfo.artist,
                                                                        songInfo.artist
                                                                    )
                                                                    addToMediaStore(resultMusicFile)
                                                                    PopNotification.show(
                                                                        "下载管理器",
                                                                        "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 下载成功。"
                                                                    )
                                                                }.catch {
                                                                    PopNotification.show(
                                                                        "下载管理器",
                                                                        "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 下载失败。"
                                                                    )
                                                                    it.printStackTrace()
                                                                    toast("下载失败")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    PlaybackStage.PAUSE -> {
                        sendEvent(false, "playing_lrc")
                        binding.play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                        binding.playCard.click {
                            StarrySky.with().restoreMusic()
                        }
                    }

                    PlaybackStage.IDLE -> {
                        binding.play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                    }

                    PlaybackStage.ERROR -> {
                        sendEvent(false, "playing_lrc")
                        binding.seekbar.animateGone()
                        snack("播放失败：" + it.errorMsg)
                    }
                }
            }
        }
        runCatching {
            StarrySky.with().setOnPlayProgressListener(object : OnPlayProgressListener {

                override fun onPlayProgress(currPos: Long, duration: Long) {
                    binding.seekbar.valueTo = duration.toFloat()
                    binding.seekbar.setValue(currPos.toFloat())
                    StarrySky.with().getNowPlayingSongInfo()?.lrclist?.find {
                        (BigDecimal.valueOf(it.time.toDouble()).multiply(BigDecimal(1000.00))
                            .toLong().formatTime()).contains(currPos.formatTime())
                    }?.let {
                        binding.nameTwo.text(it.lineLyric, 100)
                        loge("Lrc", "时间:${currPos.formatTime()}\n歌词:${it.lineLyric}")
                    }
                }
            }, "main_search")
        }.onFailure {
            toast("监听播放进度错误")
        }
        runCatching {
            StarrySky.with().getNowPlayingSongInfo()?.let { songInfo ->
                //验证白名单成功
                val musicQualityList = mutableListOf<MusicQualityModel>()
                songInfo.abslist?.mINFO?.split(";")?.forEach { s ->
                    musicQualityList.add(
                        MusicQualityModel(
                            s.substringBetween("level:", ",bitrate:"),
                            s.substringBetween("bitrate:", ",format:"),
                            s.substringBetween("format:", ",size:"),
                            s.substringAfter("size:")
                        )
                    )
                }
                val qualityTitle = arrayListOf<String>()
                val qualityUrl = arrayListOf<String>()
                val encryptInfo =
                    "user=0&android_id=0&prod=kwplayer_ar_8.5.5.0&corp=kuwo&newver=3&vipver=8.5.5.0&source=kwplayer_ar_8.5.5.0_apk_keluze.apk&p2p=1&notrace=0&type=convert_url2&br=%sk%s&format=%s&sig=0&rid=%s&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=down"
                val url = "http://nmobi.kuwo.cn/mobi.s?f=kuwo&q=%s"
                musicQualityList.forEach {
                    val lastFormatInfo = encryptInfo.format(
                        it.bitrate, it.format, it.size, songInfo.songId
                    )
                    when (it.bitrate) {
                        "2000" -> {
                            qualityTitle.add("无损音质 (${it.format.uppercase()},${it.size.uppercase()})")
                            val encryptBytes = KuwoDES.encrypt2(
                                lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                lastFormatInfo.length,
                                KuwoDES.SECRET_KEY,
                                KuwoDES.SECRET_KEY_LENG
                            )
                            val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                            qualityUrl.add(url.format(String(base64Coder)))
                        }

                        "320" -> {
                            qualityTitle.add("超品音质 (${it.format.uppercase()},${it.size.uppercase()})")
                            val encryptBytes = KuwoDES.encrypt2(
                                lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                lastFormatInfo.length,
                                KuwoDES.SECRET_KEY,
                                KuwoDES.SECRET_KEY_LENG
                            )
                            val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                            qualityUrl.add(url.format(String(base64Coder)))
                        }

                        "128" -> {
                            qualityTitle.add("高品音质 (${it.format.uppercase()},${it.size.uppercase()})")
                            val encryptBytes = KuwoDES.encrypt2(
                                lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                lastFormatInfo.length,
                                KuwoDES.SECRET_KEY,
                                KuwoDES.SECRET_KEY_LENG
                            )
                            val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                            qualityUrl.add(url.format(String(base64Coder)))
                        }

                        "48" -> {
                            qualityTitle.add("标准音质 (${it.format.uppercase()},${it.size.uppercase()})")
                            val encryptBytes = KuwoDES.encrypt2(
                                lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                                lastFormatInfo.length,
                                KuwoDES.SECRET_KEY,
                                KuwoDES.SECRET_KEY_LENG
                            )
                            val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                            qualityUrl.add(url.format(String(base64Coder)))
                        }

                    }
                }
                binding.downCard.throttleClick {
                    selector(
                        qualityTitle.toList(), "选择音质"
                    ) { dialogInterface, s, i ->
                        scopeNetLife {
                            val request =
                                Request.Builder().url(qualityUrl[i]).method("GET", null).build()
                            OkHttpClient().newCall(request).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    runMain {
                                        toast(e.message)
                                    }
                                }

                                override fun onResponse(
                                    call: Call,
                                    response: Response,
                                ) {
                                    if (response.isSuccessful) {
                                        response.body?.let {
                                            val data = it.string()
                                            loge(data)
                                            runMain {
                                                scopeNetLife {
                                                    PopNotification.show(
                                                        "下载管理器",
                                                        "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 开始下载。"
                                                    )
                                                    val getMusicFile = Get<File>(
                                                        data.substringAfter("url=")
                                                            .substringBefore("sig=")
                                                    ) {
                                                        setDownloadDir(
                                                            publicAudiosDirPath
                                                        )
                                                        setDownloadFileNameConflict(true)
                                                        setDownloadMd5Verify(true)
                                                        setDownloadFileName("${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format}")
                                                    }
                                                    val resultMusicFile = getMusicFile.await()
                                                    writeMusicInfo(
                                                        resultMusicFile,
                                                        songInfo.songName,
                                                        songInfo.artist,
                                                        songInfo.artist
                                                    )
                                                    addToMediaStore(resultMusicFile)
                                                    PopNotification.show(
                                                        "下载管理器",
                                                        "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 下载成功。"
                                                    )
                                                }.catch {
                                                    PopNotification.show(
                                                        "下载管理器",
                                                        "${songInfo.songName}_${songInfo.artist}.${musicQualityList[i].format} 下载失败。"
                                                    )
                                                    it.printStackTrace()
                                                    toast("下载失败")
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
        binding.seekbar.addOnSliderTouchStopListener {
            StarrySky.with().seekTo(it.value.toLong(), true)
        }
        binding.bottom.setOnClickListener {
            startActivity<MusicPlayerActivity>()
        }
        receiveEvent<MediaInfo>("playing_song_info") { mediaInfo ->
            val musicQualityList = mutableListOf<MusicQualityModel>()
            mediaInfo.songInfo.abslist?.mINFO?.split(";")?.forEach {
                musicQualityList.add(
                    MusicQualityModel(
                        it.substringBetween("level:", ",bitrate:"),
                        it.substringBetween("bitrate:", ",format:"),
                        it.substringBetween("format:", ",size:"),
                        it.substringAfter("size:")
                    )
                )
            }
            val qualityTitle = arrayListOf<String>()
            val qualityUrl = arrayListOf<String>()
            val encryptInfo =
                "user=0&android_id=0&prod=kwplayer_ar_8.5.5.0&corp=kuwo&newver=3&vipver=8.5.5.0&source=kwplayer_ar_8.5.5.0_apk_keluze.apk&p2p=1&notrace=0&type=convert_url2&br=%sk%s&format=%s&sig=0&rid=%s&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=down"
            val url = "http://nmobi.kuwo.cn/mobi.s?f=kuwo&q=%s"
            musicQualityList.forEach {
                val lastFormatInfo = encryptInfo.format(
                    it.bitrate, it.format, it.size, mediaInfo.songInfo.songId
                )
                when (it.bitrate) {
                    "2000" -> {
                        qualityTitle.add("无损音质 (${it.format.uppercase()},${it.size.uppercase()})")
                        val encryptBytes = KuwoDES.encrypt2(
                            lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                            lastFormatInfo.length,
                            KuwoDES.SECRET_KEY,
                            KuwoDES.SECRET_KEY_LENG
                        )
                        val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                        qualityUrl.add(url.format(String(base64Coder)))
                    }

                    "320" -> {
                        qualityTitle.add("超品音质 (${it.format.uppercase()},${it.size.uppercase()})")
                        val encryptBytes = KuwoDES.encrypt2(
                            lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                            lastFormatInfo.length,
                            KuwoDES.SECRET_KEY,
                            KuwoDES.SECRET_KEY_LENG
                        )
                        val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                        qualityUrl.add(url.format(String(base64Coder)))
                    }

                    "128" -> {
                        qualityTitle.add("高品音质 (${it.format.uppercase()},${it.size.uppercase()})")
                        val encryptBytes = KuwoDES.encrypt2(
                            lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                            lastFormatInfo.length,
                            KuwoDES.SECRET_KEY,
                            KuwoDES.SECRET_KEY_LENG
                        )
                        val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                        qualityUrl.add(url.format(String(base64Coder)))
                    }

                    "48" -> {
                        qualityTitle.add("标准音质 (${it.format.uppercase()},${it.size.uppercase()})")
                        val encryptBytes = KuwoDES.encrypt2(
                            lastFormatInfo.toByteArray(Charset.forName("UTF-8")),
                            lastFormatInfo.length,
                            KuwoDES.SECRET_KEY,
                            KuwoDES.SECRET_KEY_LENG
                        )
                        val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                        qualityUrl.add(url.format(String(base64Coder)))
                    }

                }
            }
            binding.downCard.throttleClick {
                selector(qualityTitle.toList(), "选择音质") { dialogInterface, s, i ->
                    scopeNetLife {
                        val request =
                            Request.Builder().url(qualityUrl[i]).method("GET", null).build()
                        OkHttpClient().newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                runMain {
                                    toast(e.message)
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    response.body?.let {
                                        val data = it.string()
                                        loge(data)
                                        runMain {
                                            scopeNetLife {
                                                PopNotification.show(
                                                    "下载管理器",
                                                    "${mediaInfo.songInfo.songName}_${mediaInfo.songInfo.artist}.${musicQualityList[i].format} 开始下载。"
                                                )

                                                val getMusicFile = Get<File>(
                                                    data.substringAfter("url=")
                                                        .substringBefore("sig=")
                                                ) {
                                                    setDownloadDir(
                                                        publicAudiosDirPath
                                                    )
                                                    setDownloadFileNameConflict(true)
                                                    setDownloadMd5Verify(true)
                                                    setDownloadFileName("${mediaInfo.songInfo.songName}_${mediaInfo.songInfo.artist}.${musicQualityList[i].format}")
                                                }
                                                val resultMusicFile = getMusicFile.await()
                                                writeMusicInfo(
                                                    resultMusicFile,
                                                    mediaInfo.songInfo.songName,
                                                    mediaInfo.songInfo.artist,
                                                    mediaInfo.songInfo.artist
                                                )
                                                addToMediaStore(resultMusicFile)
                                                PopNotification.show(
                                                    "下载管理器",
                                                    "${mediaInfo.songInfo.songName}_${mediaInfo.songInfo.artist}.${musicQualityList[i].format} 下载成功。"
                                                )
                                            }.catch {
                                                PopNotification.show(
                                                    "下载管理器",
                                                    "${mediaInfo.songInfo.songName}_${mediaInfo.songInfo.artist}.${musicQualityList[i].format} 下载失败。"
                                                )
                                                it.printStackTrace()
                                                toast("下载失败")
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.search -> {
                inputDialog("搜索歌曲", "请输入歌曲名称或歌手名称搜索相关歌曲", "搜索") {
                    sendEvent(it, "search_music")
                }
            }

            R.id.download -> startActivity<DownloadActivity>()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_search_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
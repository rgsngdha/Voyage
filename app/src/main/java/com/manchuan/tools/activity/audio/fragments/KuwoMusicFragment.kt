package com.manchuan.tools.activity.audio.fragments

import androidx.core.graphics.drawable.toBitmap
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.engine.base.EngineFragment
import com.drake.net.Get
import com.drake.net.utils.runMain
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.internalPicturesDirPath
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.PopNotification
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.manchuan.tools.R
import com.manchuan.tools.activity.audio.model.KuwoSongInfo
import com.manchuan.tools.activity.audio.model.MediaInfo
import com.manchuan.tools.activity.audio.model.MusicQualityModel
import com.manchuan.tools.activity.audio.model.NewKuwoMusicModel
import com.manchuan.tools.activity.audio.utils.Base64Coder
import com.manchuan.tools.activity.audio.utils.KuwoDES
import com.manchuan.tools.activity.audio.utils.KwLyricHero
import com.manchuan.tools.databinding.FragmentKuwoMusicBinding
import com.manchuan.tools.databinding.ItemMusicBinding
import com.manchuan.tools.extensions.glideDrawable
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.publicAudiosDirPath
import com.manchuan.tools.extensions.saveImage
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.substringBetween
import com.manchuan.tools.extensions.writeMusicInfo
import com.manchuan.tools.json.SerializationConverter
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


class KuwoMusicFragment : EngineFragment<FragmentKuwoMusicBinding>(R.layout.fragment_kuwo_music) {

    private var page = 0

    override fun initData() {
        receiveEvent<String>("search_music") {
            binding.page.finish(false)
            binding.page.onRefresh {
                scope {
                    playerList.clear()
                    page = 0
                    binding.recyclerView.models = emptyList<NewKuwoMusicModel.Abslist>()
                    val data = Get<NewKuwoMusicModel>("https://search.kuwo.cn/r.s") {
                        param("pn", page)
                        param("rn", 100)
                        param("all", it)
                        param("ft", "music")
                        param("client", "kt")
                        param("rformat", "json")
                        param("encoding", "utf8")
                        param("vipver", "1")
                        param("mobi", "1")
                        converter = SerializationConverter("", "", "")
                    }.await()
                    addData(data.abslist, hasMore = {
                        if (index < (data.tOTAL.toInt()) / 30) true else if (data.tOTAL.toInt()
                                .rem(30) != 0
                        ) true else false
                    })
                    page++
                }.catch {
                    it.printStackTrace()
                }
            }.autoRefresh()
            binding.page.onLoadMore {
                scope {
                    playerList.clear()
                    val data = Get<NewKuwoMusicModel>("https://search.kuwo.cn/r.s") {
                        param("pn", page)
                        param("rn", 100)
                        param("all", it)
                        param("ft", "music")
                        param("client", "kt")
                        param("rformat", "json")
                        param("encoding", "utf8")
                        param("vipver", "1")
                        param("mobi", "1")
                        converter = SerializationConverter("", "", "")
                    }.await()
                    addData(data.abslist, hasMore = {
                        index < (data.tOTAL.toInt()) / 30
                    })
                    if ((data.tOTAL.toInt()) / 30 != page) {
                        page++
                    } else if (data.tOTAL.toInt().rem(30) != 0) {
                        page++
                    }
                    loge(page.toString())
                }.catch {
                    it.printStackTrace()
                }
            }
        }
    }

    override fun initView() {
        binding.recyclerView.grid(2).setup {
            addType<NewKuwoMusicModel.Abslist>(R.layout.item_music)
            onBind {
                val binding = getBinding<ItemMusicBinding>()
                val model = getModel<NewKuwoMusicModel.Abslist>()
                binding.name.text = model.sONGNAME
                binding.content.text = model.aRTIST
            }
            onLongClick(R.id.cardview) {
                selector(listOf("添加到播放队列", "下载"), "操作") { dialogInterface, s, i ->
                    val model = getModel<NewKuwoMusicModel.Abslist>()
                    when (s) {
                        "添加到播放队列" -> {
                            scopeNetLife {
                                val songInfo =
                                    Get<KuwoSongInfo>("http://m.kuwo.cn/newh5/singles/songinfoandlrc") {
                                        param("musicId", model.dCTARGETID)
                                        param("httpsStatus", 1)
                                        param("reqId", "969ba290-4b49-11eb-8db2-ebd372233623")
                                        converter = SerializationConverter("200", "status", "msg")
                                    }.await()
                                val encryptInfo =
                                    "user=0&android_id=0&prod=kwplayer_ar_8.5.5.0&corp=kuwo&newver=3&vipver=8.5.5.0&source=kwplayer_ar_8.5.5.0_apk_keluze.apk&p2p=1&notrace=0&type=convert_url2&br=100kaac&format=flac|mp3|aac&sig=0&rid=${
                                        songInfo.data.songinfo.musicrId.substringAfter(
                                            "MUSIC_"
                                        )
                                    }&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=down"
                                val encryptBytes = KuwoDES.encrypt2(
                                    encryptInfo.toByteArray(Charset.forName("UTF-8")),
                                    encryptInfo.length,
                                    KuwoDES.SECRET_KEY,
                                    KuwoDES.SECRET_KEY_LENG
                                )
                                val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                                val url =
                                    "http://nmobi.kuwo.cn/mobi.s?f=kuwo&q=${String(base64Coder)}"
                                val request = Request.Builder().url(url).method("GET", null).build()
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
                                                    val playingSongInfo = SongInfo(
                                                        model.dCTARGETID,
                                                        data.substringAfter("url=")
                                                            .substringBefore("sig="),
                                                        model.sONGNAME,
                                                        model.aRTIST,
                                                        songInfo.data.songinfo.pic,
                                                        BigDecimal.valueOf(songInfo.data.songinfo.duration.toLong())
                                                            .multiply(BigDecimal(1000L))
                                                            .longValueExact(),
                                                        lrclist = songInfo.data.lrclist.toMutableList(),
                                                        abslist = model
                                                    )
                                                    StarrySky.with().addPlayList(
                                                        mutableListOf(playingSongInfo)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                })
                                snack("添加到播放队列成功")
                                //sendEvent(playingSongInfo, "playing_song_info")
                            }.catch {
                                snack("添加到播放队列失败")
                                it.printStackTrace()
                            }
                        }

                        "下载" -> {
                            model.let { songInfo ->
                                //验证白名单成功
                                val musicQualityList = mutableListOf<MusicQualityModel>()
                                songInfo.mINFO.split(";")?.forEach { s ->
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
                                        it.bitrate, it.format, it.size, songInfo.dCTARGETID
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
                                                                    "${songInfo.sONGNAME}_${songInfo.aRTIST}.${musicQualityList[i].format} 开始下载。"
                                                                )
                                                                val songInfos =
                                                                    Get<KuwoSongInfo>("http://m.kuwo.cn/newh5/singles/songinfoandlrc") {
                                                                        param(
                                                                            "musicId",
                                                                            model.dCTARGETID
                                                                        )
                                                                        param("httpsStatus", 1)
                                                                        param(
                                                                            "reqId",
                                                                            "969ba290-4b49-11eb-8db2-ebd372233623"
                                                                        )
                                                                        converter =
                                                                            SerializationConverter(
                                                                                "200",
                                                                                "status",
                                                                                "msg"
                                                                            )
                                                                    }.await()
                                                                val getMusicFile = Get<File>(
                                                                    data.substringAfter("url=")
                                                                        .substringBefore("sig=")
                                                                ) {
                                                                    setDownloadDir(
                                                                        publicAudiosDirPath
                                                                    )
                                                                    setDownloadFileNameConflict(true)
                                                                    setDownloadMd5Verify(true)
                                                                    setDownloadFileName("${songInfo.sONGNAME}_${songInfo.aRTIST}.${musicQualityList[i].format}")
                                                                }
                                                                val resultMusicFile =
                                                                    getMusicFile.await()
                                                                writeMusicInfo(
                                                                    resultMusicFile,
                                                                    songInfo.sONGNAME,
                                                                    songInfo.aRTIST,
                                                                    songInfo.aRTIST,
                                                                )
                                                                context.addToMediaStore(
                                                                    resultMusicFile
                                                                )
                                                                PopNotification.show(
                                                                    "下载管理器",
                                                                    "${songInfo.sONGNAME}_${songInfo.aRTIST}.${musicQualityList[i].format} 下载成功。"
                                                                )
                                                            }.catch {
                                                                PopNotification.show(
                                                                    "下载管理器",
                                                                    "${songInfo.sONGNAME}_${songInfo.aRTIST}.${musicQualityList[i].format} 下载失败。"
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
            }
            R.id.cardview.onFastClick {
                val model = getModel<NewKuwoMusicModel.Abslist>()
                scopeNetLife {
                    val songInfo =
                        Get<KuwoSongInfo>("http://m.kuwo.cn/newh5/singles/songinfoandlrc") {
                            param("musicId", model.dCTARGETID)
                            param("httpsStatus", 1)
                            param("reqId", "969ba290-4b49-11eb-8db2-ebd372233623")
                            converter = SerializationConverter("200", "status", "msg")
                        }.await()
                    val encryptInfo =
                        "user=0&android_id=0&prod=kwplayer_ar_8.5.5.0&corp=kuwo&newver=3&vipver=8.5.5.0&source=kwplayer_ar_8.5.5.0_apk_keluze.apk&p2p=1&notrace=0&type=convert_url2&br=100kaac&format=flac|mp3|aac&sig=0&rid=${
                            songInfo.data.songinfo.musicrId.substringAfter(
                                "MUSIC_"
                            )
                        }&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=down????????????????"
                    val encryptBytes = KuwoDES.encrypt2(
                        encryptInfo.toByteArray(Charset.forName("UTF-8")),
                        encryptInfo.length,
                        KuwoDES.SECRET_KEY,
                        KuwoDES.SECRET_KEY_LENG
                    )
                    val base64Coder = Base64Coder.encode(encryptBytes).toCharArray()
                    val url = "http://nmobi.kuwo.cn/mobi.s?f=kuwo&q=${String(base64Coder)}"
                    val request = Request.Builder().url(url).method("GET", null).build()
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
                                        val lyricEntity =
                                            KwLyricHero.instance?.fillLrc(model.dCTARGETID) { lyric, translate ->
                                                val playingSongInfo = SongInfo(
                                                    model.dCTARGETID,
                                                    data.substringAfter("url=")
                                                        .substringBefore("sig="),
                                                    model.sONGNAME,
                                                    model.aRTIST,
                                                    songInfo.data.songinfo.pic,
                                                    BigDecimal.valueOf(songInfo.data.songinfo.duration.toDouble())
                                                        .multiply(BigDecimal(1000.00)).toLong(),
                                                    lrclist = songInfo.data.lrclist.toMutableList(),
                                                    abslist = model,
                                                    lyric = lyric.replace(
                                                        "\\[kuwo:(\\d+)\\]".toRegex(), ""
                                                    )?.replace("[kuwo:]", "")
                                                        ?.replace("[ver:v1.0]", ""),
                                                    translateLyric = translate
                                                )
                                                StarrySky.with().playMusicByInfo(
                                                    playingSongInfo
                                                )
                                                val musicInfo = MediaInfo(model, playingSongInfo)
                                                sendEvent(musicInfo, "playing_song_info")
                                            }
                                    }
                                }
                            }
                        }
                    })
                }.catch {
                    it.printStackTrace()
                }
            }
        }
    }

    private val playerList = mutableListOf<SongInfo>()

}
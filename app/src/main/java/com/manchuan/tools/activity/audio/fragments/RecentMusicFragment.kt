package com.manchuan.tools.activity.audio.fragments

import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.channel.sendEvent
import com.drake.engine.base.EngineFragment
import com.drake.net.Get
import com.drake.net.utils.runMain
import com.drake.net.utils.scope
import com.drake.net.utils.scopeLife
import com.dylanc.longan.toast
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.manchuan.tools.R
import com.manchuan.tools.activity.audio.model.KuwoSongInfo
import com.manchuan.tools.activity.audio.utils.Base64Coder
import com.manchuan.tools.activity.audio.utils.KuwoDES
import com.manchuan.tools.database.music.RecentMusicDatabase
import com.manchuan.tools.databinding.FragmentRecentMusicBinding
import com.manchuan.tools.databinding.ItemRecentMusicBinding
import com.manchuan.tools.extensions.ioScope
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.uiScope
import com.manchuan.tools.json.SerializationConverter
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.math.BigDecimal
import java.nio.charset.Charset


class RecentMusicFragment :
    EngineFragment<FragmentRecentMusicBinding>(R.layout.fragment_recent_music) {

    private val recentMusicDatabase by lazy {
        RecentMusicDatabase.getInstance(requireContext())
    }

    override fun initView() {
        with(binding) {
            recyclerView.setup {
                setAnimation(AnimationType.ALPHA)
                addType<SongInfo>(R.layout.item_recent_music)
                onBind {
                    val binding = getBinding<ItemRecentMusicBinding>()
                    val model = getModel<SongInfo>()
                    binding.cover.load(model.songCover, isCrossFade = true)
                    binding.title.text = model.songName
                    binding.artist.text = model.artist
                }
                R.id.item.onFastClick {
                    val model = getModel<SongInfo>()
                    scopeLife {
                        val songInfo =
                            Get<KuwoSongInfo>("http://m.kuwo.cn/newh5/singles/songinfoandlrc") {
                                param("musicId", model.songId)
                                param("httpsStatus", 1)
                                param("reqId", "969ba290-4b49-11eb-8db2-ebd372233623")
                                converter = SerializationConverter("", "", "")
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
                                        val data = it.string()/*
                                        loge(data)
                                        val decrypt = KuwoDES.b(
                                            Base64Coder.decode("QTTCEVWADWjGHNKyqOt6peSJECe9IlwYOThEXM42tOPUM09JJgqs4koq6HW+DmLo6NvDv+yKU0JVRFu8k+uReMgqO9c3DBQehRhuLv8hLwiRAcRvUqhAdgBiZRX9VKg739nvVkYYODS+8UeZJD8h7bH3LC47wUeiwiGV2y87hLCVTQibrzg3XnTw3qNdXC2bMihICLmHhbR3zVg4T5X7MfUAE2V0IVlaQ2ptuTAMA0uQSVyioY9BrMe99Wu56wWP0we7oREH/egw7RZ7MZeu6NR/cSqQlJnzVupFaDeTvUeDcjBVG8upjIDPUZA6mv1u7u2Mq9uUROM/eTr+3ZrL1G1wmU7dvhqEamNIiFPkHdE="),
                                            KuwoDES.SECRET_KEY
                                        )
                                        loge(String(decrypt,Charsets.UTF_8))*/
                                        runMain {
                                            val playingSongInfo = SongInfo(
                                                model.songId,
                                                data.substringAfter("url=").substringBefore("sig="),
                                                model.songName,
                                                model.artist,
                                                songInfo.data.songinfo.pic,
                                                BigDecimal.valueOf(songInfo.data.songinfo.duration.toLong())
                                                    .multiply(
                                                        BigDecimal(1000L)
                                                    ).longValueExact(),
                                                lrclist = songInfo.data.lrclist.toMutableList(),
                                                abslist = model.abslist,
                                                lyric = model.lyric,
                                                translateLyric = model.translateLyric
                                            )
                                            StarrySky.with().playMusicByInfo(
                                                playingSongInfo
                                            )
                                            sendEvent(playingSongInfo, "playing_song_info")
                                        }
                                    }
                                }
                            }
                        })
                    }.catch {
                        it.printStackTrace()
                        snack("请求失败 ${it.message}")
                    }
                }
                R.id.item.onLongClick {
                    val model = getModel<SongInfo>()
                    selector(listOf("删除"), "操作") { dialogInterface, s, i ->
                        when (s) {
                            "删除" -> {
                                runCatching {
                                    ioScope.launch {
                                        recentMusicDatabase.musicFlowDao().deleteMusic(
                                            model.songId
                                        )
                                    }
                                    binding.recyclerView.mutable.removeAt(
                                        absoluteAdapterPosition
                                    )
                                    notifyItemRemoved(absoluteAdapterPosition)
                                    snack("删除成功")
                                }.onFailure {
                                    snack("删除失败")
                                }
                            }
                        }
                    }
                }
            }
            page.onRefresh {
                scope {
                    binding.recyclerView.models = songInfo
                }
            }
        }
        recentMusicDatabase.musicFlowDao().queryAllMusicLiveData().observe(viewLifecycleOwner) {
            songInfo.clear()
            it.forEach { songInfoEntity ->
                songInfo.add(
                    SongInfo(
                        songInfoEntity.songId,
                        songInfoEntity.songUrl,
                        songInfoEntity.songName,
                        songInfoEntity.artist,
                        songInfoEntity.songCover,
                        lrclist = songInfoEntity.lrclist,
                        abslist = songInfoEntity.abslist,
                        lyric = songInfoEntity.lyric,
                        translateLyric = songInfoEntity.translateLyric
                    )
                )
            }
            uiScope.launch {
                binding.recyclerView.models = songInfo
            }
        }
    }

    val songInfo = mutableListOf<SongInfo>()

    override fun initData() {

    }

}
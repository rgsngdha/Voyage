package com.manchuan.tools.activity.audio

import android.graphics.Bitmap
import android.os.Bundle
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.layoutmanager.HoverLinearLayoutManager
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.net.Get
import com.drake.net.utils.runMain
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.internalPicturesDirPath
import com.dylanc.longan.isLightStatusBar
import com.dylanc.longan.toast
import com.gyf.immersionbar.ktx.immersionBar
import com.kongzue.dialogx.dialogs.PopNotification
import com.lxj.androidktx.FragmentStateAdapter
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.manchuan.tools.R
import com.manchuan.tools.activity.audio.fragments.MusicLyricFragment
import com.manchuan.tools.activity.audio.fragments.MusicPlayerFragment
import com.manchuan.tools.activity.audio.model.KuwoSongInfo
import com.manchuan.tools.activity.audio.model.MusicQualityModel
import com.manchuan.tools.activity.audio.utils.Base64Coder
import com.manchuan.tools.activity.audio.utils.KuwoDES
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityMusicPlayerBinding
import com.manchuan.tools.databinding.ItemsSpeaksBinding
import com.manchuan.tools.extensions.bitmapToFile
import com.manchuan.tools.extensions.glideDrawable
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.publicAudiosDirPath
import com.manchuan.tools.extensions.selector
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
import java.nio.charset.Charset

class MusicPlayerActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMusicPlayerBinding::inflate)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            transparentBar()
        }
        isLightStatusBar = true
        binding.recyclerView.linear(HoverLinearLayoutManager.VERTICAL).setup {
            addType<SongInfo>(R.layout.items_speaks)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val model = getModel<SongInfo>()
                val items = ItemsSpeaksBinding.bind(itemView)
                items.title.text = model.songName
                items.id.text = model.artist
                items.album.load(model.songCover, isCrossFade = true)
            }
            onClick(R.id.item) {
                StarrySky.with().playMusicById(getModel<SongInfo>(modelPosition).songId)
            }
            onLongClick(R.id.item) {
                val model = getModel<SongInfo>()
                selector(listOf("下载", "删除"), model.songName) { dialogInterface, s, i ->
                    when (s) {
                        "下载" -> {
                            scopeNetLife {
                                val songInfo =
                                    Get<KuwoSongInfo>("http://m.kuwo.cn/newh5/singles/songinfoandlrc") {
                                        param("musicId", model.songId)
                                        param("httpsStatus", 1)
                                        param("reqId", "969ba290-4b49-11eb-8db2-ebd372233623")
                                        converter = SerializationConverter("200", "status", "msg")
                                    }.await()
                                model.abslist?.let { songInfo ->
                                    //验证白名单成功
                                    val musicQualityList = mutableListOf<MusicQualityModel>()
                                    songInfo.mINFO.split(";").forEach { s ->
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
                                            it.bitrate, it.format, it.size, model.songId
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
                                            val request = Request.Builder().url(qualityUrl[i])
                                                .method("GET", null).build()
                                            OkHttpClient().newCall(request)
                                                .enqueue(object : Callback {
                                                    override fun onFailure(
                                                        call: Call,
                                                        e: IOException,
                                                    ) {
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
                                                                            "${model.songName}_${model.artist}.${musicQualityList[i].format} 开始下载。"
                                                                        )
                                                                        val getMusicFile =
                                                                            Get<File>(
                                                                                data.substringAfter(
                                                                                    "url="
                                                                                ).substringBefore(
                                                                                    "sig="
                                                                                )
                                                                            ) {
                                                                                setDownloadDir(
                                                                                    publicAudiosDirPath
                                                                                )
                                                                                setDownloadFileNameConflict(
                                                                                    true
                                                                                )
                                                                                setDownloadMd5Verify(
                                                                                    true
                                                                                )
                                                                                setDownloadFileName(
                                                                                    "${model.songName}_${model.artist}.${musicQualityList[i].format}"
                                                                                )
                                                                            }
                                                                        val cover = bitmapToFile(
                                                                            internalPicturesDirPath.toString(),
                                                                            glideDrawable(model.songCover)?.toBitmap(),
                                                                            100
                                                                        )
                                                                        val resultMusicFile =
                                                                            getMusicFile.await()
                                                                        writeMusicInfo(
                                                                            resultMusicFile,
                                                                            songInfo.sONGNAME,
                                                                            songInfo.aRTIST,
                                                                            songInfo.aRTIST,
                                                                            cover
                                                                        )
                                                                        addToMediaStore(
                                                                            resultMusicFile
                                                                        )
                                                                        PopNotification.show(
                                                                            "下载管理器",
                                                                            "${model.songName}_${model.artist}.${musicQualityList[i].format} 下载成功。"
                                                                        )
                                                                    }.catch {
                                                                        PopNotification.show(
                                                                            "下载管理器",
                                                                            "${model.songName}_${model.artist}.${musicQualityList[i].format} 下载失败。"
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

                        "删除" -> {
                            if (StarrySky.with().getPlayList().contains(model)) {
                                binding.recyclerView.mutable.removeAt(absoluteAdapterPosition)
                                binding.recyclerView.bindingAdapter.notifyItemRemoved(
                                    absoluteAdapterPosition
                                )
                                StarrySky.with()
                                    .updatePlayList(binding.recyclerView.mutable.toMutableList() as MutableList<SongInfo>)
                                toast("删除成功")
                            } else {
                                toast("在播放队列中无法找到该歌曲")
                            }
                        }
                    }
                }
            }
        }.models = StarrySky.with().getPlayList()
        binding.actionBars.addNavigationBarHeightToMarginBottom()
        binding.playListTitle.addStatusBarHeightToMarginTop()
        receiveEvent<String>("open_drawer") {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter =
            FragmentStateAdapter(MusicPlayerFragment(), MusicLyricFragment(), isLazyLoading = true)
        receiveEvent<Int>("change_page") {
            binding.viewPager.currentItem = it
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

}
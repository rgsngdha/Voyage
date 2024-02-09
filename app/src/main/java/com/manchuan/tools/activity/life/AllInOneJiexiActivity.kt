package com.manchuan.tools.activity.life

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNet
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.randomUUIDString
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.activity.life.model.AllInOne
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityAllInOneJiexiBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.user.timeMills
import com.mcxiaoke.koi.ext.addToMediaStore
import java.io.File

class AllInOneJiexiActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAllInOneJiexiBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "聚合解析"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        with(binding) {
            jiexi.throttleClick {
                if (url.isTextEmpty()) {
                    toast("链接不能为空")
                } else {
                    WaitDialog.show("解析中")
                    scopeNetLife {
                        val jiexi = Post<AllInOne>("http://47.242.159.250:8168/get") {
                            json("url" to url.textString)
                            setHeader(
                                "User-Agent",
                                "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                            )
                            addHeader("Origin", "http://jiexi.serachdazhu.icu")
                            addHeader("Referer", "http://jiexi.serachdazhu.icu/")
                            addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                            addHeader("Accept-Encoding", "gzip, deflate")
                            converter = SerializationConverter("200", "code", "msg")
                        }.await()
                        TipDialog.show("解析成功", WaitDialog.TYPE.SUCCESS)
                        alertDialog {
                            title = "下载"
                            message = jiexi.data.text.ifEmpty { "暂无内容" }
                            okButton(R.string.download) {
                                jiexi.data.medias.forEach {
                                    scopeNet {
                                        val getFile = Get<File>(it.resourceUrl) {
                                            setDownloadFileName(if (it.mediaType == "image") "$timeMills-$randomUUIDString.png" else if (it.mediaType == "music") "$timeMills-$randomUUIDString.mp3" else "$timeMills-$randomUUIDString.mp4")
                                            setDownloadDir(publicDownloadsDirPath)
                                            setDownloadFileNameConflict(true)
                                            setDownloadFileNameDecode(true)
                                            addDownloadListener(object : ProgressListener() {
                                                override fun onProgress(p: Progress) {

                                                }

                                            })
                                        }.await()
                                        addToMediaStore(file = getFile)
                                        toast("下载完成")
                                    }
                                }
                            }
                            cancelButton()
                        }.build()
                    }.catch {
                        it.printStackTrace()
                        TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
                        toast("解析失败")
                    }
                }
            }
        }
    }
}
package com.manchuan.tools.activity.video

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isWebUrl
import com.dylanc.longan.textString
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.postDelay
import com.lxj.androidktx.snackbar
import com.manchuan.tools.activity.video.model.PearKVideo
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityShortVideoBinding
import com.manchuan.tools.extensions.firstClipboardText
import com.manchuan.tools.extensions.inputDialog
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.substringBetween
import com.manchuan.tools.extensions.tryWith
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.user.timeMills
import com.mcxiaoke.koi.ext.addToMediaStore
import com.nowfal.kdroidext.kex.toast
import java.io.File

class ShortVideoActivity : BaseActivity() {
    private val shortVideoBinding by lazy {
        ActivityShortVideoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(shortVideoBinding.root)
        setSupportActionBar(shortVideoBinding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "聚合短视频解析"
            setDisplayHomeAsUpEnabled(true)
        }
        shortVideoBinding.linear2.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        shortVideoBinding.jiexi.setOnClickListener {
            if (shortVideoBinding.url.text.toString().isEmpty()) {
                snackbar("请输入链接")
            } else {
                shortVideoBinding.download.visibility = View.GONE
                normalDecode()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        postDelay(100) {
            tryWith {
                val isContainsVideo =
                    firstClipboardText().contains("v.douyin.com") or firstClipboardText().contains("v.kuaishou.com") or firstClipboardText().contains(
                        "xhslink.com"
                    )
                val videoPlatform =
                    if (firstClipboardText().contains("v.douyin.com")) "抖音" else if (firstClipboardText().contains(
                            "v.kuaishou.com"
                        )
                    ) "快手" else "小红书"
                val url = if (firstClipboardText().contains("v.douyin.com")) {
                    "https://v.douyin.com/${firstClipboardText().substringAfter("https://v.douyin.com/")}"
                } else if (firstClipboardText().contains("xhslink.com")) {
                    "https://xhslink.com/${
                        firstClipboardText().substringBetween(
                            "xhslink.com/", "，复制"
                        )
                    }"
                } else if (firstClipboardText().contains("v.kuaishou.com")) {
                    "https://v.kuaishou.com/${
                        firstClipboardText().subSequence(
                            24, 29
                        )
                    }"
                } else {
                    ""
                }.toString()
                if (isContainsVideo) {
                    inputDialog(
                        "聚合解析", "检测到来自「$videoPlatform」平台的链接", "解析", url
                    ) { inputStr ->
                        if (inputStr.isWebUrl()) {
                            normalDecode(inputStr)
                        } else {
                            toast("错误的链接")
                        }
                    }
                }
            }
        }
    }

    private fun normalDecode(url: String = shortVideoBinding.url.textString) {
        WaitDialog.show("解析中...")
        scopeNetLife {
            val content = Get<PearKVideo>("https://api.pearktrue.cn/api/video/api.php?url=$url") {
                converter = SerializationConverter("200", "code", "msg")
            }.await()
            TipDialog.show("解析成功", WaitDialog.TYPE.SUCCESS)
            shortVideoBinding.download.visibility = View.VISIBLE
            shortVideoBinding.download.setOnClickListener {
                shortVideoBinding.progressBar.visibility = View.VISIBLE
                shortVideoBinding.download.isClickable = false
                shortVideoBinding.download.isEnabled = false
                shortVideoBinding.progressBar.isIndeterminate = false
                scopeNetLife {
                    val file = Get<File>(content.data.url) {
                        setDownloadFileNameConflict(true)
                        setDownloadDir(publicDownloadsDirPath)
                        setDownloadFileName("$timeMills-Voyage.mp4")
                        setDownloadMd5Verify()
                        addDownloadListener(object : ProgressListener() {
                            override fun onProgress(p: Progress) {
                                shortVideoBinding.progressBar.post {
                                    supportActionBar?.apply {
                                        title = "已下载:" + p.currentSize()
                                    }
                                    shortVideoBinding.progressBar.setProgressCompat(
                                        p.progress(), true
                                    )
                                }
                            }
                        })
                    }.await()
                    supportActionBar?.apply {
                        title = "聚合短视频解析"
                    }
                    shortVideoBinding.progressBar.visibility = View.GONE
                    addToMediaStore(file)
                    TipDialog.show("下载成功", WaitDialog.TYPE.SUCCESS)
                    shortVideoBinding.download.isClickable = true
                    shortVideoBinding.download.isEnabled = true
                    snack("已保存到手机存储空间/Movies")
                }.catch {
                    shortVideoBinding.download.isClickable = false
                    shortVideoBinding.download.isEnabled = false
                    shortVideoBinding.progressBar.visibility = View.GONE
                    TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                }
            }
        }.catch {
            it.printStackTrace()
            TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
            shortVideoBinding.download.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
package com.manchuan.tools.activity.normal

import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.activity.json.LanzouCloud
import com.manchuan.tools.databinding.ActivityLanzouBinding
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.json.SerializationConverter
import java.io.File

class LanzouActivity : AppCompatActivity() {

    private val lanzouBinding by lazy {
        ActivityLanzouBinding.inflate(layoutInflater)
    }

    var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(lanzouBinding.root)
        setSupportActionBar(lanzouBinding.toolbar)
        immersive(lanzouBinding.toolbar)
        supportActionBar?.apply {
            title = "蓝奏云解析"
            setDisplayHomeAsUpEnabled(true)
        }
        lanzouBinding.jiexi.setOnClickListener {
            val string = lanzouBinding.url.text.toString()
            val password = lanzouBinding.password.text.toString()
            if (TextUtils.isEmpty(string)) {
                PopTip.show("请输入链接")
            } else {
                if (lanzouBinding.directDown.isChecked) {
                    lanzouBinding.progressBar.visibility = View.VISIBLE
                    lanzouBinding.progressBar.isIndeterminate = true
                    scopeNetLife {
                        val content =
                            Get<LanzouCloud>("https://api.kit9.cn/api/lanzouyun_netdisc/api.php?link=$string&pwd=$password") {
                                converter = SerializationConverter("200", "code", "msg")
                            }.await().data
                        lanzouBinding.jiexi.isEnabled = false
                        lanzouBinding.jiexi.isClickable = false
                        Get<File>(content.downloadLink) {
                            setDownloadDir(publicDownloadsDirPath)
                            setDownloadMd5Verify(true)
                            setDownloadFileNameConflict(true)
                            addDownloadListener(object : ProgressListener() {
                                override fun onProgress(p: Progress) {
                                    runOnUiThread {
                                        lanzouBinding.progressBar.isIndeterminate = false
                                        lanzouBinding.progressBar.setProgressCompat(
                                            p.progress(), true
                                        )
                                        supportActionBar?.apply {
                                            title = "已下载:" + p.currentSize()
                                        }
                                    }
                                }
                            })
                        }.await()
                        lanzouBinding.jiexi.isEnabled = true
                        lanzouBinding.jiexi.isClickable = true
                        lanzouBinding.progressBar.visibility = View.GONE
                        lanzouBinding.progressBar.isIndeterminate = false
                        supportActionBar?.apply {
                            title = "蓝奏云解析"
                        }
                        TipDialog.show("已保存到下载目录", WaitDialog.TYPE.SUCCESS)
                    }.catch {
                        lanzouBinding.jiexi.isEnabled = true
                        lanzouBinding.jiexi.isClickable = true
                        lanzouBinding.progressBar.visibility = View.GONE
                        lanzouBinding.progressBar.isIndeterminate = false
                        TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                    }
                } else if (!lanzouBinding.directDown.isChecked) {
                    scopeNetLife {
                        WaitDialog.show("解析中...")
                        val content =
                            Get<LanzouCloud>("https://api.kit9.cn/api/lanzouyun_netdisc/api.php?link=$string&pwd=$password") {
                                converter = SerializationConverter("200", "code", "msg")
                            }.await().data
                        TipDialog.show("解析完成", WaitDialog.TYPE.SUCCESS)
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("文件名:${content.fileName}")
                        stringBuilder.append("\n作者:${content.fileAuthor}")
                        stringBuilder.append("\n文件大小:${content.fileSize}")
                        stringBuilder.append("\n日期:${content.fileTime}")
                        stringBuilder.append("\n下载链接:${content.downloadLink}")
                        lanzouBinding.info.setText(stringBuilder)
                    }.catch {
                        PopTip.show(it.message)
                        TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
                    }
                }
            }
        }
        lanzouBinding.imageview1.setOnClickListener {
            if (lanzouBinding.info.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(lanzouBinding.info.text.toString())
                PopTip.show("已复制")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
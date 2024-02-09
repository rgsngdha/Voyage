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
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.activity.json.CowFile
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityCowFileBinding
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.json.SerializationConverter
import java.io.File

class CowFileActivity : BaseActivity() {

    private val binding by lazy {
        ActivityCowFileBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "奶牛快传解析"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.jiexi.setOnClickListener {
            val string = binding.url.text.toString()
            val password = binding.password.text.toString()
            if (TextUtils.isEmpty(string)) {
                PopTip.show("请输入链接")
            } else {
                if (binding.directDown.isChecked) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.isIndeterminate = true
                    scopeNetLife {
                        val content =
                            Get<CowFile>("https://api.kit9.cn/api/nainiu_netdisc/api.php?link=$string&pwd=$password") {
                                converter = SerializationConverter("200", "code", "msg")
                            }.await().data
                        binding.jiexi.isEnabled = false
                        binding.jiexi.isClickable = false
                        Get<File>(content.downloadLink) {
                            setDownloadDir(publicDownloadsDirPath)
                            setDownloadMd5Verify(true)
                            setDownloadFileNameConflict(true)
                            addDownloadListener(object : ProgressListener() {
                                override fun onProgress(p: Progress) {
                                    runOnUiThread {
                                        binding.progressBar.isIndeterminate = false
                                        binding.progressBar.setProgressCompat(
                                            p.progress(), true
                                        )
                                        supportActionBar?.apply {
                                            title = "已下载:" + p.currentSize()
                                        }
                                    }
                                }
                            })
                        }.await()
                        binding.jiexi.isEnabled = true
                        binding.jiexi.isClickable = true
                        binding.progressBar.visibility = View.GONE
                        binding.progressBar.isIndeterminate = false
                        supportActionBar?.apply {
                            title = "奶牛快传解析"
                        }
                        TipDialog.show("已保存到下载目录", WaitDialog.TYPE.SUCCESS)
                    }.catch {
                        binding.jiexi.isEnabled = true
                        binding.jiexi.isClickable = true
                        binding.progressBar.visibility = View.GONE
                        binding.progressBar.isIndeterminate = false
                        TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                    }
                } else if (!binding.directDown.isChecked) {
                    scopeNetLife {
                        WaitDialog.show("解析中...")
                        val content =
                            Get<CowFile>("https://api.kit9.cn/api/nainiu_netdisc/api.php?link=$string&pwd=$password") {
                                converter = SerializationConverter("200", "code", "msg")
                            }.await().data
                        TipDialog.show("解析完成", WaitDialog.TYPE.SUCCESS)
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("文件名:${content.fileName}")
                        stringBuilder.append("\n上传文件描述:${content.fileDescribe}")
                        stringBuilder.append("\n文件过期时间:${content.fileExpireAt}")
                        stringBuilder.append("\n上传文件格式:${content.fileFormat}")
                        stringBuilder.append("\n上传文件大小:${content.fileSize}")
                        stringBuilder.append("\n文件上传时间:${content.fileTime}")
                        stringBuilder.append("\n文件下载直链:${content.downloadLink}")
                        binding.info.setText(stringBuilder)
                    }.catch {
                        PopTip.show(it.message)
                        TipDialog.show("解析失败", WaitDialog.TYPE.ERROR)
                    }
                }
            }
        }
        binding.imageview1.setOnClickListener {
            if (binding.info.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(binding.info.text.toString())
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
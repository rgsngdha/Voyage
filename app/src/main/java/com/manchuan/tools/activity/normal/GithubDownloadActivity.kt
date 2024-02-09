package com.manchuan.tools.activity.normal

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityGithubDownloadBinding
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.urlDecoded
import java.io.File


class GithubDownloadActivity : BaseActivity() {
    private val githubBinding by lazy {
        ActivityGithubDownloadBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(githubBinding.root)
        setSupportActionBar(githubBinding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Github下载加速"
        githubBinding.materialbutton1.setOnClickListener {
            if (githubBinding.url.isTextNotEmpty()) {
                githubBinding.progressBar.visibility = View.VISIBLE
                githubBinding.progressBar.isIndeterminate = true
                scopeNetLife {
                    val file =
                        Get<File>(("https://ghproxy.com/${githubBinding.url.textString}").urlDecoded()) {
                            setDownloadDir(publicDownloadsDirPath)
                            setDownloadMd5Verify()
                            setDownloadFileNameConflict(true)
                            addDownloadListener(object : ProgressListener() {
                                override fun onProgress(p: Progress) {
                                    githubBinding.progressBar.post {
                                        githubBinding.progressBar.isIndeterminate = false
                                        githubBinding.progressBar.setProgressCompat(
                                            p.progress(), true
                                        )
                                    }
                                }
                            })
                        }.await()
                    githubBinding.progressBar.visibility = View.GONE
                    githubBinding.progressBar.isIndeterminate = false
                    TipDialog.show("已保存到下载目录", WaitDialog.TYPE.SUCCESS)
                }.catch {
                    githubBinding.progressBar.visibility = View.GONE
                    githubBinding.progressBar.isIndeterminate = false
                    TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                }
            } else {
                snack("请输入文件地址")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
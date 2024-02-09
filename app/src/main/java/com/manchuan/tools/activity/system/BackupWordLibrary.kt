package com.manchuan.tools.activity.system

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.room.util.newStringBuilder
import com.drake.statusbar.immersive
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.okButton
import com.dylanc.longan.doOnBackPressed
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityBackupWordLibraryBinding
import com.manchuan.tools.extensions.copyFileFromAssets
import com.manchuan.tools.utils.KeepShellAsync
import com.manchuan.tools.utils.RootUtil
import java.io.File


class BackupWordLibrary : AppCompatActivity() {
    private val binding by lazy {
        ActivityBackupWordLibraryBinding.inflate(layoutInflater)
    }

    private val console = newStringBuilder()
    private var isBackUp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "备份字库"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(binding.toolbar)
        if (!RootUtil.isDeviceRooted) {
            BaseAlertDialogBuilder(this).setTitle("警告").setMessage("设备无ROOT，无法使用该功能")
                .setCancelable(false).setPositiveButton("确定") { dialog, which ->
                    this.finish()
                }.show()
        } else {
            runCatching {
                copyFileFromAssets(
                    "wordlibrary.sh",
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS,
                    "wordlibrary.sh"
                )
                val folder =
                    File(Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "images_backup")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
            }
            BaseAlertDialogBuilder(this).setTitle("提示")
                .setMessage("确定要开始备份吗？如果在备份过程中出现任何问题，概不负责。")
                .setCancelable(false).setPositiveButton("确定") { dialog, which ->
                    val handler = object : Handler(mainLooper) {

                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)
                            when (msg.what) {
                                1 -> {
                                    isBackUp = true
                                    val bundle = msg.obj.toString()
                                    console.append("$bundle\n")
                                    binding.resultText.text = console
                                    binding.scroll.post {
                                        binding.scroll.fullScroll(View.FOCUS_DOWN)
                                    }
                                }
                            }
                        }

                    }
                    KeepShellAsync.getInstance("backup_sdc").apply {
                        doCmd("sh /sdcard/Download/wordlibrary.sh")
                        setHandler(handler)
                    }
                }.show()
        }
        doOnBackPressed {
            if (isBackUp) {
                alertDialog {
                    title = "提示"
                    message = "当前正在备份中，确定要返回取消备份吗？"
                    okButton {
                        finish()
                    }
                    cancelButton {

                    }
                }.show()
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        KeepShellAsync.destoryInstance("backup_sdc")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
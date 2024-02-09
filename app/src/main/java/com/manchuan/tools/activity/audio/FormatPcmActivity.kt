package com.manchuan.tools.activity.audio

import android.os.Bundle
import androidx.activity.addCallback
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.drake.engine.utils.throttleClick
import com.drake.net.utils.runMain
import com.dylanc.longan.activityresult.launch
import com.dylanc.longan.activityresult.registerForOpenDocumentResult
import com.dylanc.longan.internalMusicDirPath
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityFormatPcmBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.user.timeMills
import com.mcxiaoke.koi.ext.addToMediaStore
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class FormatPcmActivity : BaseActivity() {

    private val binding by lazy {
        ActivityFormatPcmBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "音频提取PCM"
            setDisplayHomeAsUpEnabled(true)
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
        binding.colorPicker.throttleClick {
            openDocumentLauncher.launch("audio/*")
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snack("未选择文件或文件路径为空")
            } else {
                getPCM()
            }
        }
    }

    private val openDocumentLauncher = registerForOpenDocumentResult { uri ->
        if (uri != null) {
            // 处理 uri
            path = getPath(uri)
            binding.colorString.text = "已选择"
        }
    }


    private fun getPCM() {
        val targetPath = internalMusicDirPath + File.separator + "$timeMills${File.separator}"
        if (!File(targetPath).exists()) {
            File(targetPath).mkdirs()
        }
        ioScope.launch {
            val commands =
                "ffmpeg -i $path -ar 48000 -ac 2 -f s16le $targetPath$timeMills-Voyage.pcm"
            FFmpegCommand.runCmd(arrayOf(commands), callback(targetPath))
        }
    }

    private val commandResult = StringBuilder()

    private fun callback(targetPath: String?): CommonCallBack {
        return object : CommonCallBack() {
            override fun onStart() {
                runOnUiThread {
                    WaitDialog.show("别急,正在准备中...")
                }
            }

            override fun onComplete() {
                Timber.tag("FFmpegCmd").d("onComplete")
                runOnUiThread {
                    TipDialog.show("处理完成", WaitDialog.TYPE.SUCCESS)
                    commandResult.append("处理完成,已保存到:$targetPath")
                    binding.autocomplete1.setText(commandResult)
                    targetPath?.let { File(it) }?.let { addToMediaStore(it) }
                }
                commandResult.clear()
            }

            override fun onCancel() {
                runOnUiThread {
                    toast("用户取消")
                }
                Timber.tag("FFmpegCmd").d("Cancel")
            }

            override fun onProgress(progress: Int, pts: Long) {
                val duration: Int? = FFmpegCommand.getMediaInfo(path, MediaAttribute.DURATION)
                val progressN = pts / duration!!
                Timber.tag("FFmpegCmd").d("%s", progress.toString())
                commandResult.append("\n已处理:$progress%")
                runOnUiThread {
                    WaitDialog.show("已处理 $progress%", progressN.toFloat())
                }
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                Timber.tag("FFmpegCmd").e("%s", errorMsg)
                runMain {
                    TipDialog.show("处理失败", WaitDialog.TYPE.ERROR)
                    if (path.contains(" ")) {
                        alertDialog {
                            title = "警告"
                            message = "请检查选择的文件及父文件夹是否含有空格"
                            okButton { }
                        }.build()
                    }
                }
            }
        }
    }

    private var path: String = ""
    private var targetPath = ""

}
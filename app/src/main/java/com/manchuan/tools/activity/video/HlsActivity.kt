package com.manchuan.tools.activity.video

import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.coder.ffmpeg.utils.FFmpegUtils
import com.drake.statusbar.immersive
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.randomUUIDString
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.snackbar
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityHlsBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.snack
import com.mcxiaoke.koi.log.loge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class HlsActivity : BaseActivity() {

    private val binding by lazy {
        ActivityHlsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLayout.toolbar)
        immersive(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "视频切片"
        }
        binding.colorPicker.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))

        }
        binding.create.setOnClickListener {
            if (path.isEmpty() or binding.time.isTextEmpty()) {
                snackbar("未选择文件或切割时间为空")
            } else {
                video2HLS()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun video2HLS() {
        val dir = File(
            com.manchuan.tools.extensions.publicMoviesDirPath + File.separator + "hls",
            randomUUIDString
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val targetPath = dir.toString() + File.separator + "${
            File(path).name.replace(
                File(path).extension,
                ""
            )
        }.m3u8"
        GlobalScope.launch {
            FFmpegCommand.runCmd(
                FFmpegUtils.video2HLS(
                    path, targetPath, binding.time.textString.toInt()
                ), callback(dir.absolutePath)
            )
        }
    }


    private var path: String = ""

    private val commandResult = StringBuilder()

    private fun callback(targetPath: String?): CommonCallBack {
        return object : CommonCallBack() {
            override fun onStart() {
                loge("切片处理", "已开始")
                runOnUiThread {
                    WaitDialog.show("别急,正在准备中...")
                }
            }

            override fun onComplete() {
                Timber.tag("FFmpegCmd").d("onComplete")
                runOnUiThread {
                    TipDialog.show("切片处理完成", WaitDialog.TYPE.SUCCESS)
                    commandResult.append("处理完成,已保存到:$targetPath")
                    binding.autocomplete1.setText(commandResult)
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
                var progressN = pts / duration!!
                Timber.tag("FFmpegCmd").d("%s", progress.toString())
                commandResult.append("\n已处理:$progress%")
                runOnUiThread {
                    WaitDialog.show("已处理 $progress%", progress.toFloat())
                }
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                Timber.tag("FFmpegCmd").e("%s", errorMsg)
                runOnUiThread {
                    TipDialog.show("切片处理失败", WaitDialog.TYPE.ERROR)
                }
            }
        }
    }


    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                path = getPath(uri)
                binding.colorString.text = "已选择"
            } else {
                snack("选择已取消")
            }
        }


}
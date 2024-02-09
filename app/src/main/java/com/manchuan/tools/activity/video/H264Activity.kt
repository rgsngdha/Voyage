package com.manchuan.tools.activity.video

import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.coder.ffmpeg.utils.FFmpegUtils
import com.dylanc.longan.randomUUIDString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.snackbar
import com.manchuan.tools.databinding.ActivityH264Binding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.publicMoviesDirPath
import com.manchuan.tools.extensions.snack
import com.mcxiaoke.koi.log.loge
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.File

class H264Activity : AppCompatActivity() {

    private val binding by lazy {
        ActivityH264Binding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbarLayout.toolbar.apply {
            title = "视频编码H264"
            setNavigationOnClickListener {
                finish()
            }
        }
        binding.colorPicker.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snackbar("未选择文件")
            } else {
                yuv2H264()
            }
        }
    }


    private fun yuv2H264() {
        val dir = File(publicMoviesDirPath + File.separator + "H264", randomUUIDString)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val targetPath = dir.toString() + File.separator + "${System.currentTimeMillis()}.h264"
        runBlocking {
            launch {
                FFmpegCommand.runCmd(
                    FFmpegUtils.yuv2H264(
                        path,
                        targetPath,
                        FFmpegCommand.getMediaInfo(path, MediaAttribute.WIDTH)!!,
                        FFmpegCommand.getMediaInfo(path, MediaAttribute.HEIGHT)!!
                    ), callback(targetPath)
                )
            }
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
                    TipDialog.show("处理完成", WaitDialog.TYPE.SUCCESS)
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
                Timber.tag("FFmpegCmd").d("%s", progress.toString())
                commandResult.append("\n已处理:$progress%")
                runOnUiThread {
                    WaitDialog.show("已处理 $progress%", progress.toFloat())
                }
            }

            override fun onError(errorCode: Int, errorMsg: String?) {
                Timber.tag("FFmpegCmd").e("%s", errorMsg)
                runOnUiThread {
                    TipDialog.show("处理失败", WaitDialog.TYPE.ERROR)
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
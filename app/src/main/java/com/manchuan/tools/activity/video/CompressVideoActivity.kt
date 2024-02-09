package com.manchuan.tools.activity.video

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.dylanc.longan.externalMoviesDirPath
import com.dylanc.longan.textString
import com.dylanc.longan.toUri
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.snackbar
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityCompressVideoBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.publicMoviesDirPath
import com.manchuan.tools.extensions.snack
import com.mcxiaoke.koi.ext.mediaScan
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class CompressVideoActivity : BaseActivity() {
    private val binding by lazy {
        ActivityCompressVideoBinding.inflate(layoutInflater)
    }

    private val formats = arrayOf(
        "MP4",
        "WMV",
        "MPEG",
        "M4V",
        "MOV",
        "FLV",
        "AVI",
        "MKV",
    )

    private val present = arrayOf(
        "ultrafast",
        "superfast",
        "veryfast",
        "faster",
        "fast",
        "medium",
        "slow",
        "slower",
        "veryslow",
        "placebo"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val formats = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, formats)
        binding.formats.setAdapter(formats)
        val presents = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, present)
        binding.bytesInput.setAdapter(presents)
        supportActionBar?.apply {
            title = "压缩视频"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.colorPicker.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snackbar("未选择文件或文件路径为空")
            } else {
                compressVideo()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun compressVideo() {
        val targetPath =
            publicMoviesDirPath + File.separator + "${System.currentTimeMillis()}${File.separator}"
        if (!File(targetPath).exists()) {
            File(targetPath).mkdirs()
        }
        GlobalScope.launch {
            val commands =
                "ffmpeg -i $path -c:v libx264 -preset ${binding.bytesInput.textString} -crf ${binding.crf.textString} $targetPath${
                    File(path).name
                }.${binding.formats.textString.lowercase()}"
            FFmpegCommand.runCmd(arrayOf(commands), callback(targetPath))
        }
    }

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
                    mediaScan(File(targetPath).toUri())
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
                runOnUiThread {
                    TipDialog.show("处理失败", WaitDialog.TYPE.ERROR)
                }
            }
        }
    }

    private val commandResult = StringBuilder()


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

    private var path = ""

}
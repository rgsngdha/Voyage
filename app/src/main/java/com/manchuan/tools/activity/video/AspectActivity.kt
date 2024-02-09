package com.manchuan.tools.activity.video

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.lxj.androidktx.snackbar
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityAspectBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.publicMoviesDirPath
import com.manchuan.tools.extensions.snack
import com.mcxiaoke.koi.ext.addToMediaStore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class AspectActivity : BaseActivity() {

    private val binding by lazy {
        ActivityAspectBinding.inflate(layoutInflater)
    }


    private val formats = arrayOf(
        "16:9",
        "4:3",
        "16:10",
        "5:4",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "设置视频屏幕高宽比"
            setDisplayHomeAsUpEnabled(true)
        }
        val formats = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, formats)
        binding.bits.setAdapter(formats)
        binding.colorPicker.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snackbar("未选择文件或文件路径为空")
            } else {
                videoAspect()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun videoAspect() {
        val targetPath =
            publicMoviesDirPath + File.separator
        if (!File(targetPath).exists()) {
            File(targetPath).mkdirs()
        }
        GlobalScope.launch {
            val commands =
                "ffmpeg -i $path -aspect ${binding.bits.textString} $targetPath${File(path).name}"
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
                    addToMediaStore(File(targetPath))
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

    private var path: String = ""

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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
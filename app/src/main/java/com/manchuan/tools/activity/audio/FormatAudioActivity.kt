package com.manchuan.tools.activity.audio

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.coder.ffmpeg.annotation.MediaAttribute
import com.coder.ffmpeg.call.CommonCallBack
import com.coder.ffmpeg.jni.FFmpegCommand
import com.dylanc.longan.activityresult.launch
import com.dylanc.longan.activityresult.registerForOpenDocumentResult
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.textString
import com.dylanc.longan.toUri
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.snackbar
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityFormatAudioBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.publicAudiosDirPath
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.user.timeMills
import com.mcxiaoke.koi.ext.mediaScan
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class FormatAudioActivity : BaseActivity() {

    private val binding by lazy {
        ActivityFormatAudioBinding.inflate(layoutInflater)
    }

    private val formats = arrayOf(
        "MP3",
        "FLAC",
        "AAC",
        "AC3",
        "WAV",
        "WMA",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val formats = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, formats)
        binding.formats.setAdapter(formats)
        supportActionBar?.apply {
            title = "音频格式转换"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        binding.colorPicker.setOnClickListener {
            pickMedia.launch("audio/*")
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snackbar("未选择文件或文件路径为空")
            } else {
                formatAudio()
            }
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
    }


    private var path = ""


    private val pickMedia = registerForOpenDocumentResult { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            path = getPath(uri)
            binding.colorString.text = "已选择"
        } else {
            snack("选择已取消")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun formatAudio() {
        val targetPath = publicAudiosDirPath + File.separator + "${timeMills}${File.separator}"
        if (!File(targetPath).exists()) {
            File(targetPath).mkdirs()
        }
        GlobalScope.launch {
            val commands =
                "ffmpeg -i $path -f ${binding.formats.textString.lowercase()} $targetPath${
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

    private val commandResult = StringBuilder()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }


}
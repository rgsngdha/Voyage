package com.manchuan.tools.activity.life

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.net.utils.runMain
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityAiStoryBinding
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.textCopyThenPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import kotlin.concurrent.thread


class AiStoryActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAiStoryBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        with(binding) {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                title = "AI故事生成"
                setDisplayHomeAsUpEnabled(true)
            }
            enableTransitionTypes(layout)
            create.throttleClick {
                if (time.isTextNotEmpty()) {
                    WaitDialog.show("生成中")
                    thread {
                        val client = OkHttpClient()
                        val request: Request = Request.Builder()
                            .url("https://yf.sgai.cc/gptcms/api/rolechat/send?model_id=11&message=${time.textString}")
                            .addHeader("Wid", "1")
                            .addHeader("Token", "25d28cca-aea3-11ee-9f65-0242d1ee50c3")
                            .method("GET", body = null).build()
                        client.newCall(request).enqueue(object : Callback {

                            override fun onFailure(call: Call, e: IOException) {
                                runMain {
                                    WaitDialog.dismiss()
                                }
                            }

                            override fun onResponse(call: Call, response: Response) {
                                if (response.isSuccessful) {
                                    runBlocking(Dispatchers.IO) {
                                        val responseBody = response.body
                                        if (responseBody != null) {
                                            val bufferedReader =
                                                BufferedReader(responseBody.charStream())
                                            var line = bufferedReader.readLine()
                                            var index = 0
                                            val sb = StringBuilder()
                                            while (line != null) {
                                                val msg = line
                                                if (msg != null) {
                                                    sb.append(msg)
                                                }
                                                line = bufferedReader.readLine()
                                            }
                                            runOnUiThread {
                                                WaitDialog.dismiss()
                                                result.text = sb.toString().replace("data:", "")
                                                    .replace("<br/><br/>", "\n\n    ")
                                                textCopyThenPost(result.textString)
                                            }
                                            //callback.onCallBack(sb.toString(), true)
                                        }
                                    }
                                }
                            }
                        })
                    }
                } else {
                    toast("请先输入故事主题")
                }
            }
        }
    }
}
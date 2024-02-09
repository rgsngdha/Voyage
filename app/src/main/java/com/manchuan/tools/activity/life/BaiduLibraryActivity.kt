package com.manchuan.tools.activity.life

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.itxca.spannablex.spannable
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.activity.life.model.BaiduLibrary
import com.manchuan.tools.activity.life.model.BaiduLibraryTwo
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityBaiduLibraryBinding
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.json
import com.manchuan.tools.json.SerializationConverter

class BaiduLibraryActivity : BaseActivity() {

    private val binding by viewBinding(ActivityBaiduLibraryBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        with(binding) {
            setSupportActionBar(toolbar)
            enableTransitionTypes(layout)
            supportActionBar?.apply {
                title = "百度题库"
                setDisplayHomeAsUpEnabled(true)
            }
            create.throttleClick {
                if (content.isTextNotEmpty()) {
                    scopeNetLife {
                        WaitDialog.show("查询中")
                        val response = Get<String>("https://api.pearktrue.cn/api/baidutiku/") {
                            converter = SerializationConverter("200", "code", "msg")
                            param("question", content.textString)
                        }.await()
                        WaitDialog.dismiss()
                        try {
                            val results = json.decodeFromString<BaiduLibrary>(response)
                            result.text = spannable {
                                results.data.question.text()
                                newline(2)
                                for (line in results.data.options.toTypedArray().contentToString()
                                    .replace("[", "").replace("]", "").split(",")) {
                                    line.replace(" ", "").text()
                                    newline()
                                }
                                if (results.data.answer != null) {
                                    newline()
                                    "参考答案:${results.data.answer}".text()
                                }
                            }
                        } catch (e: Exception) {
                            val results = json.decodeFromString<BaiduLibraryTwo>(response)
                            result.text = spannable {
                                results.data.question.text()
                                newline(2)
                                results.data.options.text()
                                if (results.data.answer != null) {
                                    newline()
                                    "参考答案:${results.data.answer}".text()
                                }
                            }
                        }
                    }.catch {
                        WaitDialog.dismiss()
                        toast(it.message)
                    }
                } else {
                    toast("题目内容不能为空")
                }
            }
        }
    }
}
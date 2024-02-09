package com.manchuan.tools.activity.life

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.manchuan.tools.activity.video.model.Dog
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityDogParagraphBinding
import com.manchuan.tools.extensions.textCopyThenPost
import com.manchuan.tools.json.SerializationConverter

class DogParagraphActivity : BaseActivity() {

    private val binding by viewBinding(ActivityDogParagraphBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        with(binding) {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                title = "狗屁不通文章生成器"
                setDisplayHomeAsUpEnabled(true)
            }
            make.throttleClick {
                scopeNetLife {
                    val result = Get<Dog>("https://api.52vmy.cn/api/wl/s/dog") {
                        param("msg", url.textString)
                        if (num.isTextNotEmpty()) param("num", num.textString)
                        converter = SerializationConverter("200", "code", "msg")
                    }.await()
                    textCopyThenPost(result.data)
                }
            }
        }
    }
}
package com.manchuan.tools.activity.images

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.utils.scopeDialog
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.manchuan.tools.activity.life.model.AiAvatar
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityAiAvatarBinding
import com.manchuan.tools.databinding.ActivityAiPaintBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.savePic
import com.manchuan.tools.json.SerializationConverter

class AiAvatarActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAiAvatarBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        with(binding) {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                title = "AI头像生成"
                setDisplayHomeAsUpEnabled(true)
            }
            make.throttleClick {
                if (url.isTextNotEmpty()) {
                    scopeDialog {
                        val avatar = Get<AiAvatar>("https://api.pearktrue.cn/api/aiheadportrait/") {
                            param("prompt", url.textString)
                            converter = SerializationConverter("200", "code", "msg")
                        }.await()
                        image.animateVisible()
                        save.animateVisible()
                        image.load(avatar.imgurl)
                        save.throttleClick {
                            savePic(avatar.imgurl)
                        }
                    }
                } else {
                    save.animateGone()
                    image.animateGone()
                    toast("关键词不能为空")
                }
            }
        }
    }
}
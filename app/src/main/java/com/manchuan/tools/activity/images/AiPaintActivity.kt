package com.manchuan.tools.activity.images

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.manchuan.tools.extensions.load
import com.manchuan.tools.activity.images.models.AiPaintModel
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityAiPaintBinding
import com.manchuan.tools.extensions.savePic
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.utils.UiUtils
import java.util.concurrent.TimeUnit

class AiPaintActivity : BaseActivity() {

    private val binding by lazy {
        ActivityAiPaintBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "AI绘图"
        }
        immerseStatusBar(!UiUtils.isDarkMode())
        binding.aiPaint.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.scroller.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.toolbar.addStatusBarHeightToMarginTop()
        binding.textField.setEndIconOnClickListener {
            if (binding.editText.isTextNotEmpty()) {
                scopeNetLife {
                    snack("已向服务器提交请求，请耐心等待生成。")
                    binding.scroller.animateGone()
                    binding.lottie.animateVisible()
                    val models =
                        Get<AiPaintModel>("https://api.wer.plus/api/aiw?pra=${binding.editText.textString}") {
                            converter = SerializationConverter("200", "code", "")
                            setClient {
                                connectTimeout(5, TimeUnit.MINUTES)
                                callTimeout(5, TimeUnit.MINUTES)
                                readTimeout(5, TimeUnit.MINUTES)
                                writeTimeout(5, TimeUnit.MINUTES)
                            }
                        }.await()
                    binding.lottie.animateGone()
                    binding.scroller.animateVisible()
                    binding.image.load(
                        models.url, roundRadius = 16, isCrossFade = true, isForceOriginalSize = true
                    )
                    binding.save.setOnClickListener {
                        savePic(models.url)
                    }
                }.catch {
                    it.printStackTrace()
                    snack("生成失败")
                    binding.lottie.animateGone()
                    binding.scroller.animateGone()
                }
            } else {
                snack("请先输入描述后再生成...")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
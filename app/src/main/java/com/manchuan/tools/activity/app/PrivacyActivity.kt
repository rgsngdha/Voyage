package com.manchuan.tools.activity.app

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import com.drake.channel.sendEvent
import com.drake.net.Get
import com.drake.net.cache.CacheMode
import com.drake.net.time.Interval
import com.drake.net.time.IntervalStatus
import com.drake.net.utils.scopeDialog
import com.dylanc.longan.context
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.toast
import com.lxj.androidktx.core.disable
import com.lxj.androidktx.core.enable
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.visible
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPrivacyBinding
import io.noties.markwon.Markwon
import java.util.concurrent.TimeUnit

class PrivacyActivity : BaseActivity() {

    private val binding by lazy {
        ActivityPrivacyBinding.inflate(layoutInflater)
    }

    val type = safeIntentExtras<Int>("type")

    private val interval by lazy {
        Interval(10, 1, TimeUnit.SECONDS).subscribe {
            binding.ctl.subtitle = "还有${10 - count.toInt()}秒结束"
            binding.agree.disable()
        }.finish {
            binding.ctl.subtitle = "已完成"
            binding.agree.enable()
            onBackPressedDispatcher.addCallback {
                finish()
                sendEvent(binding.agree.isChecked, "agree${type.value}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "隐私政策与用户协议"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        val isGuide = safeIntentExtras<Boolean>("isGuide").value
        when (type.value) {
            1 -> {
                binding.agree.text = "我已阅读并同意《隐私政策》"
                scopeDialog {
                    val markdown = Markwon.create(context)
                    val string = Get<String>("https://user.zhongyi.team/privacy.md") {
                        setCacheMode(CacheMode.REQUEST_THEN_READ)
                    }.await()
                    binding.ctl.apply {
                        title = "隐私政策"
                    }
                    markdown.setMarkdown(binding.content, string)
                }.catch {
                    toast("获取失败")
                }
            }

            2 -> {
                binding.agree.text = "我已阅读并同意《用户协议》"
                scopeDialog {
                    val markdown = Markwon.create(context)
                    val string = Get<String>("https://user.zhongyi.team/用户协议.md") {
                        setCacheMode(CacheMode.REQUEST_THEN_READ)
                    }.await()
                    binding.ctl.apply {
                        title = "用户协议"
                    }
                    markdown.setMarkdown(binding.content, string)
                }.catch {
                    toast("获取失败")
                }
            }
        }
        if (!isGuide) {
            binding.agree.gone()
        } else {
            binding.agree.visible()
            interval.start()
            onBackPressedDispatcher.addCallback {
                finish()
                sendEvent(binding.agree.isChecked, "agree${type.value}")
            }
        }
        binding.agree.addOnCheckedStateChangedListener { materialCheckBox, i ->
            if (materialCheckBox.isChecked) {
                finish()
                sendEvent(binding.agree.isChecked, "agree${type.value}")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (safeIntentExtras<Boolean>("isGuide").value) {
                    when (interval.state) {
                        IntervalStatus.STATE_IDLE -> {
                            finish()
                        }

                        IntervalStatus.STATE_ACTIVE -> {
                            toast("等待计时结束后才可退出")
                        }

                        IntervalStatus.STATE_PAUSE -> {
                            toast("计时已暂停，请重新阅读${if (type.value == 1) "隐私政策" else "用户协议"}")
                        }
                    }
                }
                sendEvent(binding.agree.isChecked, "agree${type.value}")
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
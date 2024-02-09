package com.manchuan.tools.activity.life

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.RegexUtils
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.bean.TelephoneBean
import com.manchuan.tools.databinding.ActivityTelephoneBinding
import com.manchuan.tools.extensions.applyAccentColor
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.json.SerializationConverter

class TelephoneActivity : AppCompatActivity() {

    private val telephoneBinding by lazy {
        ActivityTelephoneBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(telephoneBinding.root)
        setSupportActionBar(telephoneBinding.toolbar)
        immersive(telephoneBinding.toolbar)
        supportActionBar?.apply {
            title = "手机号归属地查询"
            setDisplayHomeAsUpEnabled(true)
        }
        telephoneBinding.linear1.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        telephoneBinding.card.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        telephoneBinding.imageview1.applyAccentColor()
        telephoneBinding.query.setOnClickListener {
            val telephone = telephoneBinding.url.text.toString()
            if (telephone.isEmpty()) {
                snack("请输入手机号码")
            } else if (RegexUtils.isMobileSimple(telephone)) {
                WaitDialog.show("查询中...")
                scopeNetLife {
                    val content =
                        Get<TelephoneBean>("https://zj.v.api.aa1.cn/api/phone-03gs/?tel=$telephone") {
                            converter = SerializationConverter("200", "code")
                        }.await().data
                    TipDialog.show("查询成功", WaitDialog.TYPE.SUCCESS)
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("手机号:${telephone}")
                    stringBuilder.append("\n省份:${content.proviceSimple}")
                    stringBuilder.append("\n城市:${content.cityCountySimple}")
                    stringBuilder.append("\n号段:${content.prefix}")
                    stringBuilder.append("\n区域代码:${content.areaCode}")
                    stringBuilder.append("\n运营商:${content.isp}")
                    telephoneBinding.info.setText(stringBuilder)
                }.catch {
                    PopTip.show(it.message)
                    TipDialog.show("查询失败", WaitDialog.TYPE.ERROR)
                }
            } else {
                snack("手机号格式错误")
            }
        }
        telephoneBinding.imageview1.setOnClickListener {
            if (telephoneBinding.info.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(telephoneBinding.info.text.toString())
                PopTip.show("已复制")
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
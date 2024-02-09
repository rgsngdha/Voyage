package com.manchuan.tools.activity.site

import android.os.Bundle
import android.view.MenuItem
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.activity.json.DomainFiling
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityBeianBinding
import com.manchuan.tools.json.SerializationConverter

class BeianActivity : BaseActivity() {

    private val beanieActivity by lazy {
        ActivityBeianBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(beanieActivity.root)
        setSupportActionBar(beanieActivity.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "网站ICP备案查询"
            setDisplayHomeAsUpEnabled(true)
        }
        beanieActivity.query.setOnClickListener {
            val telephone = beanieActivity.url.text.toString()
            if (telephone.isEmpty()) {
                PopTip.show("请输入网站")
            } else {
                scopeNetLife {
                    WaitDialog.show("查询中...")
                    val content =
                        Get<DomainFiling>("https://api.kit9.cn/api/domain_name_filing/api.php?url=$telephone") {
                            converter = SerializationConverter("200", "code", "msg")
                        }.await().data
                    TipDialog.show("查询成功", WaitDialog.TYPE.SUCCESS)
                    val stringBuilder = StringBuilder()
                    stringBuilder.append("主办单位:${content.name}")
                    stringBuilder.append("\n单位性质:${content.nature}")
                    stringBuilder.append("\n首页地址:${content.siteindex}")
                    stringBuilder.append("\n备案号:${content.icp}")
                    stringBuilder.append("\n备案名称:${content.sitename?.ifEmpty { "无" }}")
                    stringBuilder.append("\n备案时间:${content.time}")
                    stringBuilder.append("\n限制介入:${content.limitAccess}")
                    beanieActivity.info.setText(stringBuilder)
                }.catch {
                    PopTip.show(it.message)
                    TipDialog.show("查询失败", WaitDialog.TYPE.ERROR)
                }
            }
        }
        beanieActivity.imageview1.setOnClickListener {
            if (beanieActivity.info.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                ClipboardUtils.copyText(beanieActivity.info.text.toString())
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
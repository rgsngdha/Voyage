package com.manchuan.tools.activity.game

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.crazylegend.viewbinding.viewBinding
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.click
import com.manchuan.tools.activity.game.models.McServersModel
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityMcServerInfoBinding
import com.manchuan.tools.json.SerializationConverter

class McServerInfoActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMcServerInfoBinding::inflate)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "MC服务器信息查询"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.apply {
            query.click {
                if (url.isTextNotEmpty()) {
                    scopeNetLife {
                        val data =
                            Get<McServersModel>("https://uapis.cn/api/mcserver?server=${url.textString}") {
                                converter = SerializationConverter("200", "code", "status")
                            }.await()
                        TipDialog.show("查询成功", WaitDialog.TYPE.SUCCESS)
                        val stringBuilder = StringBuilder()
                        stringBuilder.append("服务器地址:${data.server}")
                        stringBuilder.append("\n服务器状态:${data.status}")
                        stringBuilder.append("\n服务器端口:${data.port}")
                        stringBuilder.append("\n服务器玩家:${data.players}")
                        stringBuilder.append("\n服务器介绍-第一行:${data.motd}")
                        stringBuilder.append("\n服务器介绍-第二行:${data.motd2}")
                        info.setText(stringBuilder)
                    }
                } else {
                    toast("服务器地址不能为空")
                }
            }
        }
    }
}
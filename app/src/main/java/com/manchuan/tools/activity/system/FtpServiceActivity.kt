package com.manchuan.tools.activity.system

import android.os.Bundle
import android.view.View
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.serialize.intent.startService
import com.drake.serialize.intent.stopService
import com.dylanc.longan.context
import com.dylanc.longan.textString
import com.itxca.spannablex.spannable
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.visible
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityFtpServiceBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.copyToClipboard
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.textCopyThenPost
import com.manchuan.tools.service.FTPService
import com.manchuan.tools.utils.IPUtils

class FtpServiceActivity : BaseActivity() {

    private val binding by viewBinding(ActivityFtpServiceBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "FTP局域网文件共享"
            setDisplayHomeAsUpEnabled(true)
        }
        with(binding) {
            enableTransitionTypes(statusCard, statusCardLay)
            startServer.throttleClick {
                switchMode.isChecked = !switchMode.isChecked
            }
            switchMode.setOnCheckedChangeListener { buttonView, isChecked ->
                when (isChecked) {
                    true -> startService<FTPService>()
                    false -> stopService<FTPService>()
                }
            }
        }
        FTPService.isStart.observe(this) {
            loge(it)
            when (it) {
                true -> {
                    loge("Server", "isStart")
                    with(binding) {
                        startServer.alpha = 1f
                        wifi.alpha = 1f
                        description.visible()
                        serverAddress.text = spannable {
                            "ftp://${IPUtils.getIpAddress(context)}:2121/".span {
                                clickable(colorPrimary(), onClick = { view: View, s: String ->
                                    copyToClipboard(s)
                                })
                                underline()
                            }
                        }
                        copy.throttleClick {
                            textCopyThenPost(serverAddress.textString)
                        }
                    }
                }

                false -> {
                    loge("Server", "isStop")
                    with(binding) {
                        startServer.alpha = 0.5f
                        wifi.alpha = 0.5f
                        description.gone()
                    }
                }
            }
        }
    }
}
package com.manchuan.tools.activity.crypt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.EncryptUtils
import com.crazylegend.viewbinding.viewBinding
import com.dylanc.longan.textString
import com.gyf.immersionbar.ktx.immersionBar
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityMd2CryptBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.utils.UiUtils

class Md2CryptActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMd2CryptBinding::inflate)

    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "MD2加密"
        immersionBar {
            titleBar(binding.toolbar)
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        binding.encrypt.setOnClickListener {
            if (binding.inputContent.text.toString().isEmpty()) {
                PopTip.show("请输入内容")
            } else {
                binding.result.setText(
                    dev.utils.common.encrypt.EncryptUtils.encryptMD2ToHexString(
                        binding.inputContent.textString
                    )
                )
                loge(dev.utils.common.encrypt.EncryptUtils.encryptMD2(
                    binding.inputContent.textString.toByteArray()
                ).toHexString())
            }
        }
        binding.copy.setOnClickListener {
            if (binding.result.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                PopTip.show("已复制")
                ClipboardUtils.copyText(binding.result.textString)
            }
        }
    }


}
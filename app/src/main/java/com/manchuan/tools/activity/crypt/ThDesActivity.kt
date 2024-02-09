package com.manchuan.tools.activity.crypt

import android.os.Bundle
import android.view.View
import com.drake.statusbar.immersive
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityThDesBinding
import com.manchuan.tools.extensions.textCopyThenPost
import dev.utils.common.encrypt.TripleDESUtils
import okio.ByteString.Companion.toByteString
import java.nio.charset.Charset

class ThDesActivity : BaseActivity() {
    private val binding by lazy {
        ActivityThDesBinding.inflate(layoutInflater)
    }

    private lateinit var key: ByteArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "3DES加解密"
        }
        immersive(binding.toolbar)
        binding.apply {
            crypt.setOnClickListener { view: View? ->
                key = TripleDESUtils.initKey()
                inputKey.setText(key.toByteString().string(Charset.defaultCharset()))
                if (inputContent.isTextEmpty()) {
                    PopTip.show("请输入需要加密的内容")
                } else if (inputKey.isTextEmpty()) {
                    PopTip.show("请输入密钥")
                } else {
                    runCatching {
                        result.setText(
                            TripleDESUtils.encrypt(
                                inputContent.textString.toByteArray(),
                                inputKey.textString.toByteArray(),
                            ).contentToString()
                        )
                    }.onFailure {
                        toast("加密失败")
                    }
                }
            }

            decrypt.setOnClickListener { view: View? ->
                if (inputContent.isTextEmpty()) {
                    PopTip.show("请输入需要解密的内容")
                } else if (inputKey.isTextEmpty()) {
                    PopTip.show("请输入密钥")
                } else {
                    runCatching {
                        result.setText(
                            TripleDESUtils.decrypt(
                                inputContent.textString.toByteArray(),
                                inputKey.textString.toByteArray(),
                            ).contentToString()
                        )
                    }.onFailure {
                        toast("解密失败")
                    }
                }
            }
            copy.setOnClickListener {
                if (result.isTextEmpty()) {
                    PopTip.show("无内容")
                } else {
                    textCopyThenPost(result.textString)
                    PopTip.show("已复制")
                }
            }
        }
    }
}
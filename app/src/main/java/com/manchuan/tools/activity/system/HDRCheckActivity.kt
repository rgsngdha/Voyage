package com.manchuan.tools.activity.system

import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.drake.spannable.span.CenterImageSpan
import com.dylanc.longan.doOnBackPressed
import com.gyf.immersionbar.ktx.immersionBar
import com.itxca.spannablex.spannable
import com.itxca.spannablex.utils.drawableSize
import com.lxj.androidktx.core.dp
import com.lxj.androidktx.core.drawable
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityHdrcheckBinding
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.tint
import com.manchuan.tools.utils.UiUtils

class HDRCheckActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityHdrcheckBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersionBar {
            titleBar(binding.toolbar)
            statusBarDarkFont(!UiUtils.isDarkMode())
            transparentBar()
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "屏幕HDR检测"
        }
        BaseAlertDialogBuilder(this).setTitle("提示").setMessage("点击确定以开始检测")
            .setPositiveButton("确定") { _: DialogInterface, _: Int ->
                runCatching {
                    for (types in if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        this.display?.hdrCapabilities?.supportedHdrTypes!!
                    } else {
                        TODO("VERSION.SDK_INT < R")
                    }) {
                        when (types) {
                            2 -> {
                                binding.hdr10.text = "支持"
                                binding.hdr10.setTextColor(Color.GREEN)
                            }

                            1 -> {
                                binding.vision.text = spannable {
                                    image(
                                        drawable(com.manchuan.tools.R.drawable.ic_dolby_vision_logo).apply {
                                            tint(color = Color.BLACK)
                                        },
                                        align = CenterImageSpan.Align.CENTER,
                                        size = 122.dp.drawableSize
                                    )
                                }
                                binding.vision.setTextColor(Color.GREEN)
                            }

                            3 -> {
                                binding.hlg.text = "支持"
                                binding.hlg.setTextColor(Color.GREEN)
                            }

                            4 -> {
                                binding.hdr10p.text = "支持"
                                binding.hdr10p.setTextColor(Color.GREEN)
                            }
                        }
                    }
                }.onFailure {
                    snack("检测出错，可能是您的系统厂商已删除相关属性或系统不支持。")
                }
            }.setCancelable(false).setOnKeyListener { dialogInterface, i, keyEvent ->
                if (i == KeyEvent.KEYCODE_BACK) {
                    finish()
                }
                true
            }.create().show()
        doOnBackPressed {
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
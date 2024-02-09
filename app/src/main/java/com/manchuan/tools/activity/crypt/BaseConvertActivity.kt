package com.manchuan.tools.activity.crypt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ClipboardUtils
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.databinding.ActivityBaseConvertBinding
import com.manchuan.tools.utils.UiUtils
import java.util.Base64

class BaseConvertActivity : AppCompatActivity() {

    private var baseConvertBinding: ActivityBaseConvertBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseConvertBinding = ActivityBaseConvertBinding.inflate(LayoutInflater.from(this))
        setContentView(baseConvertBinding?.root)
        setSupportActionBar(baseConvertBinding?.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Base64加解密"
        }
        baseConvertBinding?.toolbar?.let { immersive(it, !UiUtils.isDarkMode()) }
        baseConvertBinding?.materialbutton1!!.setOnClickListener {
            if (baseConvertBinding?.edittext1?.text.toString().isEmpty()) {
                PopTip.show("请输入需要加密的内容")
            } else {
                baseConvertBinding?.autocomplete1!!.setText(
                    Base64.getEncoder().encodeToString(
                        baseConvertBinding?.edittext1!!.text.toString().toByteArray()
                    )
                )
            }
        }
        baseConvertBinding?.materialbutton2!!.setOnClickListener {
            kotlin.runCatching {
                if (baseConvertBinding?.edittext1?.text.toString().isEmpty()) {
                    PopTip.show("请输入需要解密的内容")
                } else {
                    val decodedBytes =
                        Base64.getDecoder().decode(baseConvertBinding?.edittext1!!.text.toString())
                    val decodedString = String(decodedBytes)
                    baseConvertBinding?.autocomplete1!!.setText(
                        decodedString
                    )
                }
            }.onFailure {
                PopTip.show("请确认输入的内容是否经过Base64加密")
            }
        }
        baseConvertBinding?.imageview1!!.setOnClickListener {
            if (baseConvertBinding?.autocomplete1!!.text.toString().isEmpty()) {
                PopTip.show("无内容")
            } else {
                PopTip.show("已复制")
                ClipboardUtils.copyText(baseConvertBinding?.autocomplete1!!.text.toString())
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
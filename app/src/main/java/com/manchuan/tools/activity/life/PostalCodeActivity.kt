package com.manchuan.tools.activity.life

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.startActivity
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPostalCodeBinding

class PostalCodeActivity : BaseActivity() {

    private val binding by viewBinding(ActivityPostalCodeBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "邮政编码查询"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        binding.queryOne.setOnClickListener {
            if (binding.province.isTextEmpty() or binding.city.isTextEmpty() or binding.area.isTextEmpty()) {
                toast("请填写完整")
            } else {
                startActivity<PostalQueryActivity>(
                    "type" to "area",
                    "province" to binding.province.textString,
                    "city" to binding.city.textString,
                    "area" to binding.area.textString
                )
            }
        }
        binding.queryTwo.setOnClickListener {
            if (binding.postalCode.isTextEmpty()) {
                toast("请填写编码")
            } else {
                startActivity<PostalQueryActivity>(
                    "type" to "code", "code" to binding.postalCode.textString
                )
            }
        }
    }


}
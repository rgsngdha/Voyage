package com.manchuan.tools.activity.life

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityLocationInquireBinding
import com.manchuan.tools.extensions.errorToast
import com.manchuan.tools.extensions.loge

class LocationInquireActivity : BaseActivity() {

    private val binding by lazy {
        ActivityLocationInquireBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        binding.jiexi.throttleClick {
            if (binding.lng.isTextNotEmpty() && binding.lat.isTextNotEmpty()) {
                scopeNetLife {
                    val content =
                        Get<String>("http://yichen.api.z7zz.cn/api/location_geocoder_address.php?lng=${binding.lng.textString}&lat=${binding.lat.textString}").await()
                    binding.autocomplete1.setText(content)
                }.catch {
                    it.message?.errorToast()
                    loge(it.toString())
                }
            } else {
                "请填写完整".errorToast()
            }
        }
    }
}
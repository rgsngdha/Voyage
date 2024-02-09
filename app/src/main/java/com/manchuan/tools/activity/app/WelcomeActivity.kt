package com.manchuan.tools.activity.app

import android.annotation.SuppressLint
import android.os.Bundle
import com.dylanc.longan.isAppDarkMode
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.databinding.ActivityWelcomeBinding
import com.microsoft.appcenter.analytics.Analytics
import rikka.material.app.MaterialActivity

class WelcomeActivity : MaterialActivity() {
    private val welcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }

    private val rules = arrayListOf<tiaoKuan>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(welcomeBinding.root)
        immersionBar {
            transparentBar()
            statusBarDarkFont(!isAppDarkMode)
        }
        Analytics.trackEvent("首次使用")/*
        welcomeBinding.title.text = "Hi,欢迎\n使用$appName".replaceSpan(appName) {
            ColorSpan(getColorByAttr(com.google.android.material.R.attr.colorPrimary))
        }
        val svgDrawable = SvgDrawable(activity, R.raw.pixel_welcome)
        svgDrawable.setScale(1F)
        val bitmap: Bitmap = Bitmap.createBitmap(
            SystemUiUtil.getDisplayWidth(activity),
            SystemUiUtil.getDisplayHeight(activity),
            Bitmap.Config.ARGB_8888
        )
        rules.add(tiaoKuan("用户协议") {
            startActivity<PrivacyActivity>("type" to 2)
        })
        rules.add(tiaoKuan("隐私政策") {
            startActivity<PrivacyActivity>("type" to 1)
        })
        val canvas = Canvas(bitmap)
        svgDrawable.draw(canvas)
        try {
            welcomeBinding.svg.setImageBitmap(bitmap)
        } catch (e: IOException) {
            Timber.tag(TAG).e(e, "onViewCreated: ")
        }
        welcomeBinding.start.setOnClickListener {
            if (Global.isFirst) {
                Global.isFirst = false
                startActivity(Intent(this, SplashActivity::class.java))
                overridePendingTransition(
                    ando.file.R.anim.abc_fade_in, ando.file.R.anim.abc_fade_out
                )
                finish()
            }
        }
        welcomeBinding.recyclerView.setup {
            addType<tiaoKuan>(R.layout.item_tiaokuan)
            onBind {
                val binding = ItemTiaokuanBinding.bind(itemView)
                binding.name.text = getModel<tiaoKuan>(modelPosition).name
            }
            onClick(R.id.item) {
                getModel<tiaoKuan>(modelPosition).unit.invoke()
            }
            onLongClick(R.id.item) {
                val binding = ItemTiaokuanBinding.bind(itemView)
                binding.item.isChecked = !binding.item.isChecked
            }
        }.models = rules
         */
    }


    data class tiaoKuan(var name: String, var unit: () -> Unit)
}


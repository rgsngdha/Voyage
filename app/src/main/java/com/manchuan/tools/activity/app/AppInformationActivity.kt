package com.manchuan.tools.activity.app

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.drake.statusbar.immersive
import com.lxj.androidktx.FragmentStateAdapter
import com.lxj.androidktx.setupWithViewPager2
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.withArguments
import com.manchuan.tools.activity.app.fragments.AppActivitiesFragment
import com.manchuan.tools.activity.app.fragments.AppInfoFragment
import com.manchuan.tools.activity.app.fragments.AppPermissionFragment
import com.manchuan.tools.databinding.ActivityAppInfoBinding
import com.manchuan.tools.utils.UiUtils

class AppInformationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAppInfoBinding.inflate(layoutInflater)
    }

    private val tabs = listOf("应用信息", "活动", "权限")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        app_name = safeIntentExtras<String>("appName").value
        package_name = safeIntentExtras<String>("packageName").value
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = app_name
        }
        immersive(binding.toolbar, !UiUtils.isDarkMode())
        binding.viewPager.apply {
            adapter = FragmentStateAdapter(
                AppInfoFragment().withArguments("packageName" to package_name),
                AppActivitiesFragment().withArguments("packageName" to package_name),
                AppPermissionFragment().withArguments("packageName" to package_name),
                isLazyLoading = false
            )
        }
        binding.tabLay.setupWithViewPager2(binding.viewPager, tabConfigurationStrategy = { tab, i ->
            tab.text = tabs[i]
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @JvmField
        var package_name: String? = null

        @JvmField
        var app_name: String? = null
    }
}
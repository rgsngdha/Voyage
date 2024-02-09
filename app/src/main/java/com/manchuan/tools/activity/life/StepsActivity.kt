package com.manchuan.tools.activity.life

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.google.android.material.tabs.TabLayout
import com.lxj.androidktx.FragmentStateAdapter
import com.lxj.androidktx.setupWithViewPager2
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityStepsBinding
import com.manchuan.tools.fragment.XiaoMiFragment

class StepsActivity : BaseActivity() {

    private lateinit var binding: ActivityStepsBinding

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "步数修改"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        binding.viewPager.adapter = FragmentStateAdapter(XiaoMiFragment())
        val tabs: TabLayout = binding.tabs
        val titles = listOf("小米运动")
        tabs.setupWithViewPager2(binding.viewPager, tabConfigurationStrategy = { tab, i ->
            tab.text = titles[i]
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
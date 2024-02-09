package com.manchuan.tools.activity.news

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.google.android.material.tabs.TabLayout
import com.lxj.androidktx.FragmentStateAdapter
import com.lxj.androidktx.setupWithViewPager2
import com.manchuan.tools.activity.news.fragments.BaiduHotFragment
import com.manchuan.tools.activity.news.fragments.BiliBiliFragment
import com.manchuan.tools.activity.news.fragments.TiebaFragment
import com.manchuan.tools.activity.news.fragments.WeiboFragment
import com.manchuan.tools.activity.news.fragments.ZhiHuFragment
import com.manchuan.tools.databinding.ActivityTopHotBinding

class TopHotActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityTopHotBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "实时热搜"
            subtitle = "汇集全网热搜榜单"
            setDisplayHomeAsUpEnabled(true)
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
        immerseStatusBar(!isAppDarkMode)
        binding.viewPager.adapter = FragmentStateAdapter(
            BaiduHotFragment(),
            BiliBiliFragment(),
            WeiboFragment(),
            ZhiHuFragment(),
            TiebaFragment(),
            isLazyLoading = true
        )
        val category = listOf("百度", "哔哩哔哩", "微博", "知乎", "贴吧")
        binding.tabLay.setupWithViewPager2(binding.viewPager,
            autoRefresh = true,
            enableScroll = true,
            tabConfigurationStrategy = { tab: TabLayout.Tab, i: Int ->
                tab.text = category[i]
            })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
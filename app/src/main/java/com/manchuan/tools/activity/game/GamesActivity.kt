package com.manchuan.tools.activity.game

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.google.android.material.tabs.TabLayout
import com.lxj.androidktx.FragmentStateAdapter
import com.lxj.androidktx.setupWithViewPager2
import com.manchuan.tools.R
import com.manchuan.tools.activity.game.fragments.GameFCFragment
import com.manchuan.tools.activity.game.fragments.GameGBAFragment
import com.manchuan.tools.activity.game.fragments.GameJieJiFragment
import com.manchuan.tools.activity.game.fragments.GameMDFragment
import com.manchuan.tools.activity.game.fragments.GameSFCFragment
import com.manchuan.tools.adapter.transformer.CascadeTransformer
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityGamesBinding
import com.manchuan.tools.extensions.startActivity


class GamesActivity : BaseActivity() {
    private val binding by lazy {
        ActivityGamesBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "怀旧游戏大全"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        initViewPager()
        val category = listOf("街机游戏", "FC游戏", "SFC游戏", "MD游戏", "GBA游戏")
        binding.tabLay.setupWithViewPager2(binding.viewPager,
            autoRefresh = true,
            enableScroll = true,
            tabConfigurationStrategy = { tab: TabLayout.Tab, i: Int ->
                tab.text = category[i]
            })
    }

    private fun initViewPager() {
        binding.viewPager.adapter = FragmentStateAdapter(
            GameJieJiFragment(),
            GameFCFragment(),
            GameSFCFragment(),
            GameMDFragment(),
            GameGBAFragment(),
            isLazyLoading = true
        )
        binding.viewPager.setPageTransformer(CascadeTransformer())
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.search -> startActivity<SearchGamesActivity>()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_search_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
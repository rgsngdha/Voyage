package com.manchuan.tools.activity.movies

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.drake.net.Get
import com.drake.net.utils.scopeDialog
import com.dylanc.longan.isAppDarkMode
import com.google.gson.Gson
import com.gyf.immersionbar.ktx.immersionBar
import com.lxj.androidktx.FragmentStateAdapter
import com.lxj.androidktx.core.decryptAES
import com.lxj.androidktx.core.doOnlyOnce
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.database.SourceEntity
import com.manchuan.tools.activity.movies.database.SourcesDatabase
import com.manchuan.tools.activity.movies.fragments.AlbumFragment
import com.manchuan.tools.activity.movies.fragments.MovieAccountFragment
import com.manchuan.tools.activity.movies.fragments.MoviesCategoryFragment
import com.manchuan.tools.activity.movies.fragments.MoviesMainFragment
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityMoviesMainBinding
import com.manchuan.tools.extensions.addPaddingBottom
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.base64Decoded
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.json
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.snack

class MoviesMainActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMoviesMainBinding.inflate(layoutInflater)
    }

    private val sourcesDatabase by lazy {
        SourcesDatabase.getInstance(this)
    }

    private lateinit var pageChangeListener: OnPageChangeCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            transparentBar()
            statusBarDarkFont(!isAppDarkMode)
        }
        binding.viewPager.adapter = FragmentStateAdapter(
            MoviesMainFragment(),
            AlbumFragment(),
            MoviesCategoryFragment(),
            MovieAccountFragment(),
            isLazyLoading = true
        )
        binding.viewPager.isUserInputEnabled = Global.isCanUserInput.value == true
        binding.bottomBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> binding.viewPager.setCurrentItem(0, true)
                R.id.ablum -> binding.viewPager.setCurrentItem(1, true)
                R.id.category -> binding.viewPager.setCurrentItem(2, true)
                R.id.my -> binding.viewPager.setCurrentItem(3, true)
            }
            true
        }
        pageChangeListener = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomBar.menu.getItem(position).isChecked = true
            }
        }
        binding.bottomBar.post {
            binding.viewPager.addPaddingBottom(binding.bottomBar.height)
        }
        doOnlyOnce("first_use_movie", action = {
            alertDialog {
                title = "提示"
                message = "检测到您为首次使用，是否导入官方订阅？"
                isCancelable = false
                okButton("导入") {
                    scopeDialog {
                        val string =
                            Get<String>("https://app.zhongyi.team/firstuse").await()
                        val json = json.decodeFromString<SourceEntity>(
                            string.decryptAES("Voyager209900000").base64Decoded())
                        json.id = if (sourcesDatabase.getSourcesDao().queryAllSources()
                                .isEmpty()
                        ) 1 else sourcesDatabase.getSourcesDao().queryAllSources().size.inc()
                        sourcesDatabase.getSourcesDao().insertSource(json)
                        snack("欢迎使用，已为你导入官方订阅")
                    }.catch {
                        snack("失败:${it.message}")
                    }
                }
                cancelButton()
            }.build()
        })
    }

    override fun onResume() {
        super.onResume()
        binding.viewPager.registerOnPageChangeCallback(pageChangeListener)
    }

    override fun onPause() {
        super.onPause()
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeListener)
    }

}
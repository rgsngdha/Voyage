package com.manchuan.tools.activity.life

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.R
import com.manchuan.tools.activity.life.model.BingWallpaper
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityBingWallpaperBinding
import com.manchuan.tools.databinding.ItemBingsWallpaperBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.savePic
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.json.SerializationConverter

class BingWallpaperActivity : BaseActivity() {

    private val binding by viewBinding(ActivityBingWallpaperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "必应壁纸"
            setDisplayHomeAsUpEnabled(true)
        }
        immersionBar {
            transparentBar()
            titleBar(binding.toolbar)
        }
        binding.page.addNavigationBarHeightToMarginBottom()
        binding.recyclerView.linear().setup {
            addType<BingWallpaper.Data.WallPaper>(R.layout.item_bings_wallpaper)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = getBinding<ItemBingsWallpaperBinding>()
                val model = getModel<BingWallpaper.Data.WallPaper>()
                binding.image.load(
                    "http://s.cn.bing.net${model.url}",
                    isCrossFade = true,
                    isForceOriginalSize = true,
                    skipMemory = true
                )
                binding.name.text = model.title
                binding.description.text = model.copyright
            }
            R.id.image.onFastClick {
                val model = getModel<BingWallpaper.Data.WallPaper>()
                selector(listOf("下载"), "操作") { dialogInterface, s, i ->
                    when (s) {
                        "下载" -> savePic("http://s.cn.bing.net${model.url}")
                    }
                }
            }
        }
        binding.page.onRefresh {
            scope {
                val bingWallpaper =
                    Get<BingWallpaper>("https://uapi.woobx.cn/app/bing-wallpaper?page=1&size=20") {
                        converter = SerializationConverter("200", "code", "")
                    }.await()
                binding.recyclerView.models = emptyList()
                addData(bingWallpaper.data.list) {
                    index < bingWallpaper.data.pages
                }
            }
        }.autoRefresh()
        binding.page.onLoadMore {
            scope {
                val bingWallpaper =
                    Get<BingWallpaper>("https://uapi.woobx.cn/app/bing-wallpaper?page=${index.inc()}&size=20") {
                        converter = SerializationConverter("200", "code", "")
                    }.await()
                addData(bingWallpaper.data.list) {
                    index < bingWallpaper.data.pages
                }
            }
        }
    }

}
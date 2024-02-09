package com.manchuan.tools.activity.images

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.startActivity
import com.github.panpf.sketch.util.PauseLoadWhenScrollingMixedScrollListener
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.bean.WallPapersCategory
import com.manchuan.tools.databinding.ActivityWallpaperCategoryBinding
import com.manchuan.tools.databinding.ItemCategoryBinding
import com.manchuan.tools.extensions.ImageEngine
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter
import timber.log.Timber

class WallpaperCategoryActivity : BaseActivity() {

    private val binding by lazy {
        ActivityWallpaperCategoryBinding.inflate(layoutInflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        binding.ctl.apply {
            title = "壁纸大全"
            subtitle = "多分类高清手机壁纸"
        }
        binding.recyclerView.addOnScrollListener(PauseLoadWhenScrollingMixedScrollListener())
        binding.recyclerView.grid(2).setup {
            setAnimation(AnimationType.ALPHA)
            addType<WallPapersCategory.Res.Category>(R.layout.item_category)
            onBind {
                val binding = ItemCategoryBinding.bind(itemView)
                val model = getModel<WallPapersCategory.Res.Category>()
                binding.text.text = model.name
                binding.image.load(
                    model.cover, isCrossFade = true, imageEngine = ImageEngine.SKETCH
                )
            }
            R.id.cardview1.onClick {
                val model = getModel<WallPapersCategory.Res.Category>()
                startActivity<WallpaperPreviewActivity>("id" to model.id, "title" to model.name)
            }
        }
        binding.page.setEnableLoadMore(false)
        binding.page.onRefresh {
            scope {
                binding.recyclerView.models =
                    Get<WallPapersCategory>("https://service.picasso.adesk.com/v1/lightwp/category") {
                        converter = SerializationConverter("0", "code", "msg")
                    }.await().res.category
            }.catch {
                Timber.tag("壁纸大全").e(it)
            }
        }.autoRefresh()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_search_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.search -> startActivity<SearchWallpaperActivity>()
        }
        return super.onOptionsItemSelected(item)
    }

}
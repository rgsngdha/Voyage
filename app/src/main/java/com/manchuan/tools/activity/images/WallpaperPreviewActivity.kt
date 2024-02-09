package com.manchuan.tools.activity.images

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.addModels
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.logError
import com.dylanc.longan.startActivity
import com.github.panpf.sketch.util.PauseLoadWhenScrollingMixedScrollListener
import com.manchuan.tools.R
import com.manchuan.tools.activity.images.models.HorizontalWall
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityWallpaperPreviewBinding
import com.manchuan.tools.databinding.ItemsPreviewsBinding
import com.manchuan.tools.extensions.ImageEngine
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.model.WallpaperModel
import me.zhanghai.android.fastscroll.FastScrollerBuilder


class WallpaperPreviewActivity : BaseActivity() {
    private val wallpaper by lazy {
        ActivityWallpaperPreviewBinding.inflate(layoutInflater)
    }
    private var limit: Int = 30
    private var skip: Int = 0
    private var ids: String? = null

    private val imageList = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged", "InflateParams", "SdCardPath")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(wallpaper.root)
        setSupportActionBar(wallpaper.toolbar)
        immerseStatusBar(!isAppDarkMode)
        val intent = intent
        if (intent != null) {
            ids = intent.getStringExtra("id")
            supportActionBar?.title = intent.getStringExtra("title")
        }
        FastScrollerBuilder(wallpaper.recyclerView).useMd2Style().build()
        logError(ids)
        wallpaper.toggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when {
                checkedId == R.id.news && isChecked -> {
                    orders = "new"
                    loadWallpaper()
                }

                checkedId == R.id.hot && isChecked -> {
                    orders = "hot"
                    loadWallpaper()
                }
            }
        }
        wallpaper.toggleTwo.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when {
                checkedId == R.id.horizontal && isChecked -> {
                    types = 1
                    loadWallpaper()
                }

                checkedId == R.id.vertical && isChecked -> {
                    types = 0
                    loadWallpaper()
                }
            }
        }
        loadWallpaper()
        wallpaper.recyclerView.addOnScrollListener(PauseLoadWhenScrollingMixedScrollListener())
    }

    private var types = 0

    private var orders = "new"

    private fun loadWallpaper(type: Int = types, order: String = orders) {
        if (type == 0) {
            switchLayout(0)
            wallpaper.page.onRefresh {
                scope {
                    skip = 0
                    limit = 30
                    imageList.clear()
                    val models =
                        Get<WallpaperModel>("https://service.picasso.adesk.com/v1/vertical/category/$ids/vertical?limit=$limit&skip=0&order=$order") {
                            converter = SerializationConverter("0", "code", "msg")
                        }.await().res.vertical
                    wallpaper.recyclerView.models = models
                    models.forEach {
                        imageList.add(it.wp)
                    }
                }
            }.autoRefresh()
            wallpaper.page.onLoadMore {
                scope {
                    skip += limit
                    val models =
                        Get<WallpaperModel>("https://service.picasso.adesk.com/v1/vertical/category/$ids/vertical?limit=${limit}&skip=$skip&order=$order") {
                            converter = SerializationConverter("0", "code", "msg")
                        }.await().res.vertical
                    wallpaper.recyclerView.addModels(models)
                    models.forEach {
                        imageList.add(it.img)
                    }
                }
            }
        } else {
            switchLayout(1)
            wallpaper.page.onRefresh {
                scope {
                    skip = 0
                    limit = 30
                    imageList.clear()
                    val models =
                        Get<HorizontalWall>("http://service.aibizhi.adesk.com/v1/wallpaper/category/$ids/wallpaper?limit=$limit&skip=0&order=$order") {
                            converter = SerializationConverter("0", "code", "msg")
                            setHeader("User-Agent", "picasso,285,nearme")
                        }.await().res.wallpaper
                    wallpaper.recyclerView.models = models
                    models.forEach {
                        imageList.add(it.wp)
                    }
                }
            }.autoRefresh()
            wallpaper.page.onLoadMore {
                scope {
                    skip += limit
                    val models =
                        Get<HorizontalWall>("http://service.aibizhi.adesk.com/v1/wallpaper/category/$ids/wallpaper?limit=${limit}&skip=$skip&order=$order") {
                            converter = SerializationConverter("0", "code", "msg")
                            setHeader("User-Agent", "picasso,285,nearme")
                        }.await().res.wallpaper
                    wallpaper.recyclerView.addModels(models)
                    models.forEach {
                        imageList.add(it.img)
                    }
                }
            }
        }
    }

    private fun switchLayout(type: Int) {
        if (type == 0) {
            wallpaper.recyclerView.grid(3).setup {
                addType<WallpaperModel.Res.Vertical>(R.layout.items_previews)
                setAnimation(AnimationType.ALPHA)
                onBind {
                    val binding = ItemsPreviewsBinding.bind(itemView)
                    val model = getModel<WallpaperModel.Res.Vertical>()
                    binding.image.load(
                        model.wp, isCrossFade = true, imageEngine = ImageEngine.SKETCH
                    )
                    loge(model.img)
                }
                onClick(R.id.image) {
                    val model = getModel<WallpaperModel.Res.Vertical>()
                    startActivity<ImagePreviewActivity>("url" to model.wp)
                }
            }
        } else {
            wallpaper.recyclerView.grid(2).setup {
                addType<HorizontalWall.Res.Wallpaper>(R.layout.item_wallpaper_horizontal)
                setAnimation(AnimationType.ALPHA)
                onBind {
                    val binding = ItemsPreviewsBinding.bind(itemView)
                    val model = getModel<HorizontalWall.Res.Wallpaper>()
                    binding.image.load(
                        model.wp, isCrossFade = true, imageEngine = ImageEngine.SKETCH
                    )
                    loge(model.img)
                }
                onClick(R.id.image) {
                    val model = getModel<HorizontalWall.Res.Wallpaper>()
                    startActivity<ImagePreviewActivity>("url" to model.wp)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
package com.manchuan.tools.activity.images

import android.os.Bundle
import cc.shinichi.library.ImagePreview
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.addModels
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scope
import com.drake.net.utils.scopeDialog
import com.dylanc.longan.fileProviderAuthority
import com.dylanc.longan.textString
import com.kongzue.dialogx.dialogs.BottomMenu
import com.manchuan.tools.R
import com.manchuan.tools.activity.images.models.SearchWallHorizontal
import com.manchuan.tools.activity.images.models.SearchWallVertical
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivitySearchWallpaperBinding
import com.manchuan.tools.databinding.ItemsPreviewsBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.publicPicturesDirPath
import com.manchuan.tools.extensions.savePic
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.urlEncoded
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.user.timeMills
import com.manchuan.tools.utils.setWallpaper
import com.mcxiaoke.koi.ext.addToMediaStore
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.File

class SearchWallpaperActivity : BaseActivity() {

    private val binding by viewBinding(ActivitySearchWallpaperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "壁纸搜索"
            subtitle = "通过关键字搜索横竖屏壁纸"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.apply {
            FastScrollerBuilder(recyclerView).useMd2Style().build()
            editText.setOnEditorActionListener { v, actionId, event ->
                selector(
                    listOf("竖屏壁纸", "横屏壁纸"), "搜索壁纸类型"
                ) { dialogInterface, s, i ->
                    when (s) {
                        "竖屏壁纸" -> {
                            type = 0
                            searchWallpaper(editText.textString)
                        }

                        "横屏壁纸" -> {
                            type = 1
                            searchWallpaper(editText.textString)
                        }
                    }
                }
                true
            }
        }
    }

    private var limit: Int = 30
    private var skip: Int = 0

    private var type = 0

    private fun searchWallpaper(content: String) {
        if (type == 0) {
            binding.recyclerView.grid(3).setup {
                addType<SearchWallVertical.Res.Vertical>(R.layout.items_previews)
                setAnimation(AnimationType.ALPHA)
                onBind {
                    val binding = ItemsPreviewsBinding.bind(itemView)
                    val model = getModel<SearchWallVertical.Res.Vertical>()
                    binding.image.load(
                        model.wp, isCrossFade = true, isForceOriginalSize = false, skipMemory = true
                    )
                }
                onClick(R.id.image) {
                    ImagePreview.instance.apply {
                        setContext(context)
                        setImageList(imageList)
                        setShowErrorToast(true)
                        setLoadStrategy(ImagePreview.LoadStrategy.Auto)
                        setShowCloseButton(true)
                        setIndex(modelPosition)
                        start()
                    }
                }
                onLongClick(R.id.image) {
                    val model = getModel<SearchWallVertical.Res.Vertical>()
                    BottomMenu.show("操作", null, listOf("下载", "设为壁纸"))
                        .setOnMenuItemClickListener { dialog, text, index ->
                            when (text) {
                                "下载" -> {
                                    savePic(model.wp)
                                }

                                "设为壁纸" -> {
                                    scopeDialog {
                                        val file = Get<File>(model.wp) {
                                            setDownloadDir(publicPicturesDirPath)
                                            setDownloadFileName("$timeMills.png")
                                            setDownloadFileNameDecode(true)
                                            setDownloadFileNameConflict(true)
                                            setDownloadMd5Verify(true)
                                            addDownloadListener(object : ProgressListener() {
                                                override fun onProgress(p: Progress) {
                                                }

                                            })
                                        }.await()
                                        addToMediaStore(file)
                                        setWallpaper(
                                            file.absolutePath, fileProviderAuthority
                                        )
                                    }.catch {
                                        loge(it.message, it)
                                    }
                                }
                            }
                            false
                        }
                }
            }
            binding.page.onRefresh {
                scope {
                    skip = 0
                    limit = 30
                    imageList.clear()
                    val vertical =
                        Get<SearchWallVertical>("http://so.picasso.adesk.com/v1/search/vertical/resource/${content.urlEncoded()}?limit=$limit&channel=nearme&adult=false&first=0&skip=$skip&order=new") {
                            converter = SerializationConverter("", "", "msg")
                        }.await()
                    binding.recyclerView.models = vertical.res.vertical
                    vertical.res.vertical.forEach {
                        imageList.add(it.wp)
                    }
                }
            }.autoRefresh()
            binding.page.onLoadMore {
                scope {
                    skip += limit
                    val vertical =
                        Get<SearchWallVertical>("http://so.picasso.adesk.com/v1/search/vertical/resource/${content.urlEncoded()}?limit=$limit&channel=nearme&adult=false&first=0&skip=$skip&order=new") {
                            converter = SerializationConverter("", "", "msg")
                        }.await()
                    binding.recyclerView.addModels(vertical.res.vertical)
                    vertical.res.vertical.forEach {
                        imageList.add(it.wp)
                    }
                }
            }
        } else {
            binding.recyclerView.grid(2).setup {
                addType<SearchWallHorizontal.Res.Wallpaper>(R.layout.item_wallpaper_horizontal)
                setAnimation(AnimationType.ALPHA)
                onBind {
                    val binding = ItemsPreviewsBinding.bind(itemView)
                    val model = getModel<SearchWallHorizontal.Res.Wallpaper>()
                    binding.image.load(
                        model.wp, isCrossFade = true, isForceOriginalSize = false, skipMemory = true
                    )
                }
                onClick(R.id.image) {
                    ImagePreview.instance.apply {
                        setContext(context)
                        setImageList(imageList)
                        setShowErrorToast(true)
                        setLoadStrategy(ImagePreview.LoadStrategy.Auto)
                        setShowCloseButton(true)
                        setIndex(modelPosition)
                        start()
                    }
                }
                onLongClick(R.id.image) {
                    val model = getModel<SearchWallHorizontal.Res.Wallpaper>()
                    BottomMenu.show("操作", null, listOf("下载", "设为壁纸"))
                        .setOnMenuItemClickListener { dialog, text, index ->
                            when (text) {
                                "下载" -> {
                                    savePic(model.wp)
                                }

                                "设为壁纸" -> {
                                    scopeDialog {
                                        val file = Get<File>(model.wp) {
                                            setDownloadDir(publicPicturesDirPath)
                                            setDownloadFileName("$timeMills.png")
                                            setDownloadFileNameDecode(true)
                                            setDownloadFileNameConflict(true)
                                            setDownloadMd5Verify(true)
                                            addDownloadListener(object : ProgressListener() {
                                                override fun onProgress(p: Progress) {
                                                }

                                            })
                                        }.await()
                                        addToMediaStore(file)
                                        setWallpaper(
                                            file.absolutePath, fileProviderAuthority
                                        )
                                    }.catch {
                                        loge(it.message, it)
                                    }
                                }
                            }
                            false
                        }
                }
            }
            binding.page.onRefresh {
                scope {
                    skip = 0
                    limit = 30
                    imageList.clear()
                    val horizontal =
                        Get<SearchWallHorizontal>("http://so.picasso.adesk.com/v1/search/wallpaper/resource/${content.urlEncoded()}?package=com.lovebizhi.wallpaper&limit=$limit&channel=nearme&adult=false&first=0&skip=$skip&order=new") {
                            converter = SerializationConverter("", "", "msg")
                        }.await()
                    binding.recyclerView.models = horizontal.res.wallpaper
                    horizontal.res.wallpaper.forEach {
                        imageList.add(it.wp)
                    }
                }
            }.autoRefresh()
            binding.page.onLoadMore {
                scope {
                    skip += limit
                    val horizontal =
                        Get<SearchWallHorizontal>("http://so.picasso.adesk.com/v1/search/wallpaper/resource/${content.urlEncoded()}?package=com.lovebizhi.wallpaper&limit=$limit&channel=nearme&adult=false&first=0&skip=$skip&order=new") {
                            converter = SerializationConverter("", "", "msg")
                        }.await()
                    binding.recyclerView.addModels(horizontal.res.wallpaper)
                    horizontal.res.wallpaper.forEach {
                        imageList.add(it.wp)
                    }
                }
            }
        }
    }


    private val imageList = mutableListOf<String>()

}
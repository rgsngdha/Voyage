package com.manchuan.tools.activity.images

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.fileProviderAuthority
import com.dylanc.longan.textString
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.gyf.immersionbar.ktx.immersionBar
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.activity.images.models.PhoneHistory
import com.manchuan.tools.databinding.ActivityPhoneHistoryBinding
import com.manchuan.tools.databinding.ItemsPreviewsBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.publicPicturesDirPath
import com.manchuan.tools.extensions.savePic
import com.manchuan.tools.user.timeMills
import com.manchuan.tools.utils.UiUtils
import com.manchuan.tools.utils.setWallpaper
import com.mcxiaoke.koi.ext.addToMediaStore
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.File


class PhoneHistoryActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityPhoneHistoryBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersionBar {
            titleBar(binding.toolbar)
            statusBarDarkFont(!UiUtils.isDarkMode())
            transparentBar()
        }
        supportActionBar?.apply {
            title = "手机平板默认壁纸"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.recyclerView.staggered(2).divider {
            orientation = DividerOrientation.GRID
            includeVisible = true
            setDivider(12, true)
        }.setup {
            addType<PhoneHistory.PhoneHistoryItem>(R.layout.items_previews)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemsPreviewsBinding.bind(itemView)
                val model = getModel<PhoneHistory.PhoneHistoryItem>()
                binding.image.load(model.thumbnail, isCrossFade = true, isForceOriginalSize = true)
            }
            onClick(R.id.image) {}
            onLongClick(R.id.image) {
                val model = getModel<PhoneHistory.PhoneHistoryItem>()
                BottomMenu.show("操作", null, listOf("下载", "设为壁纸"))
                    .setOnMenuItemClickListener { dialog, text, index ->
                        when (text) {
                            "下载" -> {
                                savePic(model.url)
                            }

                            "设为壁纸" -> {
                                scopeNetLife {
                                    val file = Get<File>(model.url) {
                                        setDownloadDir(publicPicturesDirPath)
                                        setDownloadMd5Verify(true)
                                        setDownloadFileName("$timeMills.png")
                                        addDownloadListener(object : ProgressListener() {
                                            override fun onProgress(p: Progress) {
                                                runOnUiThread {
                                                    WaitDialog.show(
                                                        "下载中", p.progress().toFloat()
                                                    )
                                                }
                                            }

                                        })
                                    }.await()
                                    WaitDialog.dismiss()
                                    addToMediaStore(file)
                                    setWallpaper(
                                        file.absolutePath, fileProviderAuthority
                                    )
                                }.catch {
                                    TipDialog.show("下载失败", WaitDialog.TYPE.ERROR)
                                }
                            }
                        }
                        false
                    }
            }
        }
        FastScrollerBuilder(binding.recyclerView).useMd2Style().build()
        binding.textField.setEndIconOnClickListener {
            binding.recyclerView.models = wallpaperList.filter {
                it.name.lowercase().contains(binding.editText.textString.lowercase())
            }
        }
        binding.page.onRefresh {
            scope {
                wallpaperList.clear()
                val data =
                    Get<String>("https://www.phonewalls.in/wp-content/uploads/Wallpapers.json").await()
                //Json的解析类对象
                //Json的解析类对象
                val parser = JsonParser()
                //将JSON的String 转成一个JsonArray对象
                val jsonArray: JsonArray = parser.parse(data).asJsonArray
                val gson = Gson()
                //加强for循环遍历JsonArray
                //加强for循环遍历JsonArray
                for (wallpaper in jsonArray) {
                    //使用GSON，直接转成Bean对象
                    val datas = gson.fromJson(wallpaper, PhoneHistory.PhoneHistoryItem::class.java)
                    wallpaperList.add(datas)
                }
                binding.recyclerView.models = wallpaperList
            }
        }.autoRefresh()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private val wallpaperList = arrayListOf<PhoneHistory.PhoneHistoryItem>()

}
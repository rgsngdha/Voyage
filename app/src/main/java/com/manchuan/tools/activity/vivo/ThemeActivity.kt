package com.manchuan.tools.activity.vivo

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.annotation.DrawableRes
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.addModels
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.runMain
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNet
import com.drake.statusbar.immersive
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.PopNotification
import com.lxj.androidktx.core.drawable
import com.manchuan.tools.R
import com.manchuan.tools.activity.vivo.json.VivoTheme
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityThemeBinding
import com.manchuan.tools.databinding.ItemVivoThemeBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.joinGroup
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.utils.UiUtils
import com.mcxiaoke.koi.ext.addToMediaStore
import kotlinx.coroutines.Dispatchers
import java.io.File

class ThemeActivity : BaseActivity() {

    private var selectTypes = "主题"
    private val typesString = arrayListOf<String>()
    private val binding by viewBinding(ActivityThemeBinding::inflate)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersive(binding.toolbar, !UiUtils.isDarkMode())
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "i 主题下载"
            subtitle = "免费下载付费主题"
            setDisplayHomeAsUpEnabled(true)
        }
        types.forEach {
            typesString.add(it.name)
        }
        alertDialog {
            title = "i 主题导入教程"
            message = "vivo主题、字体、息屏导入教程远航官方群群文件内都有，若不会导入的请加群获取。"
            okButton("加群") {
                joinGroup("754591110")
            }
            cancelButton()
        }.build()
        selectTypes = typesString.first()
        val adapter = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, typesString)
        binding.types.setAdapter(adapter)
        binding.types.setOnItemClickListener { parent, view, position, id ->
            selectTypes = typesString[position]
            binding.styleType.apply {
                startIconDrawable = drawable(types[position].icon)
            }
        }
        binding.recyclerView.staggered(2).divider {
            orientation = DividerOrientation.GRID
            setDivider(12, true)
            includeVisible = true
        }.setup {
            addType<VivoTheme.Res>(R.layout.item_vivo_theme)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val model = getModel<VivoTheme.Res>()
                val binding = getBinding<ItemVivoThemeBinding>()
                binding.image.load(
                    model.thumbPath,
                    isCrossFade = true,
                    isForceOriginalSize = true,
                    skipMemory = true
                )
                binding.name.text = model.name
                binding.author.text = model.resAuthor
            }
            R.id.card.onClick {
                val model = getModel<VivoTheme.Res>()
                val binding = getBinding<ItemVivoThemeBinding>()
                selector(listOf("下载"), "操作") { dialogInterface, s, i ->
                    when (s) {
                        "下载" -> {
                            PopNotification.show("下载管理器", "主题 ${model.name} 开始下载")
                                .showLong()
                            scopeNet(Dispatchers.IO) {
                                val file =
                                    Get<File>("http://theme.vivo.com.cn/v3/resource/dl?packageId=${model.packageId}&category=1") {
                                        setDownloadDir(publicDownloadsDirPath)
                                        setDownloadFileNameConflict(true)
                                        setDownloadFileName("${model.name}.itz")
                                        setDownloadMd5Verify(true)
                                        addDownloadListener(object : ProgressListener() {
                                            override fun onProgress(p: Progress) {

                                            }
                                        })
                                    }.await()
                                addToMediaStore(file)
                                runMain {
                                    PopNotification.show(
                                        "下载管理器",
                                        "主题 ${model.name} 下载完成，保存目录:$publicDownloadsDirPath"
                                    ).showLong()
                                    toast("主题 ${model.name} 下载完成，保存目录:$publicDownloadsDirPath")
                                }
                            }.catch {
                                runMain {
                                    PopNotification.show(
                                        "下载管理器", "主题 ${model.name} 下载失败"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        binding.textField.setEndIconOnClickListener {
            if (binding.editText.textString.isNotBlank()) {
                binding.state.autoRefresh()
            } else {
                toast("搜索内容不能为空")
            }
        }
        binding.state.onRefresh {
            scope {
                if (binding.editText.textString.isNotBlank()) {
                    pageIndex = 0
                    val themeData =
                        Get<VivoTheme>("$url?${searchUrl(binding.editText.textString)}") {
                            converter = SerializationConverter("200", "stat", "msg")
                        }.await()
                    if (themeData.resList.isNotEmpty()) {
                        binding.recyclerView.models = themeData.resList
                    } else {
                        binding.state.showEmpty()
                    }
                } else {
                    toast("搜索内容为空")
                    binding.state.finish(false)
                }
            }
        }
        binding.state.onLoadMore {
            scope {
                if (binding.editText.textString.isNotBlank()) {
                    pageIndex = pageIndex.inc()
                    val themeData =
                        Get<VivoTheme>("$url?${searchUrl(binding.editText.textString)}") {
                            converter = SerializationConverter("200", "stat", "msg")
                        }.await()
                    if (themeData.resList.isNotEmpty()) {
                        binding.recyclerView.addModels(themeData.resList)
                    } else {
                        binding.state.finishLoadMore(delayed = 0, success = true, noMoreData = true)
                    }
                } else {
                    toast("搜索内容为空")
                    binding.state.finish(false)
                }
            }
        }
    }

    private var pageIndex = 0

    private fun searchUrl(content: String): String {
        return when (selectTypes) {
            "主题" -> "model=vivoX21A&mktprdmodel=vivoX21A&promodel=PD1728&imei=869704036083738&appversion=6.3.8.0&appvercode=6380&e=1234567890&apppkgName=com.bbk.theme&av=28&adrVerName=9&timestamp=1587482919020&pixel=3.0&cs=0&locale=zh_CN&themetype=1&elapsedtime=592960661&width=1080&height=2280&romVer=4.2&sysVer=PD1728_A_7.9.0&sysromver=9&tt=1&nightpearlResVersion=3.1.0&isShowClock=1&requestId=7048651e-4d95-49a8-a6a8-7bae98ad8471&requestTime=1587482919020&hots=$content&pageIndex=$pageIndex&setId=-1&flag=11&cfrom=819"
            "息屏" -> "model=vivoX21A&mktprdmodel=vivoX21A&promodel=PD1728&imei=869704036083738&appversion=6.3.8.0&appvercode=6380&e=1234567890&apppkgName=com.bbk.theme&av=28&adrVerName=9&timestamp=1587300222863&pixel=3.0&cs=0&locale=zh_CN&themetype=7&elapsedtime=408778567&width=1080&height=2280&romVer=4.2&sysVer=PD1728_A_7.9.0&sysromver=9&tt=7&nightpearlResVersion=3.1.0&isShowClock=1&requestId=4eb648bc-5426-443c-a524-0c58a2f9d7cf&requestTime=1587300222863&hots=$content&pageIndex=$pageIndex&setId=-1&flag=11&cfrom=819"
            "字体" -> "model=vivoX21A&mktprdmodel=vivoX21A&promodel=PD1728&imei=869704036083738&appversion=6.3.8.0&appvercode=6380&e=1234567890&apppkgName=com.bbk.theme&av=28&adrVerName=9&timestamp=1587512320557&pixel=3.0&cs=0&locale=zh_CN&themetype=4&elapsedtime=15533037&width=1080&height=2280&romVer=4.2&sysVer=PD1728_A_7.9.0&sysromver=9&tt=4&nightpearlResVersion=3.1.0&isShowClock=1&requestId=13302d09-679e-4992-9f9d-419b4cbc849a&requestTime=1587512320557&hots=$content&pageIndex=$pageIndex&setId=-1&flag=11&cfrom=826"
            else -> ""
        }
    }

    private val url = "https://stheme.vivo.com.cn/api14.do"
    private var data = ""

    private val types = mutableListOf(
        VivoThemeTypes(R.drawable.ic_round_color_24, "主题"),
        VivoThemeTypes(R.drawable.ic_primary_font_download_24, "字体"),
        VivoThemeTypes(R.drawable.ic_baseline_phone_android_24, "息屏")
    )

    data class VivoThemeTypes(@DrawableRes var icon: Int, var name: String)

}
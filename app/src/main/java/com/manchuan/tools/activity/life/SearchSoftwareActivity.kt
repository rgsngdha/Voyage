package com.manchuan.tools.activity.life

import android.os.Bundle
import com.crazylegend.kotlinextensions.views.snack
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.addModels
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.kongzue.dialogx.dialogs.PopNotification
import com.manchuan.tools.R
import com.manchuan.tools.activity.life.model.SearchSoftware
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivitySearchsSoftwareBinding
import com.manchuan.tools.databinding.ItemAppsRecyclerBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.json.SerializationConverter
import java.io.File

class SearchSoftwareActivity : BaseActivity() {

    private var pageCounts = 0;

    private val binding by viewBinding(ActivitySearchsSoftwareBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!isAppDarkMode)
        setSupportActionBar(binding.toolbar)
        with(binding) {
            supportActionBar?.apply {
                title = "软件搜索"
                setDisplayHomeAsUpEnabled(true)
                subtitle = "海量破解版应用/游戏任你搜索"
            }
            recyclerApps.linear().divider {
                orientation = DividerOrientation.HORIZONTAL
                setDivider(8, true)
            }.setup {
                addType<SearchSoftware.Gameapp>(R.layout.item_apps_recycler)
                onBind {
                    val model = getModel<SearchSoftware.Gameapp>()
                    val binding = getBinding<ItemAppsRecyclerBinding>()
                    binding.icon.load(model.applogo, isCrossFade = true)
                    binding.title.text = model.apptitle
                    binding.summary.text = "${model.categoryname} ${model.appsize}MB"
                }
                R.id.item.onFastClick {
                    val model = getModel<SearchSoftware.Gameapp>()
                    alertDialog {
                        title = "应用详情"
                        message = "应用介绍:${model.appdesc} 破解内容:${model.appcrackdesc}"
                        okButton(R.string.download) {
                            scopeNetLife {
                                PopNotification.show("下载管理器", "${model.apptitle} 开始下载")
                                val file = Get<File>(model.localurl.url) {
                                    setDownloadFileNameDecode(true)
                                    setDownloadFileNameConflict(true)
                                    setDownloadDir(publicDownloadsDirPath)
                                    addDownloadListener(object : ProgressListener() {
                                        override fun onProgress(p: Progress) {

                                        }

                                    })
                                }.await()
                                PopNotification.show(
                                    "下载管理器",
                                    "${model.apptitle} 下载完成，已保存到:${file.absolutePath}"
                                )
                            }
                        }
                    }.build()
                }
            }
            state.onRefresh {
                scope {
                    if (appName.isTextNotEmpty()) {
                        val appLists =
                            Get<SearchSoftware>("http://search.huluxia.com/game/search/ANDROID/4.1.5") {
                                param("platform", 2)
                                param("gkey", "000000")
                                param("app_version", "4.2.1.6")
                                param("versioncode", "366")
                                param("market_id", "tool_web")
                                param(
                                    "_key",
                                    "73472D95B52AF45D14A69C49961A78F5BDB873A1EFC3CC66EE4C7CDE1D7CF3C8BC57CE91E016157D732A4A22A0416B6D069DB72178B24EFA"
                                )
                                param("device_code", "[d]748319f7-cf43-4372-8a14-a2bee901c562")
                                param("phone_brand_type", "OP")
                                param("start", 0)
                                param("count", 20)
                                param("keyword", appName.textString)
                                converter = SerializationConverter("1", "status", "msg")
                            }.await()
                        recyclerApps.models = appLists.gameapps
                    } else {
                        finishRefresh(false)
                        snack("应用名称不能为空")
                    }
                }
            }
            state.onLoadMore {
                scope {
                    if (appName.isTextNotEmpty()) {
                        val appLists =
                            Get<SearchSoftware>("http://search.huluxia.com/game/search/ANDROID/4.1.5") {
                                param("platform", 2)
                                param("gkey", "000000")
                                param("app_version", "4.2.1.6")
                                param("versioncode", "366")
                                param("market_id", "tool_web")
                                param(
                                    "_key",
                                    "73472D95B52AF45D14A69C49961A78F5BDB873A1EFC3CC66EE4C7CDE1D7CF3C8BC57CE91E016157D732A4A22A0416B6D069DB72178B24EFA"
                                )
                                param("device_code", "[d]748319f7-cf43-4372-8a14-a2bee901c562")
                                param("phone_brand_type", "OP")
                                param("start", pageCounts)
                                param("count", 20)
                                param("keyword", appName.textString)
                                converter = SerializationConverter("1", "status", "msg")
                            }.await()
                        recyclerApps.addModels(appLists.gameapps.distinct())
                        pageCounts += 20
                    } else {
                        finishRefresh(false)
                        snack("应用名称不能为空")
                    }
                }
            }
            searchFab.throttleClick {
                if (appName.isTextNotEmpty()) {
                    state.refresh()
                } else {
                    snack("应用名称不能为空")
                }
            }
        }
    }
}
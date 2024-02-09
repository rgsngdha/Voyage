package com.manchuan.tools.activity.movies

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import com.crazylegend.viewbinding.viewBinding
import com.drake.channel.sendEvent
import com.drake.engine.utils.throttleClick
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.context
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isJson
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.isWebUrl
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.textString
import com.dylanc.longan.withArguments
import com.google.android.material.tabs.TabLayout
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.views.ActivityScreenShotImageView
import com.lxj.androidktx.FragmentStateAdapter
import com.lxj.androidktx.core.postDelay
import com.lxj.androidktx.setupWithViewPager2
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.database.SourceType
import com.manchuan.tools.activity.movies.database.SourcesDatabase
import com.manchuan.tools.activity.movies.database.SubscribeList
import com.manchuan.tools.activity.movies.fragments.SiteSourcesFragment
import com.manchuan.tools.activity.movies.fragments.VideoParserFragment
import com.manchuan.tools.activity.movies.model.AHModel
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivitySubscribeListBinding
import com.manchuan.tools.databinding.SubscribeSiteBinding
import com.manchuan.tools.extensions.firstClipboardText
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.extensions.windowBackground
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.Validation
import com.maxkeppeler.sheets.input.type.InputEditText
import com.mcxiaoke.koi.ext.toast
import com.nowfal.kdroidext.kex.afterTextChanged
import com.wajahatkarim3.easyvalidation.core.view_ktx.validUrl
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json


class SubscribeListActivity : BaseActivity() {

    private val binding by viewBinding(ActivitySubscribeListBinding::inflate)

    private val sourcesDatabase by lazy {
        SourcesDatabase.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val id = safeIntentExtras<Int>("id")
        supportActionBar?.apply {
            title = "管理 ${sourcesDatabase.getSourcesDao().getSourceById(id.value).name}"
        }
        immerseStatusBar(!isAppDarkMode)
        binding.viewPager.adapter = FragmentStateAdapter(
            SiteSourcesFragment().withArguments("id" to id.value),
            VideoParserFragment().withArguments("id" to id.value),
            isLazyLoading = false
        )
        val category = listOf("站源", "解析接口")
        binding.tab.setupWithViewPager2(binding.viewPager,
            autoRefresh = true,
            enableScroll = true,
            tabConfigurationStrategy = { tab: TabLayout.Tab, i: Int ->
                tab.text = category[i]
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.subscribes_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishAfterTransition()
            R.id.add_video_tag -> sendEvent("", "create_video_tag")
            R.id.add_video_parser -> sendEvent("", "create_video_parser")
            R.id.add_live -> {
                InputSheet().show(context) {
                    title("添加直播")
                    with(InputEditText("name") {
                        required()
                        label("必填")
                        hint("直播名称")
                    })
                    with(InputEditText("pic") {
                        required()
                        hint("海报")
                    })
                    with(InputEditText("url") {
                        required()
                        label("http(s):// 开头")
                        hint("直播链接")
                        inputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT)
                        validationListener { value ->
                            if (value.toString().isWebUrl().not()) {
                                Validation.failed("错误的网址")
                            } else Validation.success()
                        }
                    })
                    onPositive { result ->
                        val name = result.getString("name")
                        val ua = result.getString("ua")
                        val url = result.getString("url")
                        val entity = sourcesDatabase.getSourcesDao()
                            .getSourceById(safeIntentExtras<Int>("id").value)
                        entity.sources?.add(
                            SubscribeList(
                                name = name!!,
                                sourceType = SourceType.LIVE,
                                poster = ua.toString(),
                                playUrl = url.toString()
                            )
                        )
                        sourcesDatabase.getSourcesDao().updateSource(entity)
                    }
                }
            }

            R.id.add_site -> {
                ActivityScreenShotImageView.hideContentView = true;
                FullScreenDialog.show(object :
                    OnBindView<FullScreenDialog>(R.layout.subscribe_site) {
                    override fun onBind(dialog: FullScreenDialog?, v: View?) {
                        val siteBinding = SubscribeSiteBinding.bind(v!!)
                        siteBinding.root.addStatusBarHeightToMarginTop()
                        siteBinding.searchUrl.afterTextChanged { editable ->
                            //loge(editable?.toString())
                            editable?.toString()?.let {
                                siteBinding.searchUrlInput.isErrorEnabled =
                                    !it.validUrl { callback ->
                                        loge(tag = "搜索url", callback)
                                        siteBinding.searchUrlInput.error = callback
                                    }
                            }
                        }
                        fun check() {
                            siteBinding.name.validator().nonEmpty().addErrorCallback {
                                siteBinding.nameInput.isErrorEnabled = true
                                siteBinding.nameInput.error = it
                            }.addSuccessCallback {
                                siteBinding.nameInput.isErrorEnabled = false
                            }.check()
                            siteBinding.movieName.validator().nonEmpty().addErrorCallback {
                                siteBinding.movieNameInput.isErrorEnabled = true
                                siteBinding.movieNameInput.error = it
                            }.addSuccessCallback {
                                siteBinding.movieNameInput.isErrorEnabled = false
                            }.check()
                            siteBinding.detailUrl.validator().nonEmpty().addErrorCallback {
                                siteBinding.detailUrlInput.isErrorEnabled = true
                                siteBinding.detailUrlInput.error = it
                            }.addSuccessCallback {
                                siteBinding.detailUrlInput.isErrorEnabled = false
                            }.check()
                            siteBinding.poster.validator().nonEmpty().addErrorCallback {
                                siteBinding.posterInput.isErrorEnabled = true
                                siteBinding.posterInput.error = it
                            }.addSuccessCallback {
                                siteBinding.posterInput.isErrorEnabled = false
                            }.check()
                            siteBinding.line.validator().nonEmpty().addErrorCallback {
                                siteBinding.lineInput.isErrorEnabled = true
                                siteBinding.lineInput.error = it
                            }.addSuccessCallback {
                                siteBinding.lineInput.isErrorEnabled = false
                            }.check()
                            siteBinding.lineName.validator().nonEmpty().addErrorCallback {
                                siteBinding.lineNameInput.isErrorEnabled = true
                                siteBinding.lineNameInput.error = it
                            }.addSuccessCallback {
                                siteBinding.lineNameInput.isErrorEnabled = false
                            }.check()
                            siteBinding.episodeName.validator().nonEmpty().addErrorCallback {
                                siteBinding.episodeNameInput.isErrorEnabled = true
                                siteBinding.episodeNameInput.error = it
                            }.addSuccessCallback {
                                siteBinding.episodeNameInput.isErrorEnabled = false
                            }.check()
                            siteBinding.videoUrl.validator().nonEmpty().addErrorCallback {
                                siteBinding.videoInput.isErrorEnabled = true
                                siteBinding.videoInput.error = it
                            }.addSuccessCallback {
                                siteBinding.videoInput.isErrorEnabled = false
                            }.check()
                        }
                        siteBinding.paste.setOnClickListener {
                            postDelay(100) {
                                //loge(firstClipboardText().replace("\\", ""))
                                if (firstClipboardText().isJson()) {
                                    runCatching {
                                        val json = Json {
                                            explicitNulls = true
                                            ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
                                            coerceInputValues = true // 如果JSON字段是Null则使用默认值
                                        }
                                        val model: AHModel =
                                            json.decodeFromString(firstClipboardText())
                                        siteBinding.name.text(model.源名字)
                                        if (model.搜索URL.isNotEmpty()) siteBinding.searchUrl.text(
                                            model.搜索URL.substringBefore(
                                                "fansearch"
                                            ).substringBefore(
                                                "search"
                                            ).substringBefore("vod").substringBefore("index.php/")
                                                .substringBefore("?").substringBefore("s/")
                                                .substringBefore("so/")
                                        )
                                        if (model.搜索URL.isNotEmpty()) {
                                            siteBinding.searchParam.text(
                                                model.搜索URL.substringAfter(
                                                    ".com/"
                                                ).substringAfter(".top/").substringAfter(".cn/")
                                                    .substringAfter(".xyz/").substringAfter(".net/")
                                                    .substringAfter(".store/")
                                                    .substringAfter(".love/")
                                                    .substringAfter(".vip/").substringAfter(".icu/")
                                                    .substringAfter(".cc/").substringAfter(".gay/")
                                                    .substringAfter(".club/")
                                                    .substringAfter(".vip/").substringAfter(".ltd/")
                                                    .substringAfter(".wang/")
                                                    .substringAfter(".fans/")
                                                    .substringAfter(".online/")
                                                    .substringAfter(".pub/")
                                                    .substringAfter(".live/").substringAfter(".me/")
                                                    .substringAfter(".tv/").substringAfter(".tk/")
                                                    .substringAfter(".fun/").substringAfter(".ml/")
                                                    .substringAfter(".pro/")
                                                    .substringAfter(".name/")
                                                    .substringAfter(".aero/")
                                                    .substringAfter(".xxx/").substringAfter(".idv/")
                                                    .substringAfter(".org/")
                                                    .substringAfter(".info/")
                                                    .substringAfter(".coop/")
                                                    .substringAfter(".biz/")
                                            )
                                        }
                                        if (model.搜索API.isNotEmpty()) siteBinding.searchApi.text(
                                            model.搜索API.trim()
                                        )
                                        if (model.pOST参数.isNotEmpty()) siteBinding.postParam.text(
                                            model.pOST参数.substringBefore(
                                                "={name}"
                                            )
                                        )
                                        if (model.影片名规则.isNotEmpty()) siteBinding.movieName.text(
                                            model.影片名规则.trim()
                                        )
                                        if (model.影片状态规则.isNotEmpty()) siteBinding.movieStatus.text(
                                            model.影片状态规则.trim()
                                        )
                                        if (model.海报规则.isNotEmpty()) siteBinding.poster.text(
                                            model.海报规则.trim()
                                        )
                                        if (model.演员获取规则.isNotEmpty()) siteBinding.actor.text(
                                            model.演员获取规则.trim()
                                        )
                                        if (model.影片简介.isNotEmpty()) siteBinding.introduce.text(
                                            model.影片简介.trim()
                                        )
                                        if (model.列表规则.isNotEmpty()) siteBinding.line.text(model.列表规则.trim())
                                        if (model.线路名规则.isNotEmpty()) siteBinding.lineName.text(
                                            model.线路名规则.trim()
                                        )
                                        if (model.集数名规则.isNotEmpty()) siteBinding.episodeName.text(
                                            model.集数名规则.trim()
                                        )
                                        if (model.视频链接规则.isNotEmpty()) siteBinding.videoUrl.text(
                                            model.视频链接规则.trim()
                                        )
                                        if (model.网页选集链接.isNotEmpty()) siteBinding.selectUrl.text(
                                            model.网页选集链接.trim()
                                        )
                                        if (model.详情页链接规则.isNotEmpty()) siteBinding.detailUrl.text(
                                            model.详情页链接规则.trim()
                                        )
                                        if (model.详情影片状态规则.isNotEmpty()) siteBinding.detailStatus.text(
                                            model.详情影片状态规则.trim()
                                        )
                                    }.onFailure {
                                        toast("失败:$it")
                                    }
                                } else {
                                    toast("只支持AH视频规则导入")
                                }
                            }
                        }
                        siteBinding.save.throttleClick {
                            if (siteBinding.searchUrl.isTextEmpty() and siteBinding.searchApi.isTextEmpty()) {
                                toast("搜索URL和API必须填写其中一项")
                            } else if (siteBinding.name.isTextEmpty() || siteBinding.line.isTextEmpty() || siteBinding.lineName.isTextEmpty() || siteBinding.episodeName.isTextEmpty() || siteBinding.videoUrl.isTextEmpty()) {
                                toast("缺少必填内容")
                                check()
                            } else {
                                runCatching {
                                    val entity = sourcesDatabase.getSourcesDao()
                                        .getSourceById(safeIntentExtras<Int>("id").value)
                                    entity.sources?.add(SubscribeList(name = siteBinding.name.textString,
                                        sourceType = SourceType.SITE,
                                        searchUa = siteBinding.userAgent.textString.ifEmpty { userAgent() }
                                            .toString(),
                                        searchUrl = siteBinding.searchUrl.textString.ifEmpty { "" },
                                        searchParam = siteBinding.searchParam.textString.ifEmpty { "" },
                                        postParam = siteBinding.postParam.textString.ifEmpty { "" },
                                        movieNameRule = siteBinding.movieName.textString,
                                        detailPageUrlRule = siteBinding.detailUrl.textString,
                                        posterRule = siteBinding.poster.textString,
                                        movieStatusRule = siteBinding.movieStatus.textString.ifEmpty { "" },
                                        actorRule = siteBinding.actor.textString.ifEmpty { "" },
                                        movieIntroduce = siteBinding.introduce.textString.ifEmpty { "" },
                                        listRule = siteBinding.line.textString,
                                        lineNameRule = siteBinding.lineName.textString,
                                        episodeNameRule = siteBinding.episodeName.textString,
                                        videoUrlRule = siteBinding.videoUrl.textString,
                                        detailMovieStatusRule = siteBinding.detailStatus.textString.ifEmpty { "" },
                                        searchApi = siteBinding.searchApi.textString.ifEmpty { "" },
                                        selectEpisodeUrl = siteBinding.selectUrl.textString.ifEmpty { "" })
                                    )
                                    sourcesDatabase.getSourcesDao().updateSource(entity)
                                }.onSuccess {
                                    dialog?.dismiss()
                                    toast("保存成功")
                                }.onFailure {
                                    toast("保存失败:${it}")
                                }
                            }
                        }
                    }
                }).setBackgroundColor(windowBackground())
            }

            R.id.add_collection -> {
                InputSheet().show(context) {
                    title("添加采集站")
                    with(InputEditText("name") {
                        required()
                        label("必填")
                        hint("采集站名称")
                    })
                    with(InputEditText("ua") {
                        hint("搜索UA")
                        label("默认 Dart/2.14(dart:io)")
                    })
                    with(InputEditText("url") {
                        required()
                        label("http(s):// 开头")
                        hint("请求链接")
                        inputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT)
                        validationListener { value ->
                            if (value.toString().isWebUrl().not()) {
                                Validation.failed("错误的网址")
                            } else Validation.success()
                        }
                    })
                    onPositive { result ->
                        val name = result.getString("name")
                        val ua = result.getString("ua")
                        val url = result.getString("url")
                        val entity = sourcesDatabase.getSourcesDao()
                            .getSourceById(safeIntentExtras<Int>("id").value)
                        entity.sources?.add(
                            SubscribeList(
                                name = name!!,
                                sourceType = SourceType.COLLECTION,
                                searchUa = ua?.ifEmpty { "" }.toString(),
                                searchUrl = url.toString()
                            )
                        )
                        sourcesDatabase.getSourcesDao().updateSource(entity)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
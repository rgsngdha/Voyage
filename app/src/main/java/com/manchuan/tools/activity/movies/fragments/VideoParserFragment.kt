package com.manchuan.tools.activity.movies.fragments

import android.text.InputType
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.isWebUrl
import com.dylanc.longan.safeArguments
import com.lxj.androidktx.core.postDelay
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.database.SourcesDatabase
import com.manchuan.tools.activity.movies.database.SubVideoParser
import com.manchuan.tools.activity.movies.database.VideoParse
import com.manchuan.tools.activity.movies.database.VideoParseType
import com.manchuan.tools.databinding.FragmentVideoParserBinding
import com.manchuan.tools.databinding.ItemSubscribeListBinding
import com.manchuan.tools.extensions.accentColorVariant
import com.manchuan.tools.extensions.inputDialog
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.textColorSecondary
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.Validation
import com.maxkeppeler.sheets.input.type.InputEditText
import com.skydoves.whatif.whatIfNotNull
import q.rorbin.verticaltablayout.VerticalTabLayout
import q.rorbin.verticaltablayout.widget.ITabView
import q.rorbin.verticaltablayout.widget.QTabView
import q.rorbin.verticaltablayout.widget.TabView


class VideoParserFragment :
    EngineFragment<FragmentVideoParserBinding>(R.layout.fragment_video_parser) {

    private val sourcesDatabase by lazy {
        SourcesDatabase.getInstance(requireContext())
    }

    private val ids by safeArguments<Int>("id")

    override fun initData() {
        refreshVideoParser()
    }

    private lateinit var currentVideoParse: SubVideoParser

    override fun initView() {
        binding.recyclerView.linear().divider {
            orientation = DividerOrientation.GRID
            includeVisible = true
            setDivider(12, true)
        }.setup {
            addType<VideoParse>(R.layout.item_subscribe_list)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemSubscribeListBinding.bind(itemView)
                val model = getModel<VideoParse>()
                binding.name.text = model.name
                binding.type.text = if (model.type == VideoParseType.JSON) "Json" else "嗅探"
            }
            onClick(R.id.item) {

            }
            onLongClick(R.id.item) {
                val model = getModel<VideoParse>()
                selector(listOf("删除规则"), model.name) { dialogInterface, s, i ->
                    when (s) {
                        "删除规则" -> {
                            currentVideoParse.whatIfNotNull {
                                val entity = sourcesDatabase.getSourcesDao().getSourceById(ids)
                                entity.videoParse?.apply {
                                    find { it.name == currentVideoParse.name }?.videoParse?.remove(
                                        model
                                    )
                                }
                                sourcesDatabase.getSourcesDao().updateSource(entity)
                            }
                        }
                    }
                }
            }
        }
        binding.state.setEnableLoadMore(false)
        binding.tabs.addOnTabSelectedListener(object : VerticalTabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabView?, position: Int) {
                loge(tag = "位置", position)
                val videoParser = (sourcesDatabase.getSourcesDao()
                    .getSourceById(ids).videoParse?.filter { it.name == tab?.title?.content })?.get(
                        0
                    )?.videoParse
                currentVideoParse = (sourcesDatabase.getSourcesDao()
                    .getSourceById(ids).videoParse?.filter { it.name == tab?.title?.content })?.get(
                        0
                    )!!
                loge(tag = "videoparser获取", videoParser)
                binding.recyclerView.models = videoParser
            }

            override fun onTabReselected(tab: TabView?, position: Int) {

            }

        })
        receiveEvent<String>("create_video_tag") {
            inputDialog("添加解析关键词", "请输入关键词，如www.qq.com，可填qq", "添加") { inputStr ->
                if (inputStr.isNotBlank()) {
                    val entity = sourcesDatabase.getSourcesDao().getSourceById(ids)
                    entity.videoParse?.add(
                        SubVideoParser(
                            inputStr, mutableListOf()
                        )
                    )
                    sourcesDatabase.getSourcesDao().updateSource(entity)
                    refreshVideoParser()
                } else {
                    snack("关键词不能为空")
                }
            }
        }
        receiveEvent<String>("create_video_parser") {
            selector(listOf("JSON", "网页"), "解析接口返回类型") { dialogInterface, s, i ->
                when (s) {
                    "JSON" -> {
                        InputSheet().show(requireContext()) {
                            title("添加JSON解析接口")
                            with(InputEditText("name") {
                                required()
                                label("必填")
                                hint("解析接口名称")
                            })
                            with(InputEditText("ua") {
                                label("请求 User-Agent，不填写则为系统默认")
                                hint("User-Agent")
                            })
                            with(InputEditText("url") {
                                required()
                                label("http(s):// 开头")
                                hint("解析接口链接")
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
                                val entity = sourcesDatabase.getSourcesDao().getSourceById(ids)
                                entity.videoParse?.let {
                                    it[it.indexOf(currentVideoParse)].videoParse.add(
                                        VideoParse(
                                            name.toString(),
                                            url = url.toString(),
                                            ua = ua.toString(),
                                            type = VideoParseType.JSON
                                        )
                                    )
                                }
                                sourcesDatabase.getSourcesDao().updateSource(entity)
                                refreshVideoParser()
                            }
                        }
                    }

                    "网页" -> {
                        InputSheet().show(requireContext()) {
                            title("添加解析接口")
                            with(InputEditText("name") {
                                required()
                                label("必填")
                                hint("解析接口名称")
                            })
                            with(InputEditText("ua") {
                                label("请求 User-Agent，不填写则为系统默认")
                                hint("User-Agent")
                            })
                            with(InputEditText("url") {
                                required()
                                label("http(s):// 开头")
                                hint("解析接口链接")
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
                                val entity = sourcesDatabase.getSourcesDao().getSourceById(ids)
                                entity.videoParse?.let {
                                    it[it.indexOf(currentVideoParse)].videoParse.add(
                                        VideoParse(
                                            name.toString(),
                                            url = url.toString(),
                                            ua = ua.toString(),
                                            type = VideoParseType.SNIFFING
                                        )
                                    )
                                }
                                sourcesDatabase.getSourcesDao().updateSource(entity)
                                refreshVideoParser()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun refreshVideoParser() {
        if (sourcesDatabase.getSourcesDao().getSourceById(ids).videoParse?.isEmpty() == true) {
            binding.rootState.showEmpty()
        } else {
            binding.rootState.showContent()
            binding.tabs.removeAllTabs()
            sourcesDatabase.getSourcesDao().getSourceById(ids).videoParse!!.forEach {
                binding.tabs.addTab(
                    QTabView(context).setTitle(
                        ITabView.TabTitle.Builder().setContent(it.name).setTextColor(
                            requireContext().accentColorVariant(), textColorSecondary()
                        ).build()
                    )
                )
            }
            postDelay(200) {
                binding.tabs.setTabSelected(0, true)
            }
        }
    }

}
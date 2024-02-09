package com.manchuan.tools.activity.movies

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import androidx.activity.addCallback
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.interval.Interval
import com.drake.net.Get
import com.drake.net.cache.CacheMode
import com.drake.net.utils.scopeNet
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.context
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isWebUrl
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.startActivity
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.dylanc.longan.topActivity
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.color
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.database.SourceType
import com.manchuan.tools.activity.movies.database.SourcesDatabase
import com.manchuan.tools.activity.movies.model.HotQuery
import com.manchuan.tools.activity.movies.model.MovieCount
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivitySearchMovieBinding
import com.manchuan.tools.databinding.ItemHotQueryBinding
import com.manchuan.tools.databinding.ItemSearchMovieBinding
import com.manchuan.tools.extensions.accentColorVariant
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.setTextWithAnimation
import com.manchuan.tools.extensions.startActivity
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.textColorSecondary
import com.manchuan.tools.extensions.tryWith
import com.manchuan.tools.extensions.urlEncoded
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.json.SerializationConverter
import org.seimicrawler.xpath.JXDocument
import q.rorbin.verticaltablayout.VerticalTabLayout
import q.rorbin.verticaltablayout.widget.ITabView.TabTitle
import q.rorbin.verticaltablayout.widget.QTabView
import q.rorbin.verticaltablayout.widget.TabView
import java.util.concurrent.TimeUnit

class SearchMovieActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySearchMovieBinding.inflate(layoutInflater)
    }

    private val sourcesDatabase by lazy {
        SourcesDatabase.getInstance(this)
    }

    private val allMoviesCount = mutableSetOf<MovieCount>()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.searchBar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        binding.hotQuery.grid(2).setup {
            addType<HotQuery.Data>(R.layout.item_hot_query)
            onBind {
                val binding = ItemHotQueryBinding.bind(itemView)
                val model = getModel<HotQuery.Data>()
                binding.content.text = model.query
                binding.num.text = modelPosition.inc().toString()
                when (binding.num.textString) {
                    "1" -> binding.num.setTextColor(Color.RED)
                    "2" -> binding.num.setTextColor(color(rikka.material.R.color.material_orange_500))
                    "3" -> binding.num.setTextColor(color(rikka.material.R.color.material_blue_500))
                }
            }
            onClick(R.id.item) {
                val model = getModel<HotQuery.Data>()
                binding.searchBar.setTextWithAnimation(model.query)
                search(model.query)
            }
        }
        scopeNetLife {
            binding.hotQuery.models =
                Get<HotQuery>("https://search.video.iqiyi.com/m?if=hotQuery&p=15&is_qipu_platform=1") {
                    converter = SerializationConverter("false", "is_empty", "is_empty")
                }.await().data
        }
        binding.searchResult.grid(2).setup {
            addType<MovieCount>(R.layout.item_search_movie)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val count = ItemSearchMovieBinding.bind(itemView)
                val countModel = getModel<MovieCount>()
                count.summary.text = countModel.sourceName
                count.name.text = countModel.name
                count.year.text = countModel.remarks
                count.image.load(
                    countModel.image,
                    placeholder = R.drawable.placeholder,
                    isCrossFade = true,
                )
            }
            R.id.movie.onFastClick {
                val countModel = getModel<MovieCount>()
                startActivity<VideoPlayerActivity>(
                    "url" to countModel.playUrl,
                    "category" to countModel.category,
                    "image" to countModel.image,
                    "name" to countModel.name,
                    "jsonString" to countModel.json,
                    "searchUrl" to countModel.searchUrl,
                    "sourcesType" to countModel.sourceType,
                    "videoParser" to countModel.videoParser,
                    "list" to countModel.subscribeModel
                )
                runCatching {
                    VideoPlayerActivity.movieCount = countModel
                    VideoPlayerActivity.subscribeList = countModel.subscribeModel!!
                }
                loge(countModel.subscribeModel)
            }
        }
        binding.tabs.addOnTabSelectedListener(object : VerticalTabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabView?, position: Int) {
                binding.searchResult.models =
                    allMoviesCount.filter { it.sourceName == tab?.title?.content }
                loge("选中数据", allMoviesCount.filter { it.sourceName == tab?.title?.content })
            }

            override fun onTabReselected(tab: TabView?, position: Int) {

            }

        })
        runCatching {
            if (ids.isNotEmpty()) {
                search(ids)
                binding.searchBar.text(ids)
            }
        }
        binding.searchView.editText.setOnEditorActionListener { v, actionId, event ->
            search(v.textString)
            true
        }
        onBackPressedDispatcher.addCallback {
            if (binding.searchView.isShowing) {
                binding.searchView.hide()
            } else {
                finish()
            }
        }
    }

    private val ids: String by safeIntentExtras("name")

    companion object {
        fun start(id: String) {
            topActivity.startActivity<SearchMovieActivity>(bundle = arrayOf("name" to id))
        }
    }


    private fun search(name: String) {
        binding.searchView.show()
        binding.searchView.editText.text(name)
        binding.hotLay.animateGone()
        binding.state.animateVisible()
        allSources.clear()
        allMoviesCount.clear()
        if (sourcesDatabase.getSourcesDao().queryAllSources().isEmpty()) {
            binding.state.showEmpty()
            toast("无订阅源，请添加订阅源后再搜索。")
        } else {
            runCatching {
                binding.tabs.removeAllTabs()
            }
            binding.searchResult.models = emptyList()
            Interval(600, TimeUnit.MILLISECONDS).subscribe {
                if (allMoviesCount.isNotEmpty()) {
                    binding.state.showContent()
                    binding.tabs.setTabSelected(0, true)
                    binding.searchResult.models =
                        allMoviesCount.filter { it.sourceName == binding.tabs.getTabAt(0)?.title?.content }
                    stop()
                }
            }.start()
            sourcesDatabase.getSourcesDao().queryAllSources().forEach { sourceEntity ->
                sourceEntity.sources?.forEach { subscribeList ->
                    if (sourceEntity.sourceSwitch) {
                        loge(subscribeList)
                        when (subscribeList.sourceType) {
                            SourceType.SITE -> {
                                scopeNet {
                                    val string = Get<String>(
                                        if (subscribeList.searchUrl.isNotEmpty() and subscribeList.searchApi.isNotEmpty()) subscribeList.searchApi + "/index.php/ajax/suggest?mid=1&wd=$name" else if (subscribeList.searchUrl.isNotEmpty()) subscribeList.searchUrl + subscribeList.searchParam.replace(
                                            "{name}", name
                                        ) else subscribeList.searchApi + "/index.php/ajax/suggest?mid=1&wd=$name"
                                    ) {
                                        if (subscribeList.postParam.isNotEmpty()) {
                                            param(subscribeList.postParam, name)
                                        }
                                        setCacheMode(CacheMode.READ_THEN_REQUEST)
                                        setHeader("User-Agent",
                                            subscribeList.searchUa.ifEmpty { userAgent() })
                                    }.await()
                                    val jxDocument = JXDocument.create(string)
                                    if (subscribeList.searchParam.isNotEmpty() && jxDocument.selN(
                                            subscribeList.movieNameRule
                                        ).isNotEmpty()
                                    ) {
                                        val moviesName =
                                            jxDocument.selN(subscribeList.movieNameRule)
                                        val poster = jxDocument.selN(subscribeList.posterRule)
                                        val movieStatus =
                                            jxDocument.selN(subscribeList.movieStatusRule.ifEmpty { "" })
                                        val detailPageUrl =
                                            jxDocument.selN(subscribeList.detailPageUrlRule)
                                        if (moviesName.isNotEmpty() && poster.isNotEmpty()) {
                                            val tab = QTabView(context).setTitle(
                                                TabTitle.Builder().setContent(subscribeList.name)
                                                    .setTextColor(
                                                        accentColorVariant(), textColorSecondary()
                                                    ).build()
                                            )
                                            if (allSources.find { it.title == tab.title } == null) {
                                                allSources.add(tab)
                                                binding.tabs.addTab(tab)
                                            }
                                            moviesName.indices.forEach {
                                                val detailUrl = if (detailPageUrl[it].asString()
                                                        .isNullOrEmpty()
                                                ) "" else if (!detailPageUrl[it].asString()
                                                        .isWebUrl()
                                                ) subscribeList.searchUrl + detailPageUrl[it].asString() else detailPageUrl[it].asString()
                                                loge(
                                                    tag = "电影",
                                                    "归属源:${subscribeList.name}\n影片名:${moviesName[it].asString()}\n海报:${poster[it].asString()}\n详情页链接:${detailUrl}"
                                                )
                                                allMoviesCount.add(MovieCount(subscribeName = sourceEntity.name,
                                                    name = tryWith(call = { moviesName[it].asString() }) { "" }.toString(),
                                                    image = tryWith(call = { poster[it].asString() }) { "" }.toString(),
                                                    playUrl = if (detailUrl.isNullOrEmpty()) "" else detailUrl,
                                                    category = subscribeList.name,
                                                    searchUrl = subscribeList.searchUrl,
                                                    remarks = tryWith(call = { movieStatus[it].asString() }) { "N/A" }.toString(),
                                                    sourceType = SourceType.SITE,
                                                    subscribeModel = subscribeList,
                                                    sourceName = subscribeList.name,
                                                    videoParser = sourceEntity.videoParse
                                                )
                                                )
                                            }
                                        }
                                    } else {
                                        val gson =
                                            JSONObject.parseObject(string).getJSONArray("list")
                                        if (gson.isNotEmpty()) {
                                            val tab = QTabView(context).setTitle(
                                                TabTitle.Builder().setContent(subscribeList.name)
                                                    .setTextColor(
                                                        accentColorVariant(), textColorSecondary()
                                                    ).build()
                                            )

                                            if (allSources.find { it.title == tab.title } == null) {
                                                allSources.add(tab)
                                                binding.tabs.addTab(tab)
                                            }
                                        }
                                        gson.forEach {
                                            val jsonObject = JSONObject.parseObject(it.toString())
                                            val movie =
                                                MovieCount(subscribeName = sourceEntity.name,
                                                    name = jsonObject.getString(
                                                        "name"
                                                    ),
                                                    id = jsonObject.getIntValue("id"),
                                                    sourceType = SourceType.SITE,
                                                    image = jsonObject.getString("pic"),
                                                    category = subscribeList.name,
                                                    remarks = "N/A",
                                                    playUrl = subscribeList.selectEpisodeRule.ifEmpty { subscribeList.selectEpisodeUrl }
                                                        .replace(
                                                            "{id}",
                                                            jsonObject.getIntValue("id").toString()
                                                        ),
                                                    subscribeModel = subscribeList,
                                                    sourceName = subscribeList.name,
                                                    videoParser = sourceEntity.videoParse)
                                            allMoviesCount.add(movie)
                                            loge("${subscribeList.name} 添加数据", movie)
                                        }
                                    }
                                }.catch {
                                    loge(message = it)
                                }
                            }

                            SourceType.COLLECTION -> {
                                val searchUrl = when {
                                    subscribeList.searchUrl.contains("provide/vod") or subscribeList.searchUrl.contains(
                                        "php/provide"
                                    ) -> "${subscribeList.searchUrl}?ac=videolist&wd="

                                    subscribeList.searchUrl.contains("api.php/app") -> "${subscribeList.searchUrl}search?pg=1&text="
                                    subscribeList.searchUrl.contains("xgapp.php/v1") || subscribeList.searchUrl.contains(
                                        "xgapp.php/v2"
                                    ) || subscribeList.searchUrl.contains(
                                        "xgapp.php/v3"
                                    ) -> "${subscribeList.searchUrl}subscribeList?pg=1&text="

                                    subscribeList.searchUrl.contains("api.php/v1") || subscribeList.searchUrl.contains(
                                        "api.php/v2"
                                    ) -> "${subscribeList.searchUrl}search?pg=1&text="

                                    else -> ""
                                }
                                scopeNet {
                                    val content = Get<String>("$searchUrl${name.urlEncoded()}") {
                                        setCacheMode(CacheMode.READ_THEN_REQUEST)
                                        addHeader("User-Agent",
                                            subscribeList.searchUa.ifEmpty { "Dart/2.19 (dart:io)" })
                                    }.await()
                                    var jsonArray = JSONArray()
                                    if (JSON.parseObject(content).toString().contains(name)) {
                                        jsonArray =
                                            if (content.contains("videoName") && content.contains("starName")) {
                                                JSON.parseObject(content).getJSONArray("data")
                                            } else if (content.contains("data") && content.contains(
                                                    "list"
                                                )
                                            ) {
                                                JSON.parseObject(
                                                    JSON.parseObject(content).getString("data")
                                                ).getJSONArray("list")
                                            } else if (content.contains("data")) {
                                                JSON.parseObject(content).getJSONArray("data")
                                            } else {
                                                JSON.parseObject(content).getJSONArray("list")
                                            }
                                        if (jsonArray.isNotEmpty()) {
                                            val tab = QTabView(context).setTitle(
                                                TabTitle.Builder().setContent(subscribeList.name)
                                                    .setTextColor(
                                                        accentColorVariant(), textColorSecondary()
                                                    ).build()
                                            )

                                            if (allSources.find { it.title == tab.title } == null) {
                                                allSources.add(tab)
                                                binding.tabs.addTab(tab)
                                            }
                                        }
                                    }
                                    jsonArray.forEach {
                                        val jsonObject = JSON.parseObject(it.toString())
                                        val movie = MovieCount(
                                            subscribeName = sourceEntity.name,
                                            name = jsonObject.getString("vod_name"),
                                            image = jsonObject.getString("vod_pic"),
                                            playUrl = jsonObject.getString("vod_play_url"),
                                            category = subscribeList.name,
                                            json = jsonObject.toString(),
                                            searchUrl = subscribeList.searchUrl,
                                            remarks = jsonObject.getString("vod_remarks")
                                                .ifEmpty { "N/A" },
                                            sourceType = SourceType.COLLECTION,
                                            sourceName = subscribeList.name,
                                            videoParser = sourceEntity.videoParse
                                        )
                                        if (allMoviesCount.contains(movie).not()) {
                                            allMoviesCount.add(movie)
                                        }
                                    }
                                }.catch {

                                }
                            }

                            else -> {
                                toast("${subscribeList.name}:无法匹配源类型")
                            }
                        }
                    }
                }
            }
            allMoviesCount.distinct()
            //PopTip.show("测试")
            //logError(sourcesDatabase.getSourcesDao().queryAllSources())
        }
    }

    private val allSources = mutableListOf<QTabView>()

}
package com.manchuan.tools.activity.movies.fragments

import android.annotation.SuppressLint
import android.os.Build
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.addModels
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.drake.net.Get
import com.drake.net.cache.CacheMode
import com.drake.net.utils.scope
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.addStatusBarHeightToPaddingTop
import com.dylanc.longan.logError
import com.dylanc.longan.toast
import com.google.android.material.tabs.TabLayout
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.SearchMovieActivity
import com.manchuan.tools.activity.movies.model.MovieCategorys
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ItemLeaderMovieBinding
import com.manchuan.tools.databinding.MoviesCategoryBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class MoviesCategoryFragment : EngineFragment<MoviesCategoryBinding>(R.layout.movies_category) {


    override fun initData() {
        refreshCategories()
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        view?.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                insets.displayCutout?.apply {
                    binding.toolbar.addStatusBarHeightToPaddingTop()
                }
            }
            insets
        }
        binding.toolbar.title = "分类"
        binding.recyclerView.grid(3).setup {
            addType<MovieCategorys.Data.Result>(R.layout.item_leader_movie)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemLeaderMovieBinding.bind(itemView)
                val model = getModel<MovieCategorys.Data.Result>()
                binding.image.load(model.picurl, isCrossFade = true, isForceOriginalSize = true)
                binding.name.text = model.name
                binding.text.text = model.score.ifEmpty { "暂无评分" }
            }
            onClick(R.id.cardview) {
                val model = getModel<MovieCategorys.Data.Result>()
                SearchMovieActivity.start(model.name)
            }
        }
        binding.state.onLoadMore {
            refreshAdapter(clearAll = false)
        }
        FastScrollerBuilder(binding.recyclerView).useMd2Style().build()
        binding.category.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                movieType = tab?.text?.toString()!!
                refreshAdapter()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.types.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                movieCategory = tab?.text?.toString()!!
                refreshAdapter()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.areas.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                movieArea = tab?.text?.toString()!!
                refreshAdapter()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.years.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                movieYear = tab?.text?.toString()!!
                refreshAdapter()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.sort.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                movieRank = tab?.text?.toString()!!
                refreshAdapter()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private val categories = listOf("电影", "电视剧", "动漫", "综艺")

    private fun loadCategories() {
        categories.forEach {
            binding.category.addTab(binding.category.newTab().setText(it))
        }
        binding.types.addTab(binding.types.newTab().setText("全部"))
        binding.areas.addTab(binding.areas.newTab().setText("全部"))
        Global.localMovieCategories[0].optionList.forEach {
            binding.types.addTab(binding.types.newTab().setText(it))
        }
        Global.localMovieCategories[1].optionList.forEach {
            binding.areas.addTab(binding.areas.newTab().setText(it))
        }
        binding.years.addTab(binding.years.newTab().setText("全部"))
        Global.localMovieCategories[2].optionList.forEach {
            binding.years.addTab(binding.years.newTab().setText(it))
        }
        binding.sort.addTab(binding.sort.newTab().setText("热门"))
        binding.sort.addTab(binding.sort.newTab().setText("评分"))
        binding.sort.addTab(binding.sort.newTab().setText("最新"))
        logError(Global.localMovieCategories)
    }

    private fun refreshCategories() {
        scopeNetLife {
            val content =
                Get<MovieCategorys>("https://waptv.sogou.com/napi/re?style=&zone=&year=&fee=&order=&entity=film&req=list&class=1&fr=filter&start=0&len=15") {
                    converter = SerializationConverter("0", "status", "info")
                    setCacheMode(CacheMode.REQUEST_THEN_READ)
                }.await()
            if (Global.localMovieCategories != content.data.list.filterList || Global.localMovieCategories.isEmpty()) {
                Global.localMovieCategories = content.data.list.filterList
            }
            loadCategories()
        }.catch {
            toast("分类加载失败")
            it.printStackTrace()
        }
    }

    private var pages = 0

    override fun onDestroy() {
        super.onDestroy()
        movieCategory = ""
        movieType = "电影"
        movieRank = ""
        movieArea = ""
        movieYear = ""
        pages = 0
    }

    private var movieYear: String? = ""
    private var movieArea: String? = ""
    private var movieCategory: String? = ""
    private var movieType: String? = "电影"
    private var movieRank: String? = ""

    private fun refreshAdapter(
        clearAll: Boolean = true,
    ) {
        val type = when (movieType) {
            "电影" -> "film"
            "电视剧" -> "teleplay"
            "动漫" -> "cartoon"
            "综艺" -> "tvshow"
            else -> ""
        }
        val category = when (movieCategory) {
            "全部" -> ""
            else -> movieCategory
        }
        val year = when (movieYear) {
            "全部" -> ""
            else -> movieYear
        }
        val area = when (movieArea) {
            "全部" -> ""
            else -> movieArea
        }
        val rank = when (movieRank) {
            "热门" -> ""
            "评分" -> "score"
            "最新" -> "time"
            else -> ""
        }
        binding.state.onRefresh {
            scope {
                if (clearAll) {
                    pages = 0
                    binding.recyclerView.models =
                        Get<MovieCategorys>("https://waptv.sogou.com/napi/re?style=$category&zone=$area&year=$year&fee=&order=$rank&entity=$type&req=list&class=1&fr=filter&start=$pages&len=15") {
                            converter = SerializationConverter("0", "status", "info")
                        }.await().data.results.ifEmpty { emptyList() }
                } else {
                    pages += 15
                    binding.recyclerView.addModels(Get<MovieCategorys>("https://waptv.sogou.com/napi/re?style=$category&zone=$area&year=$year&fee=&order=$rank&entity=$type&req=list&class=1&fr=filter&start=${pages}&len=15") {
                        converter = SerializationConverter("0", "status", "info")
                    }.await().data.results)
                }
            }
        }.autoRefresh()
        binding.state.onLoadMore {
            scope {
                pages += 15
                binding.recyclerView.addModels(Get<MovieCategorys>("https://waptv.sogou.com/napi/re?style=$category&zone=$area&year=$year&fee=&order=$rank&entity=$type&req=list&class=1&fr=filter&start=${pages}&len=15") {
                    converter = SerializationConverter("0", "status", "info")
                }.await().data.results)
            }
        }
    }

}
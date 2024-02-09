package com.manchuan.tools.activity.movies.fragments

import android.annotation.SuppressLint
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.addModels
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.manchuan.tools.extensions.load
import com.lxj.androidktx.core.toDateString
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.SearchMovieActivity
import com.manchuan.tools.activity.movies.fragments.model.MoviesListDan
import com.manchuan.tools.activity.movies.model.MovieLeader
import com.manchuan.tools.databinding.FragmentLeaderboardBinding
import com.manchuan.tools.databinding.ItemsMoviesBinding
import com.manchuan.tools.json.SerializationConverter

class MoviesListFragment :
    EngineFragment<FragmentLeaderboardBinding>(R.layout.fragment_leaderboard) {

    private var type = "全部"
    private var index = 1

    override fun initData() {
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.ctl.title = "影单"
        binding.recyclerView.grid(3).setup {
            addType<MoviesListDan.Data.Item>(R.layout.items_movies)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemsMoviesBinding.bind(itemView)
                val model = getModel<MoviesListDan.Data.Item>()
                binding.image.load(
                    model.coverUrl1.ifEmpty { model.coverUrl2.ifEmpty { model.coverUrl3 } },
                    isCrossFade = true,
                    isForceOriginalSize = true
                )
                binding.name.text = model.movieListName
                binding.year.text = "${model.movieCount} 部"
                binding.summary.text = model.createdOn.toDateString()
            }
            onClick(R.id.item) {
                val model = getModel<MovieLeader.Data.Result.Result>()
                SearchMovieActivity.start(model.ename)
            }
        }
        binding.tabLay.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                type = tab?.text?.toString()!!
                index = 1
                loadLeaders()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.state.onRefresh {
            scope {
                binding.recyclerView.models =
                    Get<MoviesListDan>("https://api-shoulei-ssl.xunlei.com/xlppc.movie.recommend.api/get_movie_list_by_tag") {
                        param("page_size", "24")
                        param("tag", type)
                        param("sort", "upvote")
                        param("page_index", index)
                        converter = SerializationConverter("0", "code", "result")
                    }.await().data.itemList
            }.catch {
                it.printStackTrace()
            }
        }.autoRefresh()
        binding.state.onLoadMore {
            ++index
            scope {
                binding.recyclerView.addModels(
                    Get<MoviesListDan>("https://api-shoulei-ssl.xunlei.com/xlppc.movie.recommend.api/get_movie_list_by_tag") {
                        param("page_size", "24")
                        param("tag", type)
                        param("sort", "upvote")
                        param("page_index", index)
                        converter = SerializationConverter("0", "code", "result")
                    }.await().data.itemList
                )
            }.catch {
                it.printStackTrace()
            }
        }
    }

    private fun loadLeaders() {
        binding.recyclerView.smoothScrollToPosition(0)
        binding.state.autoRefresh()
    }

}
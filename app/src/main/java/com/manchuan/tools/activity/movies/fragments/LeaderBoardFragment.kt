package com.manchuan.tools.activity.movies.fragments

import android.annotation.SuppressLint
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.SearchMovieActivity
import com.manchuan.tools.activity.movies.model.MovieLeader
import com.manchuan.tools.databinding.FragmentLeaderboardBinding
import com.manchuan.tools.databinding.ItemLeaderMovieBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter

class LeaderBoardFragment :
    EngineFragment<FragmentLeaderboardBinding>(R.layout.fragment_leaderboard) {

    private var type = "电影"

    override fun initData() {
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.toolbar.addStatusBarHeightToMarginTop()
        binding.state.setEnableLoadMore(false)
        binding.ctl.title = "排行榜"
        binding.recyclerView.linear().setup {
            addType<MovieLeader.Data.Result.Result>(R.layout.item_leader_movie)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemLeaderMovieBinding.bind(itemView)
                val model = getModel<MovieLeader.Data.Result.Result>()
            }
            onClick(R.id.item) {
                val model = getModel<MovieLeader.Data.Result.Result>()
                SearchMovieActivity.start(model.ename)
            }
        }
        binding.tabLay.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                type = tab?.text?.toString()!!
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
                    Get<MovieLeader>("https://sp1.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?resource_id=28214&ks_from=ks_sf&new_need_di=1&from_mid=1&sort_type=1&query=${type + "排行榜"}&tn=wisexmlnew&dsp=iphone&format=json&ie=utf-8&oe=utf-8&sort_key=0&stat0=$type&pd=movie_general&rn=24&pn=0") {
                        setHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.33"
                        )
                        converter = SerializationConverter("0", "status", "")
                    }.await().data.first().result.result
            }.catch {
                it.printStackTrace()
            }
        }.showLoading()
    }

    override fun onResume() {
        super.onResume()
        binding.state.refresh()
    }

    private fun loadLeaders() {
        binding.state.refresh()
    }

}
package com.manchuan.tools.activity.movies.fragments

import android.annotation.SuppressLint
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.drake.interval.Interval
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.addStatusBarHeightToPaddingTop
import com.dylanc.longan.doOnClick
import com.dylanc.longan.startActivity
import com.lxj.androidktx.core.carousel
import com.lxj.androidktx.core.gone
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.SearchMovieActivity
import com.manchuan.tools.activity.movies.model.ComingSoon
import com.manchuan.tools.activity.movies.model.HotQuery
import com.manchuan.tools.activity.movies.model.MovieCategorys
import com.manchuan.tools.databinding.FragmentMoviesMainBinding
import com.manchuan.tools.databinding.ItemsMoviesBinding
import com.manchuan.tools.databinding.ItemsMoviesComingBinding
import com.manchuan.tools.extensions.hint
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter
import java.util.concurrent.TimeUnit


class MoviesMainFragment :
    EngineFragment<FragmentMoviesMainBinding>(R.layout.fragment_movies_main) {


    @SuppressLint("NotifyDataSetChanged")
    override fun initData() {
        scopeNetLife {
            binding.coming.models = Get<ComingSoon>("http://www.young1024.com:1234/soon") {
                converter = SerializationConverter("0", "status", "msg")
            }.await().data.films
            if (binding.coming.mutable.isEmpty()) {
                binding.comingSoon.gone()
                binding.coming.gone()
            }
            binding.hotTv.models =
                Get<MovieCategorys>("https://waptv.sogou.com/napi/re?style=&zone=&year=&fee=&order=&entity=teleplay&req=list&class=&fr=filter&start=0&len=6") {
                    converter = SerializationConverter("0", "status", "info")
                }.await().data.results
            binding.hotFilm.models =
                Get<MovieCategorys>("https://waptv.sogou.com/napi/re?style=&zone=&year=&fee=&order=&entity=film&req=list&class=&fr=filter&start=0&len=6") {
                    converter = SerializationConverter("0", "status", "info")
                }.await().data.results
            binding.hotShow.models =
                Get<MovieCategorys>("https://waptv.sogou.com/napi/re?style=&zone=&year=&fee=&order=&entity=tvshow&req=list&class=&fr=filter&start=0&len=6") {
                    converter = SerializationConverter("0", "status", "info")
                }.await().data.results
            binding.state.showContent()
        }.catch {
            it.printStackTrace()
            binding.state.showError()
        }
        scopeNetLife {
            val content =
                Get<HotQuery>("https://search.video.iqiyi.com/m?if=hotQuery&p=15&is_qipu_platform=1") {
                    converter = SerializationConverter("false", "is_empty", "is_empty")
                }.await().data
            Interval(12, TimeUnit.SECONDS).subscribe {
                runCatching {
                    binding.searchBar.textView.hint(
                        content[(0..content.size.dec()).random()].query, 500
                    )
                }
            }.start()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.appbar.addStatusBarHeightToMarginTop()
        binding.searchBar.doOnClick {
            startActivity<SearchMovieActivity>()
        }
        binding.coming.setup {
            addType<ComingSoon.Data.Film>(R.layout.items_movies_coming)
            onBind {
                val binding = ItemsMoviesComingBinding.bind(itemView)
                val model = getModel<ComingSoon.Data.Film>()
                binding.image.load(
                    model.poster, isCrossFade = true, skipMemory = true
                )
                binding.name.text = model.name
                binding.summary.text = model.category
            }
            onClick(R.id.movie) {
                val model = getModel<ComingSoon.Data.Film>()
                SearchMovieActivity.start(model.name)
            }
        }
        binding.hotFilm.setup {
            addType<MovieCategorys.Data.Result>(R.layout.items_movies)
            onBind {
                val binding = ItemsMoviesBinding.bind(itemView)
                val model = getModel<MovieCategorys.Data.Result>()
                binding.image.load(model.picurl, isCrossFade = true, isForceOriginalSize = true)
                binding.year.text = model.year
                binding.name.text = model.name
                binding.summary.text = model.score.ifEmpty { model.style }
            }
            onClick(R.id.movie) {
                val model = getModel<MovieCategorys.Data.Result>()
                SearchMovieActivity.start(model.name)
            }
        }
        binding.hotTv.setup {
            addType<MovieCategorys.Data.Result>(R.layout.items_movies)
            onBind {
                val binding = ItemsMoviesBinding.bind(itemView)
                val model = getModel<MovieCategorys.Data.Result>()
                binding.image.load(model.picurl, isCrossFade = true, isForceOriginalSize = true)
                binding.year.text = model.year
                binding.name.text = model.name
                if (model.ipadPlayForList?.episode!! == model.ipadPlayForList?.finishEpisode) {
                    binding.summary.text = "${model.ipadPlayForList?.finishEpisode}集全"
                } else {
                    binding.summary.text = "更新至${model.ipadPlayForList?.episode}集"
                }
            }
            onClick(R.id.movie) {
                val model = getModel<MovieCategorys.Data.Result>()
                SearchMovieActivity.start(model.name)
            }
        }
        binding.hotShow.setup {
            addType<MovieCategorys.Data.Result>(R.layout.items_movies)
            onBind {
                val binding = ItemsMoviesBinding.bind(itemView)
                val model = getModel<MovieCategorys.Data.Result>()
                binding.image.load(model.picurl, isCrossFade = true, isForceOriginalSize = true)
                binding.year.text = model.zone
                binding.name.text = model.name
                binding.summary.gone()
            }
            onClick(R.id.movie) {
                val model = getModel<MovieCategorys.Data.Result>()
                SearchMovieActivity.start(model.name)
            }
        }
    }

}
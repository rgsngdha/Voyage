package com.manchuan.tools.activity.news.fragments

import androidx.fragment.app.Fragment
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.drake.net.Get
import com.drake.net.utils.scope
import com.manchuan.tools.R
import com.manchuan.tools.activity.site.WebActivity
import com.manchuan.tools.activity.news.models.TiebaModels
import com.manchuan.tools.databinding.FragmentTieBaBinding
import com.manchuan.tools.databinding.ItemLocalNewsBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TiebaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TiebaFragment : EngineFragment<FragmentTieBaBinding>(R.layout.fragment_tie_ba) {

    override fun initView() {
        binding.refresh.setEnableLoadMore(false)
        binding.recyclerView.linear().divider {
            startVisible = true
            endVisible = true
            includeVisible = true
            orientation = DividerOrientation.VERTICAL
            setDivider(8, true)
        }.setup {
            addType<TiebaModels.Data>(R.layout.item_local_news)
            onBind {
                val binding = ItemLocalNewsBinding.bind(itemView)
                val model = getModel<TiebaModels.Data>()
                binding.cover.load(
                    model.pic,
                    placeholder = R.drawable.placeholder,
                    isCrossFade = true,
                    isForceOriginalSize = true,
                    isCenterCrop = true
                )
                binding.title.text = model.title
                binding.digest.text = model.desc
                binding.author.text = "${model.hot}"
            }
            onClick(R.id.item) {
                val model = getModel<TiebaModels.Data>()
                com.dylanc.longan.startActivity<WebActivity>("url" to model.url)
            }
        }
    }

    override fun initData() {
        binding.refresh.onRefresh {
            scope {
                binding.recyclerView.models =
                    Get<TiebaModels>("https://api.vvhan.com/api/hotlist?type=baiduRY") {
                        converter = SerializationConverter("true", "success", "success")
                    }.await().data.sortedBy { it.index }
            }
        }.autoRefresh()
    }

}
package com.manchuan.tools.activity.news.fragments

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
import com.manchuan.tools.activity.news.models.BiliBiliModels
import com.manchuan.tools.databinding.FragmentBiliBiliBinding
import com.manchuan.tools.databinding.ItemLocalNewsBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter

class BiliBiliFragment : EngineFragment<FragmentBiliBiliBinding>(R.layout.fragment_bili_bili) {

    override fun initView() {
        binding.refresh.setEnableLoadMore(false)
        binding.recyclerView.linear().divider {
            startVisible = true
            endVisible = true
            includeVisible = true
            orientation = DividerOrientation.VERTICAL
            setDivider(8, true)
        }.setup {
            addType<BiliBiliModels.Data>(R.layout.item_local_news)
            onBind {
                val binding = ItemLocalNewsBinding.bind(itemView)
                val model = getModel<BiliBiliModels.Data>()
                binding.cover.load(
                    model.pic,
                    placeholder = R.drawable.placeholder,
                    isCrossFade = true,
                    isForceOriginalSize = true,
                    isCenterCrop = true
                )
                binding.title.text = model.title
                binding.digest.text = model.desc
                binding.author.text = "热度:${model.hot}"
            }
            onClick(R.id.item) {
                val model = getModel<BiliBiliModels.Data>()
                com.dylanc.longan.startActivity<WebActivity>("url" to model.url)
            }
        }
    }

    override fun initData() {
        binding.refresh.onRefresh {
            scope {
                binding.recyclerView.models =
                    Get<BiliBiliModels>("https://api.vvhan.com/api/hotlist?type=bili") {
                        converter = SerializationConverter("true", "success", "success")
                    }.await().data.sortedBy { it.index }
            }
        }.autoRefresh()
    }

}
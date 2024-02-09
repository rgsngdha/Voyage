package com.manchuan.tools.activity.news.fragments

import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.drake.net.Get
import com.drake.net.utils.scope
import com.lxj.androidktx.core.gone
import com.manchuan.tools.R
import com.manchuan.tools.activity.site.WebActivity
import com.manchuan.tools.activity.news.models.WeiboModels
import com.manchuan.tools.databinding.FragmentWeiboBinding
import com.manchuan.tools.databinding.ItemLocalNewsBinding
import com.manchuan.tools.json.SerializationConverter

class WeiboFragment : EngineFragment<FragmentWeiboBinding>(R.layout.fragment_weibo) {

    override fun initView() {
        binding.refresh.setEnableLoadMore(false)
        binding.recyclerView.linear().divider {
            startVisible = true
            endVisible = true
            includeVisible = true
            orientation = DividerOrientation.VERTICAL
            setDivider(8, true)
        }.setup {
            addType<WeiboModels.Data>(R.layout.item_local_news)
            onBind {
                val binding = ItemLocalNewsBinding.bind(itemView)
                val model = getModel<WeiboModels.Data>()
                binding.cover.gone()
                binding.title.text = model.title
                binding.digest.gone()
                binding.author.text = "热度:${model.hot}"
            }
            onClick(R.id.item) {
                val model = getModel<WeiboModels.Data>()
                com.dylanc.longan.startActivity<WebActivity>("url" to model.url)
            }
        }
    }

    override fun initData() {
        binding.refresh.onRefresh {
            scope {
                binding.recyclerView.models = Get<WeiboModels>("https://api.vvhan.com/api/wbhot") {
                    converter = SerializationConverter("true", "success", "success")
                }.await().data.sortedByDescending { it.hot }
            }
        }.autoRefresh()
    }

}
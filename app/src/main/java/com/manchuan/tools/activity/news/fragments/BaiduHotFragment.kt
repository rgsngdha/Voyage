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
import com.manchuan.tools.activity.news.models.BaiduModels
import com.manchuan.tools.databinding.FragmentBaiduHotBinding
import com.manchuan.tools.databinding.ItemLocalNewsBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter

class BaiduHotFragment : EngineFragment<FragmentBaiduHotBinding>(R.layout.fragment_baidu_hot) {

    override fun initView() {
        binding.refresh.setEnableLoadMore(false)
        binding.recyclerView.linear().divider {
            startVisible = true
            endVisible = true
            includeVisible = true
            orientation = DividerOrientation.VERTICAL
            setDivider(8, true)
        }.setup {
            addType<BaiduModels.Data>(R.layout.item_local_news)
            onBind {
                val binding = ItemLocalNewsBinding.bind(itemView)
                val model = getModel<BaiduModels.Data>()
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
                val model = getModel<BaiduModels.Data>()
                com.dylanc.longan.startActivity<WebActivity>("url" to model.url)
            }
        }
    }

    override fun initData() {
        binding.refresh.onRefresh {
            scope {
                binding.recyclerView.models =
                    Get<BaiduModels>("https://api.vvhan.com/api/hotlist?type=baiduRD") {
                        converter = SerializationConverter("true", "success", "success")
                    }.await().data.sortedBy { it.index }
            }
        }.autoRefresh()
    }

}
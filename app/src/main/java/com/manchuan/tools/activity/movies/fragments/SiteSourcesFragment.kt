package com.manchuan.tools.activity.movies.fragments

import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.engine.base.EngineFragment
import com.drake.net.utils.scope
import com.dylanc.longan.safeArguments
import com.dylanc.longan.toast
import com.lxj.androidktx.core.color
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.database.SourceType
import com.manchuan.tools.activity.movies.database.SourcesDatabase
import com.manchuan.tools.activity.movies.database.SubscribeList
import com.manchuan.tools.databinding.FragmentSiteSourcesBinding
import com.manchuan.tools.databinding.ItemSubscribeListBinding
import com.manchuan.tools.extensions.selector
import kotlinx.coroutines.runBlocking


class SiteSourcesFragment :
    EngineFragment<FragmentSiteSourcesBinding>(R.layout.fragment_site_sources) {

    private val sourcesDatabase by lazy {
        SourcesDatabase.getInstance(requireContext())
    }

    private lateinit var ids: Lazy<Int>

    override fun initView() {
        ids = safeArguments("id")
        binding.recyclerView.staggered(2).setup {
            addType<SubscribeList>(R.layout.item_subscribe_list)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemSubscribeListBinding.bind(itemView)
                val model = getModel<SubscribeList>()
                binding.name.text = model.name
                binding.type.text =
                    if (model.sourceType == SourceType.COLLECTION) "采集站" else if (model.sourceType == SourceType.SITE) "影视站" else "直播"
                binding.type.setTextColor(
                    when (model.sourceType) {
                        SourceType.COLLECTION -> color(rikka.material.R.color.material_blue_600)
                        SourceType.SITE -> color(
                            rikka.material.R.color.material_amber_600
                        )

                        else -> color(rikka.material.R.color.material_cyan_600)
                     }
                )
            }
            onClick(R.id.item) {

            }
            onLongClick(R.id.item) {
                val model = getModel<SubscribeList>()
                selector(listOf("删除规则"), model.name) { dialogInterface, s, i ->
                    when (s) {
                        "删除规则" -> {
                            runBlocking {
                                runCatching {
                                    val entity =
                                        sourcesDatabase.getSourcesDao().getSourceById(ids.value)
                                    entity.sources?.remove(model)
                                    sourcesDatabase.getSourcesDao().updateSource(entity)
                                    binding.state.refreshing()
                                }.onFailure {
                                    toast("规则删除失败,可能是父级订阅存在问题,或数据库已损坏")
                                }.onSuccess {
                                    toast("删除成功")
                                }
                            }
                        }
                    }
                }
            }
        }
        binding.state.setEnableLoadMore(false)
        binding.state.onRefresh {
            scope {
                binding.recyclerView.models =
                    sourcesDatabase.getSourcesDao().getSourceById(ids.value).sources?.ifEmpty { emptyList() }
            }.catch {
                it.printStackTrace()
            }
        }.autoRefresh()
    }

    override fun initData() {

    }

}
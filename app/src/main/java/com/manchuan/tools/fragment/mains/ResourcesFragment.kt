package com.manchuan.tools.fragment.mains

import android.os.Build
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.toast
import com.lxj.androidktx.core.flexbox
import com.manchuan.tools.R
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.FragmentResourcesBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.rootView
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.fragment.model.FunctionModel

class ResourcesFragment : EngineFragment<FragmentResourcesBinding>(R.layout.fragment_resources) {

    override fun initView() {
        activity?.rootView?.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                insets.displayCutout?.apply {
                    binding.root.apply {
                        // 设置padding，防止内容显示到非安全区域
                        setPadding(
                            safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom
                        )
                    }
                }
            } else {
                binding.root.addStatusBarHeightToMarginTop()
            }
            // 不消费，直接返回原始对象
            insets
        }
        with(binding) {
            resourcesRecycler.flexbox().setup {
                setAnimation(AnimationType.ALPHA)
                addType<FunctionModel>(R.layout.item_chip)
                onClick(R.id.item) {
                    runCatching {
                        val model = getModel<FunctionModel>()
                        model.unit.invoke()
                    }.onFailure {
                        Global.favoriteFunctions.value = arrayListOf()
                        toast("收藏数据库错误，已重置并修复数据库")
                    }
                }
                onLongClick(R.id.item) {
                    selector(listOf("删除"), "操作") { dialogInterface, s, i ->
                        when (s) {
                            "删除" -> {
                                runCatching {

                                    binding.resourcesRecycler.mutable.removeAt(
                                        absoluteAdapterPosition
                                    )
                                    binding.resourcesRecycler.bindingAdapter.notifyItemRemoved(
                                        absoluteAdapterPosition
                                    )
                                    Global.favoriteFunctions.value =
                                        (binding.resourcesRecycler.mutable) as MutableList<FunctionModel>
                                    toast("删除成功")
                                }.onFailure {
                                    toast("删除失败")
                                }
                            }
                        }
                    }
                }
            }
        }
        Global.favoriteFunctions.observe(this) {
            loge(it)
            binding.resourcesRecycler.models = it
        }
    }

    override fun initData() {

    }

}
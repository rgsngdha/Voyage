package com.manchuan.tools.fragment.mains

import android.os.Build
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.toast
import com.lxj.androidktx.core.flexbox
import com.manchuan.tools.R
import com.manchuan.tools.database.Global
import com.manchuan.tools.database.dailyTool
import com.manchuan.tools.database.imageTool
import com.manchuan.tools.database.mediaTool
import com.manchuan.tools.database.queryTool
import com.manchuan.tools.database.siteTool
import com.manchuan.tools.database.systemTool
import com.manchuan.tools.database.transformerTool
import com.manchuan.tools.databinding.FragmentGalleryBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.rootView
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.fragment.model.CategoryGroup
import com.manchuan.tools.fragment.model.FunctionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FunctionsFragment : EngineFragment<FragmentGalleryBinding>(R.layout.fragment_gallery) {
    override fun initData() {

    }

    override fun initView() {
        binding.apply {
            title.text = "功能"
            subtitle.text = "更多功能敬请期待"
        }
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
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
                binding.functionsRecycler.flexbox().setup {
                    setAnimation(AnimationType.ALPHA)
                    addType<CategoryGroup>(R.layout.item_functions)
                    addType<FunctionModel>(R.layout.item_chip)
                    R.id.cardview.onFastClick {
                        when (itemViewType) {
                            R.layout.item_functions -> {
                                expandOrCollapse()
                                loge("子数组数量", getModel<CategoryGroup>().sublist.size)
                            }
                        }
                    }
                    R.id.item.onFastClick {
                        val model = getModel<FunctionModel>()
                        model.unit.invoke()
                    }
                    R.id.item.onLongClick {
                        val model = getModel<FunctionModel>()
                        selector(listOf("收藏"), "操作") { dialogInterface, s, i ->
                            when (s) {
                                "收藏" -> {
                                    runCatching {
                                        with(Global.favoriteFunctions.value!!) {
                                            if (contains(model)) {
                                                toast("收藏数据中已包含该功能")
                                            } else {
                                                Global.favoriteFunctions.value =
                                                    Global.favoriteFunctions.value?.apply {
                                                        add(model)
                                                    }
                                                toast("收藏成功")
                                            }
                                        }
                                    }.onFailure {
                                        loge("失败", it)
                                        toast("收藏失败")
                                    }
                                }
                            }
                        }
                    }
                }.models = mutableListOf(CategoryGroup(
                    R.drawable.ic_baseline_wb_sunny_24, "日常工具", "Daily Tool"
                ).apply {
                    sublist.addAll(dailyTool)
                },
                    CategoryGroup(
                        R.drawable.baseline_screen_search_desktop_24, "查询工具", "Query Tool"
                    ).apply {
                        sublist.addAll(queryTool)
                    },
                    CategoryGroup(R.drawable.ic_outline_image_24, "图片工具", "Image Tool").apply {
                        sublist.addAll(imageTool)
                    },
                    CategoryGroup(
                        R.drawable.ic_round_video_library_24, "媒体工具", "Media Tool"
                    ).apply {
                        sublist.addAll(mediaTool)
                    },
                    CategoryGroup(
                        R.drawable.ic_baseline_android_24, "系统工具", "System Tool"
                    ).apply {
                        sublist.addAll(systemTool)
                    },
                    CategoryGroup(
                        R.drawable.ic_baseline_sync_alt_24, "转码工具", "Transcoding Tool"
                    ).apply {
                        sublist.addAll(transformerTool)
                    },
                    CategoryGroup(R.drawable.public_material, "站长工具", "Webmaster Tool").apply {
                        sublist.addAll(siteTool)
                    })
            }


        }
    }


}
package com.manchuan.tools.activity.life

import android.graphics.Typeface
import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.debounce
import com.drake.net.utils.launchIn
import com.drake.net.utils.scope
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.textString
import com.itxca.spannablex.spannable
import com.manchuan.tools.R
import com.manchuan.tools.activity.life.model.PostalModel
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPostalQueryBinding
import com.manchuan.tools.databinding.ItemPostalQueryBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.json.SerializationConverter
import kotlinx.coroutines.flow.distinctUntilChanged

class PostalQueryActivity : BaseActivity() {

    private val binding by viewBinding(ActivityPostalQueryBinding::inflate)
    private var url = ""
    private val postalList = mutableListOf<PostalModel.Data.Contentlist>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "邮编查询"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        binding.recyclerView.divider {
            setDrawable(R.drawable.divider_horizontal)
            startVisible = false
            endVisible = true
            orientation = DividerOrientation.HORIZONTAL
        }.linear().setup {
            addType<PostalModel.Data.Contentlist>(R.layout.item_postal_query)
            onBind {
                val binding = getBinding<ItemPostalQueryBinding>()
                val model = getModel<PostalModel.Data.Contentlist>()
                binding.area.text = model.county
                binding.code.text = spannable {
                    "邮政编码".text()
                    "   ${model.code}".span {
                        color(colorPrimary())
                        style(Typeface.BOLD)
                    }
                }
                binding.province.text = "${model.province} ${model.city} ${model.area}"
            }
        }
        binding.editText.debounce(100).distinctUntilChanged().launchIn(this) {
            binding.recyclerView.models = postalList.filter {
                it.county.contains(
                    binding.editText.textString, true
                )
            }
        }
        if (safeIntentExtras<String>("type").value == "area") {
            supportActionBar?.subtitle = "${
                safeIntentExtras<String>(
                    "province"
                ).value
            } ${
                safeIntentExtras<String>(
                    "city"
                ).value
            } ${
                safeIntentExtras<String>(
                    "area"
                ).value
            }"
        }
        url = when (safeIntentExtras<String>("type").value) {
            "area" -> "https://uapi.woobx.cn/app/postal-code-query?province=${
                safeIntentExtras<String>(
                    "province"
                ).value
            }&city=${
                safeIntentExtras<String>(
                    "city"
                ).value
            }&area=${
                safeIntentExtras<String>(
                    "area"
                ).value
            }&page=1"

            "code" -> "https://uapi.woobx.cn/app/postal-code-query-by-code?code=${
                safeIntentExtras<String>(
                    "code"
                ).value
            }&page=1"

            else -> ""
        }
        binding.page.onRefresh {
            scope {
                val postal = Get<PostalModel>(url) {
                    converter = SerializationConverter("200", "code", "")
                }.await()
                binding.recyclerView.models = emptyList()
                postalList.clear()
                addData(postal.data.contentlist) {
                    index < postal.data.allPages
                }
                postalList.addAll(postal.data.contentlist)
            }
        }.autoRefresh()
        binding.page.onLoadMore {
            scope {
                val postal = Get<PostalModel>(url.replace("page=1", "page=${index.inc()}")) {
                    converter = SerializationConverter("200", "code", "")
                }.await()
                postalList.addAll(postal.data.contentlist)
                addData(postal.data.contentlist) {
                    index < postal.data.allPages
                }
            }
        }

    }

}
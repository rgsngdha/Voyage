package com.manchuan.tools.activity.life

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.runMain
import com.drake.net.utils.scope
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.startActivity
import com.lxj.androidktx.core.flexbox
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityHistoryPersonBinding
import com.manchuan.tools.databinding.ItemHistoryPersonBinding
import kotlinx.coroutines.Dispatchers
import org.jsoup.Jsoup
import org.seimicrawler.xpath.JXDocument

class HistoryPersonActivity : BaseActivity() {

    private val binding by viewBinding(ActivityHistoryPersonBinding::inflate)

    private val rootUrl = "https://renwuzhi.wiki"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "人物志"
            subtitle = "权威历史人物传记"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        with(binding) {
            recycler.flexbox().setup {
                addType<HistoryPerson>(R.layout.item_history_person)
                setAnimation(AnimationType.ALPHA)
                onBind {
                    val binding = getBinding<ItemHistoryPersonBinding>()
                    val model = getModel<HistoryPerson>()
                    binding.name.text = model.name
                    binding.count.text = model.count.toString()
                }
                R.id.item.onFastClick {
                    val model = getModel<HistoryPerson>()
                    //toast(model.url)
                    startActivity<ListPersonActivity>("url" to model.url, "dynasty" to model.name)
                }
            }
            state.onRefresh {
                scope(Dispatchers.IO) {
                    historyPersons.clear()
                    runMain {
                        binding.recycler.models = emptyList()
                    }
                    val document = JXDocument.create(Jsoup.connect(rootUrl).get())
                    val container = document.selNOne("//div[2]/div")
                    JXDocument.create(container.asString()).selN("//a").forEach {
                        val countDocument = JXDocument.create(it.asString())
                        val url = countDocument.selNOne("//@href")
                        val name = countDocument.selNOne("//div[1]/text()")
                        val count = countDocument.selNOne("//div[2]/text()")
                        historyPersons.add(
                            HistoryPerson(
                                name.asString(),
                                "$rootUrl${url.asString()}",
                                count.asString().replace(",", "").toInt()
                            )
                        )
                    }
                    runMain {
                        binding.recycler.models = historyPersons
                    }
                }
            }.autoRefresh()
        }
    }

    private val historyPersons = mutableListOf<HistoryPerson>()

    data class HistoryPerson(var name: String, var url: String, var count: Int)

}

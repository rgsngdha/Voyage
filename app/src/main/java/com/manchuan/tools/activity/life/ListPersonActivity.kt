package com.manchuan.tools.activity.life

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.addModels
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.runMain
import com.drake.net.utils.scope
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.lxj.androidktx.core.flexbox
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityListPersonBinding
import com.manchuan.tools.databinding.ItemListPersonBinding
import com.manchuan.tools.extensions.loge
import kotlinx.coroutines.Dispatchers
import org.jsoup.Jsoup
import org.seimicrawler.xpath.JXDocument

class ListPersonActivity : BaseActivity() {

    private val binding by viewBinding(ActivityListPersonBinding::inflate)
    private val url by safeIntentExtras<String>("url")
    private val dynasty by safeIntentExtras<String>("dynasty")

    private val rootUrl = "https://renwuzhi.wiki"
    private val persons = mutableListOf<Persons>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "朝代人物详情"
            subtitle = dynasty
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        with(binding) {
            binding.recycler.flexbox().setup {
                setAnimation(AnimationType.ALPHA)
                addType<Persons>(R.layout.item_list_person)
                onBind {
                    val binding = getBinding<ItemListPersonBinding>()
                    val model = getModel<Persons>()
                    binding.name.text = model.name
                }
                R.id.item.onFastClick {
                    val model = getModel<Persons>()
                    startActivity<PersonDetailActivity>("url" to model.url)
                }
            }
            state.onRefresh {
                scope(Dispatchers.IO) {
                    persons.clear()
                    page = 1
                    runMain {
                        binding.recycler.models = emptyList()
                    }
                    val document = JXDocument.create(Jsoup.connect(url).get())
                    val container = document.selNOne("//div[2]/div")
                    JXDocument.create(container.asString()).selN("//a").forEach { jxNode ->
                        val itemDocument = JXDocument.create(jxNode.asString())
                        val name = itemDocument.selNOne("//div/div[1]/text()")
                        val url = itemDocument.selNOne("//@href")
                        runCatching {
                            if (persons.find { it.name == name.asString() } == null) {
                                persons.add(Persons(name.asString(), rootUrl + url.asString()))
                            }
                        }.onFailure {
                            loge("查询失败")
                        }
                    }
                    runMain {
                        binding.recycler.models = persons
                    }
                }
            }.autoRefresh()
            state.onLoadMore {
                scope(Dispatchers.IO) {
                    page++
                    val document =
                        JXDocument.create(Jsoup.connect(url.replace("page-1", "page-$page")).get())
                    val container = document.selNOne("//div[2]/div")
                    val loadMorePersons = mutableListOf<Persons>()
                    JXDocument.create(container.asString()).selN("//a").forEach { jxNode ->
                        val itemDocument = JXDocument.create(jxNode.asString())
                        val name = itemDocument.selNOne("//div/div[1]/text()")
                        val url = itemDocument.selNOne("//@href")
                        runCatching {
                            if (persons.count { it.name == name.asString() } < 1) {
                                loadMorePersons.add(
                                    Persons(
                                        name.asString(), rootUrl + url.asString()
                                    )
                                )
                            }
                        }.onFailure {
                            loge("查询失败")
                        }
                    }
                    runMain {
                        binding.recycler.addModels(loadMorePersons)
                    }
                }.catch {
                    it.printStackTrace()
                    runMain {
                        toast("加载错误")
                    }
                }
            }
        }
    }

    private var page = 1

    data class Persons(var name: String, var url: String)

}
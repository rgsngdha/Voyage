package com.manchuan.tools.activity.game

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.drake.brv.utils.addModels
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivitySearchGamesBinding
import com.manchuan.tools.databinding.ItemsGamesBinding
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.startActivity
import com.manchuan.tools.model.GameModel
import com.manchuan.tools.utils.SettingsLoader
import com.manchuan.tools.utils.UiUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.jsoup.Jsoup

class SearchGamesActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySearchGamesBinding.inflate(layoutInflater)
    }
    private val gameList: ArrayList<GameModel> = ArrayList()
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "搜索怀旧游戏"
        }
        immerseStatusBar(!UiUtils.isDarkMode())
        binding.toolbar.addStatusBarHeightToMarginTop()
        FastScrollerBuilder(binding.recyclerView).useMd2Style().build()
        binding.recyclerView.staggered(2).setup {
            addType<GameModel>(R.layout.items_games)
            onBind {
                val binding = ItemsGamesBinding.bind(itemView)
                binding.name.text = getModel<GameModel>().title
                Glide.with(context).load(getModel<GameModel>().imageUrl).skipMemoryCache(true)
                    .diskCacheStrategy(SettingsLoader.diskCacheMethod).into(binding.image)
            }
            onClick(R.id.card) {
                context.startActivity<FullscreenGameActivity>(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP, bundle = arrayOf(
                        "url" to getModel<GameModel>(modelPosition).playUrl
                    )
                )
            }
        }
        receiveEvent<ArrayList<GameModel>>("models_fc_search") {
            runBlocking {
                launch {
                    binding.recyclerView.models = it
                }
            }
        }
        receiveEvent<ArrayList<GameModel>>("load_more_models_fc_search") {
            runBlocking {
                launch {
                    binding.recyclerView.addModels(it)
                }
            }
        }

        binding.textField.setEndIconOnClickListener {
            if (binding.editText.isTextNotEmpty()) {
                runCatching {
                    binding.page.showLoading()
                    page = 1
                    Thread {
                        runCatching {
                            gameList.clear()
                            val document =
                                Jsoup.connect("https://www.yikm.net/search?name=${binding.editText.textString}&page=$page")
                                    .get()
                            val row = document.getElementsByClass("row")[1]
                            val stringBuilder = StringBuilder()
                            for (item in row.getElementsByClass("col-md-3 col-xs-6")) {
                                val items = item.getElementsByClass("card card-blog").first()
                                val table = items?.getElementsByClass("table")?.first()
                                val image =
                                    items?.getElementsByClass("img img-raised")?.attr("abs:src")
                                val playUrlElement = items?.getElementsByTag("h4")?.first()
                                val playUrl = playUrlElement?.getElementsByTag("a")?.select("a")
                                    ?.attr("abs:href")
                                val title =
                                    playUrlElement?.getElementsByTag("a")?.select("a")?.text()
                                val category = ArrayList<String>()
                                for (categoryItem in table?.getElementsByTag("span")!!) {
                                    category.add(categoryItem.text())
                                }
                                gameList.add(GameModel(playUrl!!, title!!, image!!, category))
                                stringBuilder.append("\n${playUrlElement.attr("abs:href")}")
                                loge(stringBuilder.toString())
                            }
                            sendEvent(gameList, "models_fc_search")
                            binding.page.showContent()
                            page++
                        }.onFailure {
                            it.printStackTrace()
                            binding.page.showError()
                            if (!NetworkUtils.isAvailable()) {
                                toast("网络不可用")
                            }
                        }
                    }.start()
                }
                binding.page.onLoadMore {
                    Thread {
                        runCatching {
                            val gameList: ArrayList<GameModel> = ArrayList()
                            val document =
                                Jsoup.connect("https://www.yikm.net/search?name=${binding.editText.textString}&page=$page")
                                    .get()
                            val row = document.getElementsByClass("row")[1]
                            val stringBuilder = StringBuilder()
                            for (item in row.getElementsByClass("col-md-3 col-xs-6")) {
                                val items = item.getElementsByClass("card card-blog").first()
                                val table = items?.getElementsByClass("table")?.first()
                                val image =
                                    items?.getElementsByClass("img img-raised")?.attr("abs:src")
                                val playUrlElement = items?.getElementsByTag("h4")?.first()
                                val playUrl = playUrlElement?.getElementsByTag("a")?.select("a")
                                    ?.attr("abs:href")
                                val title =
                                    playUrlElement?.getElementsByTag("a")?.select("a")?.text()
                                val category = ArrayList<String>()
                                for (categoryItem in table?.getElementsByTag("span")!!) {
                                    category.add(categoryItem.text())
                                }
                                gameList.add(GameModel(playUrl!!, title!!, image!!, category))
                                stringBuilder.append("\n${playUrlElement.attr("abs:href")}")
                                loge(stringBuilder.toString())
                            }
                            sendEvent(gameList, "load_more_models_fc_search")
                            page++
                            this.finish(true)
                            binding.page.finishLoadMore(true)
                        }.onFailure {
                            this.finish(false)
                            binding.page.finishLoadMore(true)
                            if (!NetworkUtils.isAvailable()) {
                                toast("网络不可用")
                            }
                        }
                    }.start()
                }
            } else {
                snack("请先输入关键词后再搜索...")
            }
        }
    }


}
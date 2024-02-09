package com.manchuan.tools.activity.game

import android.content.DialogInterface
import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.drake.net.utils.scopeDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.manchuan.tools.R
import com.manchuan.tools.activity.game.models.HeroPower
import com.manchuan.tools.activity.game.models.HonorKings
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityHonorKingsPowerBinding
import com.manchuan.tools.databinding.ItemSgameBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.json.SerializationConverter

class HonorKingsPowerActivity : BaseActivity() {

    private val binding by viewBinding(ActivityHonorKingsPowerBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "最低战力查询"
            subtitle = "查询指定英雄最低战力的地区"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.grid(4).setup {
            setAnimation(AnimationType.ALPHA)
            addType<HonorKings.HonorKingsItem>(R.layout.item_sgame)
            onBind {
                val binding = getBinding<ItemSgameBinding>()
                val model = getModel<HonorKings.HonorKingsItem>()
                binding.image.load(
                    "https://game.gtimg.cn/images/yxzj/img201606/heroimg/${model.ename}/${model.ename}.jpg",
                    isCrossFade = true,
                    skipMemory = true
                )
                binding.text.text = model.cname
            }
            R.id.card.onFastClick {
                val model = getModel<HonorKings.HonorKingsItem>()
                selector(listOf("安卓Q区", "安卓微区"), "选择平台") { dialogInterface, s, i ->
                    when (s) {
                        "安卓Q区" -> getHeroPower(model.cname, "qq")
                        "安卓微区" -> getHeroPower(model.cname, "wx")
                    }
                }
            }
        }
        binding.page.onRefresh {
            scope {
                val heroList = mutableListOf<HonorKings.HonorKingsItem>()
                val data = Get<String>("https://pvp.qq.com/web201605/js/herolist.json").await()
                //Json的解析类对象
                //Json的解析类对象
                val parser = JsonParser()
                //将JSON的String 转成一个JsonArray对象
                val jsonArray: JsonArray = parser.parse(data).asJsonArray
                val gson = Gson()
                //加强for循环遍历JsonArray
                //加强for循环遍历JsonArray
                for (wallpaper in jsonArray) {
                    //使用GSON，直接转成Bean对象
                    val hero = gson.fromJson(wallpaper, HonorKings.HonorKingsItem::class.java)
                    heroList.add(hero)
                }
                binding.recyclerView.models = heroList
            }
        }.autoRefresh()
    }

    private fun getHeroPower(name: String, channel: String) {
        scopeDialog {
            val power =
                Get<HeroPower>("https://www.sapi.run/hero/select.php?hero=$name&type=$channel") {
                    converter = SerializationConverter("200", "code", "msg")
                }.await().data
            alertDialog {
                title = power.alias
                items(
                    listOf(
                        "平台：${power.platform}",
                        "更新时间：${power.updatetime}",
                        "区标：${power.area}（${power.areaPower}）",
                        "市标：${power.city}（${power.cityPower}）",
                        "省标：${power.province}（${power.provincePower}）"
                    )
                ) { dialogInterface: DialogInterface, i: Int ->

                }
                okButton { }
            }.build()
        }
    }

}
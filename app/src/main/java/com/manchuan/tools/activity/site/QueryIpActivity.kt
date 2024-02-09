package com.manchuan.tools.activity.site

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.textString
import com.manchuan.tools.R
import com.manchuan.tools.activity.site.model.QueryIP
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityQueryIpBinding
import com.manchuan.tools.databinding.ItemQueryIpBinding
import com.manchuan.tools.json.SerializationConverter

class QueryIpActivity : BaseActivity() {

    private val binding by viewBinding(ActivityQueryIpBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "全球IP查询"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        binding.recyclerView.linear().setup {
            addType<IPModel>(R.layout.item_query_ip)
            onBind {
                val binding = getBinding<ItemQueryIpBinding>()
                val model = getModel<IPModel>()
                binding.name.text = model.name
                binding.content.text = model.content.ifEmpty { "无数据" }
            }
        }
        binding.textField.setEndIconOnClickListener {
            queryIp()
        }
        binding.useMyIp.setOnClickListener {
            queryIp("")
        }
        binding.editText.setOnEditorActionListener { v, actionId, event ->
            queryIp()
            true
        }
    }

    private fun queryIp(ip: String = binding.editText.textString) {
        scopeNetLife {
            val query =
                Get<QueryIP>("https://uapi.woobx.cn/app/ip-location?ip=$ip") {
                    converter = SerializationConverter("200", "code", "showapi_res_error")
                }.await().data.showapiResBody
            binding.recyclerView.models = listOf(
                IPModel("市", query.city),
                IPModel("省", query.region),
                IPModel("洲", query.continents),
                IPModel("经度", query.lnt),
                IPModel("纬度", query.lat),
                IPModel("行政代码", query.cityCode),
                IPModel("运营商", query.isp),
                IPModel("英文名", query.enName)
            )
        }
    }

    data class IPModel(var name: String, var content: String)

}
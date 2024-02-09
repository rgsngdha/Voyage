package com.manchuan.tools.activity.site

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.encryptMD5
import com.dylanc.longan.lifecycleOwner
import com.dylanc.longan.startActivity
import com.lxj.androidktx.core.click
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.user.MovieLoginActivity
import com.manchuan.tools.activity.site.model.Sites
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivitySitesShareBinding
import com.manchuan.tools.databinding.ItemSitesBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.glideDrawable
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.user.appId
import com.manchuan.tools.user.appKey
import com.manchuan.tools.user.host
import com.manchuan.tools.user.timeMills
import com.manchuan.tools.user.verifyRole
import rikka.material.app.MaterialActivity

class SitesShareActivity : MaterialActivity() {

    private val binding by viewBinding(ActivitySitesShareBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "实用网站分享"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.recyclerView.linear().divider {
            orientation = DividerOrientation.VERTICAL
            setDivider(8, true)
            includeVisible = true
        }.setup {
            addType<Sites.Msg>(R.layout.item_sites)
            onBind {
                val modelBinding = getBinding<ItemSitesBinding>()
                val model = getModel<Sites.Msg>()
                modelBinding.time.text = model.name
                modelBinding.title.text = model.description
                modelBinding.card.click {
                    startActivity<WebActivity>("url" to model.url)
                }
            }
        }
        Global.token.observe(lifecycleOwner) { s ->
            if (s.isEmpty()) {
                alertDialog {
                    title = "登录"
                    message = "该功能仅登录后使用。"
                    isCancelable = false
                    okButton("登录") {
                        startActivity<MovieLoginActivity>()
                    }
                    cancelButton("退出") {
                        finish()
                    }
                }.show()
            } else {
                binding.state.onRefresh {
                    scope {
                        val sites = Get<Sites>(host) {
                            setHeader("User-Agent", userAgent())
                            param("act", "get_sites")
                            param("app", appId)
                            param("token", s)
                            param("t", timeMills)
                            param("sign", ("token=$s&t=$timeMills&$appKey").encryptMD5())
                            converter = SerializationConverter("200", "code", "msg")
                        }.await()
                        binding.recyclerView.models = sites.msg
                    }
                }.autoRefresh()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        verifyRole(Global.token.value.toString(), success = {
            menuInflater.inflate(R.menu.share_sites, menu)
        }, failed = {

        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.add_site -> startActivity<AddSitesActivity>()
        }
        return super.onOptionsItemSelected(item)
    }
}
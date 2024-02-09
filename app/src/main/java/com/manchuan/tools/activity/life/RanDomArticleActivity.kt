package com.manchuan.tools.activity.life

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.crazylegend.viewbinding.viewBinding
import com.drake.net.utils.runMain
import com.drake.net.utils.withMain
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityArticleBinding
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.text
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.seimicrawler.xpath.JXDocument

/**
 * @author padre
 */
class RanDomArticleActivity : BaseActivity() {

    private val binding by viewBinding(ActivityArticleBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "随机一文"
            subtitle = "精选文章"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        enableTransitionTypes(binding.contentLay)
        loadArticle()
    }

    private fun loadArticle() {
        WaitDialog.show("请稍后...")
        ioScope.launch {
            runCatching {
                val document =
                    JXDocument.create(Jsoup.connect("https://www.dushu.com/meiwen/random/").get())
                val title = document.selNOne("//div[6]/div/div/h1/text()")
                val author = document.selNOne("//div[6]/div/div/div[1]/div/span/text()")
                val content = StringBuilder()
                JXDocument.create(document.selNOne("//div[6]/div/div/div[2]").asString())
                    .selN("//p/text()").forEach {
                        content.appendLine("    " + it.asString())
                    }
                withMain {
                    binding.ctl.apply {
                        this.title = title.asString()
                        subtitle = author.asString()
                    }
                    binding.content.text(content.toString())
                    WaitDialog.dismiss()
                }
            }.onFailure {
                snack("错误")
                loge(it)
                withMain {
                    WaitDialog.dismiss()
                }
            }
            //binding.content.text(content.toString() )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_article, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.title = "随机一文"
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.refresh -> loadArticle()
        }
        return super.onOptionsItemSelected(item)
    }
}
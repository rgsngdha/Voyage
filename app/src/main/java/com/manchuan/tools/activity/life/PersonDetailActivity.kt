package com.manchuan.tools.activity.life

import android.graphics.Typeface
import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.net.utils.runMain
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.toast
import com.itxca.spannablex.spannable
import com.lxj.androidktx.core.string
import com.lxj.xpopup.XPopup
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPersonDetailBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.loge
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.seimicrawler.xpath.JXDocument

class PersonDetailActivity : BaseActivity() {

    private val binding by viewBinding(ActivityPersonDetailBinding::inflate)

    private val url by safeIntentExtras<String>("url")

    private val loadingDialog by lazy {
        XPopup.Builder(this).dismissOnBackPressed(false).dismissOnTouchOutside(false)
            .asLoading(string(com.drake.net.R.string.srl_footer_loading))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "人物详情"
            setDisplayHomeAsUpEnabled(true)
        }
        ioScope.launch {
            runMain {
                loadingDialog.show()
            }
            runCatching {
                val document = JXDocument.create(Jsoup.connect(url).get())
                val name = document.selNOne("//div[2]/div/div[2]/h1/text()")
                val gender = document.selNOne("//div[2]/div/div[2]/div[5]/text()")
                val born = document.selNOne("//div[2]/div/div[2]/div[6]/text()")
                val birthday = document.selNOne("//div[2]/div/div[2]/div[8]/text()")
                val famousBooks =
                    document.selNOne("//div[2]/div/div[4]/div/div[1][@class=\"custom-table\"]")
                val books = arrayListOf<String>()
                books.clear()
                runCatching {
                    JXDocument.create(famousBooks.asString())
                        .selN("//div[@class=\"custom-table-tr-td\"]").forEach {
                            val item = JXDocument.create(it.asString())
                            loge(it.asString())
                            val bookName = "《${item.selNOne("//div/div[1]/text()").asString()}》"
                            val time = item.selNOne("//div/div[2]/text()").asString()
                            books.add("$bookName - $time")
                        }
                    loge(tag = "著作", books)
                }
                runMain {
                    loadingDialog.dismiss()
                    binding.ctl.apply {
                        title = name.asString()
                        subtitle = gender.asString()
                    }
                    with(binding) {
                        info.text = spannable {
                            runCatching {
                                "基本信息".span {
                                    color(colorPrimary())
                                    style(Typeface.BOLD)
                                    absoluteSize(18)
                                }
                                newline()
                                born.asString().text()
                                newline()
                                birthday.asString().text()
                                newline(2)
                                "著作".span {
                                    color(colorPrimary())
                                    style(Typeface.BOLD)
                                    absoluteSize(18)
                                }
                                newline()
                                books.toArray().contentToString().text()
                            }
                        }
                    }
                }
            }.onFailure {
                runMain {
                    loadingDialog.dismiss()
                    toast("获取超时，请检查网络是否畅通。")
                }
            }
        }
    }
}
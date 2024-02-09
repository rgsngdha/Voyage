package com.manchuan.tools.activity.life

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.itxca.spannablex.activateClick
import com.itxca.spannablex.spannable
import com.itxca.spannablex.toReplaceRule
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.gone
import com.manchuan.tools.R
import com.manchuan.tools.activity.life.model.MovieLines
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityMovieLinesSearchBinding
import com.manchuan.tools.databinding.ItemMovieLineBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.concurrent.TimeUnit

class MovieLinesSearchActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMovieLinesSearchBinding::inflate)

    private var pages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        with(binding) {
            enableTransitionTypes(linear2)
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                title = "影视台词搜寻"
                setDisplayHomeAsUpEnabled(true)
            }
            recycler.staggered(2).divider {
                orientation = DividerOrientation.GRID
                setDivider(16, true)
            }.setup {
                addType<MovieLines.Data>(R.layout.item_movie_line)
                onBind {
                    val binding = getBinding<ItemMovieLineBinding>()
                    val model = getModel<MovieLines.Data>()
                    with(binding) {
                        with(model) {
                            holidayImage.load(localImg, isCrossFade = true)
                            movie.text = title
                            summary.activateClick(false).text = spannable {
                                "查看详情".span {
                                    color(colorPrimary())
                                    clickable(onClick = { view: View, s: String ->
                                        alertDialog {
                                            title = "影片详情"
                                            message =
                                                "电影名:${model.title}\n" + "区域:$area\n" + "标签:$tags\n" + "导演:$directors\n" + "演员:$actors\n" + "搜索台词:${zhWord.ifBlank { enWord }}\n" + "台词:${
                                                    allZhWord.ifEmpty { allEnWord }.toTypedArray()
                                                        .contentToString().replace(",", "\n")
                                                        .spannable {
                                                            color(colorPrimary(),
                                                                arrayOf(zhWord.ifBlank { enWord }
                                                                    .toReplaceRule()))
                                                        }
                                                }"
                                        }.build()
                                    })
                                }
                            }
                        }
                    }
                }
            }
            FastScrollerBuilder(recycler).useMd2Style().build()
            urlLay.setEndIconOnClickListener {
                if (url.isTextNotEmpty()) {
                    warning.gone()
                    WaitDialog.show("查找中")
                    page.onRefresh {
                        scope {
                            pages = 1
                            val lines =
                                Get<MovieLines>("https://api.pearktrue.cn/api/media/lines.php") {
                                    setClient {
                                        connectTimeout(60, TimeUnit.SECONDS)
                                    }
                                    param("word", url.textString)
                                    param("page", pages)
                                    converter = SerializationConverter("200", "code", "msg")
                                }.await()
                            WaitDialog.dismiss()
                            addData(lines.data) {
                                index < lines.lastPage.toInt()
                            }
                        }.catch {
                            WaitDialog.dismiss()
                            toast(it.message)
                            it.printStackTrace()
                        }
                    }.autoRefresh()
                    page.onLoadMore {
                        scope {
                            pages++
                            val lines =
                                Get<MovieLines>("https://api.pearktrue.cn/api/media/lines.php") {
                                    param("word", url.textString)
                                    param("page", pages)
                                    converter = SerializationConverter("200", "code", "msg")
                                }.await()
                            addData(lines.data) {
                                index < lines.lastPage.toInt()
                            }
                        }
                    }
                } else {
                    toast("输入的台词不能为空")
                }
            }

        }
    }

}

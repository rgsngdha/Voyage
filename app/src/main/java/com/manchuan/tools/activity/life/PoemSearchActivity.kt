package com.manchuan.tools.activity.life

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.shareText
import com.dylanc.longan.textString
import com.manchuan.tools.R
import com.manchuan.tools.activity.json.PoemModel
import com.manchuan.tools.databinding.ActivityPoemSearchBinding
import com.manchuan.tools.databinding.DialogPoemBinding
import com.manchuan.tools.databinding.ItemPoemBinding
import com.manchuan.tools.extensions.sheetDialog
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.textCopyThenPost
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.utils.UiUtils
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class PoemSearchActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPoemSearchBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "古诗词搜索"
        }
        immerseStatusBar(!UiUtils.isDarkMode())
        binding.toolbar.addStatusBarHeightToMarginTop()
        FastScrollerBuilder(binding.recyclerView).useMd2Style()
        binding.recyclerView.staggered(2).setup {
            addType<PoemModel.PoemList>(R.layout.item_poem)
            onBind {
                val binding = ItemPoemBinding.bind(itemView)
                val model = getModel<PoemModel.PoemList>()
                binding.title.text = model.title
                binding.author.text = model.author
            }
            R.id.card.onClick {
                val model = getModel<PoemModel.PoemList>()
                sheetDialog(DialogPoemBinding.inflate(layoutInflater).root, "诗词详情") {
                    val poemBinding = DialogPoemBinding.bind(it)
                    poemBinding.title.text = model.title
                    poemBinding.author.text = model.author
                    poemBinding.chaodai.text = model.chaodai
                    poemBinding.content.text = model.cont
                    poemBinding.share.setOnClickListener {
                        shareText("标题:${model.title}\n" + "作者:${model.author}\n" + "朝代:${model.chaodai}\n" + "内容:${model.cont}")
                    }
                    poemBinding.copy.setOnClickListener {
                        textCopyThenPost(
                            "标题:${model.title}\n" + "作者:${model.author}\n" + "朝代:${model.chaodai}\n" + "内容:${model.cont}"
                        )
                    }
                }
            }
        }
        binding.textField.setEndIconOnClickListener {
            if (binding.editText.isTextNotEmpty()) {
                scopeNetLife {
                    binding.progress.show()
                    binding.recyclerView.models =
                        Get<PoemModel>("http://wenxin110.top/api/gushici?msg=${binding.editText.textString}") {
                            converter = SerializationConverter("1", "code", "msg")
                        }.await().list
                    binding.progress.hide()
                }
            } else {
                snack("请先输入关键词后再搜索...")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
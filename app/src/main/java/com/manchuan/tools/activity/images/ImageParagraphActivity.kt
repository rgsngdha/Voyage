package com.manchuan.tools.activity.images

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.doOnClick
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.isWebUrl
import com.dylanc.longan.textString
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.post
import com.manchuan.tools.R
import com.manchuan.tools.activity.images.models.PearKTrueImages
import com.manchuan.tools.activity.images.models.SuperImageParagraph
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityImageParagraphBinding
import com.manchuan.tools.databinding.ItemParagraphBinding
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.firstClipboardText
import com.manchuan.tools.extensions.inputDialog
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.savePic
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.substringBetween
import com.manchuan.tools.extensions.tryWith
import com.manchuan.tools.json.SerializationConverter
import com.mcxiaoke.koi.ext.toast

class ImageParagraphActivity : BaseActivity() {

    private val binding by lazy {
        ActivityImageParagraphBinding.inflate(layoutInflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "聚合图集解析"
        }
        binding.recyclerView.staggered(2).setup {
            addType<SuperImageParagraph.Content.Image>(R.layout.item_paragraph)
            onBind {
                val binding = getBinding<ItemParagraphBinding>()
                val model = getModel<SuperImageParagraph.Content.Image>()
                binding.image.load(
                    model.url, isCrossFade = true, isForceOriginalSize = true
                )
            }
            R.id.cardview.onFastClick {
                val model = getModel<SuperImageParagraph.Content.Image>()
                selector(listOf("下载"), "操作") { dialogInterface, s, i ->
                    when (s) {
                        "下载" -> savePic(model.url)
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
        enableTransitionTypes(binding.layout)
        binding.create.doOnClick {
            if (binding.time.isTextNotEmpty()) {
                normalDecode()
            }
        }
    }

    private fun normalDecode(url: String = binding.time.textString) {
        scopeNetLife {
            WaitDialog.show("解析中...")
            val links = Get<PearKTrueImages>("https://api.pearktrue.cn/api/tuji/api.php?url=$url") {
                converter = SerializationConverter("200", "code", "msg")
            }.await().images
            links.forEach {
                savePic(it)
            }
            toast("下载完成")
            WaitDialog.dismiss()
        }.catch {
            TipDialog.show("解析失败")
        }
    }


    override fun onResume() {
        super.onResume()
        post {
            runCatching {
                val isContainsVideo =
                    firstClipboardText().contains("v.douyin.com") or firstClipboardText().contains("v.kuaishou.com") or firstClipboardText().contains(
                        "xhslink.com"
                    )
                val videoPlatform =
                    if (firstClipboardText().contains("v.douyin.com")) "抖音" else if (firstClipboardText().contains(
                            "v.kuaishou.com"
                        )
                    ) "快手" else "小红书"
                val url = if (firstClipboardText().contains("v.douyin.com")) {
                    "https://v.douyin.com/${firstClipboardText().substringAfter("https://v.douyin.com/")}"
                } else if (firstClipboardText().contains("xhslink.com")) {
                    "https://xhslink.com/${
                        firstClipboardText().substringBetween(
                            "xhslink.com/", "，复制"
                        )
                    }"
                } else if (firstClipboardText().contains("v.kuaishou.com")) {
                    "https://v.kuaishou.com/${
                        firstClipboardText().subSequence(
                            24, 29
                        )
                    }"
                } else {
                    ""
                }.toString()
                if (isContainsVideo) {
                    inputDialog(
                        "聚合解析",
                        "检测到来自「$videoPlatform」平台的链接",
                        "解析",
                        tryWith { url }) { inputStr ->
                        if (inputStr.isWebUrl()) {
                            normalDecode(inputStr)
                        } else {
                            toast("错误的链接")
                        }
                    }
                }
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
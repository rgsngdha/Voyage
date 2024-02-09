package com.manchuan.tools.activity.images

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.addModels
import com.drake.brv.utils.divider
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.logError
import com.dylanc.longan.safeIntentExtras
import com.github.panpf.sketch.util.PauseLoadWhenScrollingMixedScrollListener
import com.manchuan.tools.R
import com.manchuan.tools.activity.images.models.AvatarsPreviewModel
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityAvatarsPreviewBinding
import com.manchuan.tools.databinding.ItemAvatarsPreviewBinding
import com.manchuan.tools.extensions.ImageEngine
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.savePic
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.json.SerializationConverter
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class AvatarsPreviewActivity : BaseActivity() {

    private val binding by lazy {
        ActivityAvatarsPreviewBinding.inflate(layoutInflater)
    }
    private var limit: Int = 26
    private var skip: Int = 0
    private lateinit var ids: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = safeIntentExtras<String>("title").value
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
        ids = safeIntentExtras<String>("ids").value
        FastScrollerBuilder(binding.recyclerView).useMd2Style().build()
        binding.recyclerView.addOnScrollListener(PauseLoadWhenScrollingMixedScrollListener())
        binding.recyclerView.staggered(2).divider { // 水平间距
            orientation = DividerOrientation.GRID
            setDivider(12, true)
        }.setup {
            addType<AvatarsPreviewModel.Res.Avatar>(R.layout.item_avatars_preview)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemAvatarsPreviewBinding.bind(itemView)
                val model = getModel<AvatarsPreviewModel.Res.Avatar>()
                binding.image.load(model.thumb, isCrossFade = true, imageEngine = ImageEngine.SKETCH)
            }
            onClick(R.id.cardview1) {
                val model = getModel<AvatarsPreviewModel.Res.Avatar>()
                //ImagePreview.instance.setContext(activity).setImage(model.thumb).start()
                selector(listOf("下载"), "操作") { dialogInterface, s, i ->
                    when (s) {
                        "下载" -> {
                            savePic(model.thumb)
                        }
                    }
                }
            }
        }
        logError(ids)
        binding.page.onRefresh {
            scope {
                skip = 0
                limit = 26
                binding.recyclerView.models =
                    Get<AvatarsPreviewModel>("https://service.avatar.adesk.com/v1/avatar/avatar?limit=$limit&skip=$skip&order=new&cid=$ids") {
                        converter = SerializationConverter("0", "code", "msg")
                    }.await().res.avatar
            }
        }.autoRefresh()
        binding.page.onLoadMore {
            scope {
                skip += limit
                binding.recyclerView.addModels(Get<AvatarsPreviewModel>("https://service.avatar.adesk.com/v1/avatar/avatar?limit=$limit&skip=$skip&order=new&cid=$ids") {
                    converter = SerializationConverter("0", "code", "msg")
                }.await().res.avatar)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
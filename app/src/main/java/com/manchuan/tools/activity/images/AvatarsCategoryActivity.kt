package com.manchuan.tools.activity.images

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.Get
import com.drake.net.utils.scope
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.startActivity
import com.github.panpf.sketch.util.PauseLoadWhenScrollingMixedScrollListener
import com.manchuan.tools.R
import com.manchuan.tools.activity.images.models.AvatarsModel
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityAvatarsCategoryBinding
import com.manchuan.tools.databinding.ItemAvatarsCategoryBinding
import com.manchuan.tools.extensions.ImageEngine
import com.manchuan.tools.extensions.load
import com.manchuan.tools.json.SerializationConverter
import timber.log.Timber

class AvatarsCategoryActivity : BaseActivity() {

    private val binding by lazy {
        ActivityAvatarsCategoryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "头像大全"
            subtitle = "多分类高清头像"
            setDisplayHomeAsUpEnabled(true)
        }
        immerseStatusBar(!isAppDarkMode)
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
        binding.recyclerView.addOnScrollListener(PauseLoadWhenScrollingMixedScrollListener())
        binding.recyclerView.grid(2).setup {
            setAnimation(AnimationType.ALPHA)
            addType<AvatarsModel.Res.Category>(R.layout.item_avatars_category)
            onBind {
                val binding = ItemAvatarsCategoryBinding.bind(itemView)
                val model = getModel<AvatarsModel.Res.Category>()
                binding.text.text = model.name
                binding.image.load(
                    model.img, isCrossFade = true, imageEngine = ImageEngine.SKETCH
                )
            }
            R.id.cardview.onClick {
                val model = getModel<AvatarsModel.Res.Category>()
                startActivity<AvatarsPreviewActivity>("ids" to model.id, "title" to model.name)
            }
        }
        binding.page.onRefresh {
            scope {
                binding.recyclerView.models =
                    Get<AvatarsModel>("https://service.avatar.adesk.com/v1/avatar/category") {
                        converter = SerializationConverter("0", "code", "msg")
                    }.await().res.category
            }.catch {
                Timber.tag("壁纸大全").e(it)
            }
        }.autoRefresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}
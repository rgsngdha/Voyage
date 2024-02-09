package com.manchuan.tools.activity.images

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import com.blankj.utilcode.util.ImageUtils
import com.drake.statusbar.immersive
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityBlackGreyBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.saveToAlbum
import com.manchuan.tools.extensions.snack

class BlackGreyPhotoActivity : BaseActivity() {

    private val blackGreyBinding by lazy {
        ActivityBlackGreyBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(blackGreyBinding.root)
        setSupportActionBar(blackGreyBinding.toolbar)
        supportActionBar?.apply {
            title = "图片灰白化"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(blackGreyBinding.toolbar)
        blackGreyBinding.xztp.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        blackGreyBinding.bctp.setOnClickListener {
            if (blackGreyBinding.tp.drawable == null) {
                snack("请先选择图片")
            } else {
                saveToAlbum(blackGreyBinding.tp.drawable.toBitmap(), success = {
                    snack("已保存到相册")
                }, failed = {
                    snack("保存失败")
                })
            }
        }
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                blackGreyBinding.tp.refreshDrawableState()
                blackGreyBinding.tp.setImageBitmap(
                    ImageUtils.toGray(
                        BitmapFactory.decodeFile(
                            getPath(uri)
                        )
                    )
                )
            } else {
                //Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
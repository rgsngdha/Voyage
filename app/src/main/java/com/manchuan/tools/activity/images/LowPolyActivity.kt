package com.manchuan.tools.activity.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.crazylegend.view.toBitmap
import com.drake.statusbar.immersive
import com.dylanc.longan.context
import com.dylanc.longan.toast
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityLowpolyBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.saveToAlbum
import com.zebrostudio.rxlowpoly.Quality
import com.zebrostudio.rxlowpoly.RxLowpoly

class LowPolyActivity : BaseActivity() {
    private var toolbar: Toolbar? = null
    private var lowpoly_image: ImageView? = null
    private var path: String? = null
    private var uri_file: String? = null
    private var isDone = false
    private var saved = false
    private var isSave = true
    private var path2: String? = null
    lateinit var lowpolyBinding: ActivityLowpolyBinding
    private var quality = Quality.VERY_HIGH
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lowpolyBinding = ActivityLowpolyBinding.inflate(layoutInflater)
        setContentView(lowpolyBinding.root)
        toolbar = lowpolyBinding.toolbar
        lowpoly_image = lowpolyBinding.lowpolyImage
        mLowpoly_image = lowpolyBinding.lowpolyImage
        materialbutton1 = lowpolyBinding.materialbutton1
        materialbutton2 = lowpolyBinding.materialbutton2
        materialbutton3 = lowpolyBinding.materialbutton3
        setSupportActionBar(toolbar)
        mLowpoly_image!!.scaleType = ImageView.ScaleType.FIT_CENTER
        mLowpoly_image!!.adjustViewBounds = true
        mLowpoly_image!!.setImageBitmap(null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        immersive(toolbar!!)
        //图片高度
        materialbutton1!!.setOnClickListener {
            path = null
            isDone = false
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        lowpolyBinding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                R.id.very_high -> {
                    quality = Quality.VERY_HIGH
                    return@addOnButtonCheckedListener
                }

                R.id.high -> {
                    quality = Quality.HIGH
                    return@addOnButtonCheckedListener
                }

                R.id.medium -> {
                    quality = Quality.MEDIUM
                    return@addOnButtonCheckedListener
                }

                R.id.low -> {
                    quality = Quality.LOW
                    return@addOnButtonCheckedListener
                }

                R.id.very_low -> {
                    quality = Quality.VERY_LOW
                    return@addOnButtonCheckedListener
                }
            }
        }
        materialbutton2!!.setOnClickListener {
            if (mLowpoly_image!!.drawable != null && path != null) {
                runOnUiThread {
                    try {
                        if (uri_file != null) {
                            if (!isDone) {
                                isSave = false
                                mLowpoly_image!!.drawable.toBitmap().apply {
                                    val generateBitmap =
                                        RxLowpoly.with(context).input(this).quality(quality)
                                            .generateAsync().blockingGet()
                                    bitmap = generateBitmap
                                    mLowpoly_image?.load(
                                        generateBitmap, isCrossFade = true, skipMemory = true
                                    )
                                }
                            } else {
                                toast("已经制作完了，请重新选择图片")
                            }
                        }
                        isDone = true
                    } catch (ignored: Exception) {
                    }
                }
            } else {
                PopTip.show("请先选择图片")
            }
        }
        materialbutton3!!.setOnClickListener {
            if (mLowpoly_image!!.drawable != null && path != null) {
                if (!isSave) {
                    isSave = true
                    saved = true
                    bitmap.saveToAlbum(this, success = {
                        toast("已保存到相册")
                    }, failed = {
                        toast("保存到相册失败")
                    })
                } else {
                    if (saved) {
                        PopTip.show("该图片已保存")
                    } else {
                        PopTip.show("请先选择图片然后生成")
                    }
                }
            } else {
                PopTip.show("请先选择图片")
            }
        }
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // photo picker.
        if (uri != null) {
            val imagePath = getPath(uri)
            mLowpoly_image!!.refreshDrawableState()
            mLowpoly_image!!.setImageBitmap(
                BitmapFactory.decodeFile(imagePath)
            )
            val uris = imagePath
            uri_file = uris
            path = uris
            isSave = true
            saved = false
        }
    }

    @JvmField
    var mLowpoly_image: ImageView? = null
    private var materialbutton1: MaterialButton? = null
    private var materialbutton2: MaterialButton? = null
    private var materialbutton3: MaterialButton? = null

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar!!.title = "LowPoly图片生成"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
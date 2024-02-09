package com.manchuan.tools.activity.images

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.drake.statusbar.immersive
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPhotoMiaoBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.utils.ImageUtil
import com.manchuan.tools.utils.SecondSketchFilter
import rikka.material.app.MaterialActivity

class PhotoMiaoActivity : BaseActivity() {
    private var toolbar: Toolbar? = null
    private var isNeed = false
    private var isFinished = false
    private var mNeed_lay: LinearLayout? = null
    private var mNeed_photo: ImageView? = null
    private var start: MaterialButton? = null
    private var save: MaterialButton? = null
    private var miaoBinding: ActivityPhotoMiaoBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        miaoBinding = ActivityPhotoMiaoBinding.inflate(LayoutInflater.from(this))
        setContentView(miaoBinding?.root)
        toolbar = miaoBinding?.toolbar
        mNeed_lay = miaoBinding?.needLay
        mNeed_photo = miaoBinding?.needPhoto
        start = miaoBinding?.start
        save = miaoBinding?.save
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "图片素描"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(toolbar!!)
        if (isNeed) {
            mNeed_lay!!.visibility = View.GONE
        } else {
            mNeed_lay!!.visibility = View.VISIBLE
            mNeed_photo!!.setOnClickListener { view: View? ->
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
        start!!.setOnClickListener { view: View? ->
            if (!isNeed) {
                TipDialog.show("请先选择图片", WaitDialog.TYPE.WARNING)
            } else {
                miaoBinding?.progressBar?.visibility = View.VISIBLE
                val bitmapSource = ImageUtil.drawableToBitmap(
                    mNeed_photo!!.drawable
                )
                val simpleSketch = arrayOf<Bitmap?>(null)
                Thread {
                    simpleSketch[0] = SecondSketchFilter().getSimpleSketch(bitmapSource)
                    mNeed_photo?.setImageBitmap(simpleSketch[0])
                    TipDialog.show("绘制完成", WaitDialog.TYPE.SUCCESS)
                    runOnUiThread {
                        miaoBinding?.progressBar?.visibility = View.GONE
                    }
                    isFinished = true
                }.start()
            }
        }
        save!!.setOnClickListener { view: View? ->
            if (!isFinished) {
                TipDialog.show("请先绘制图片", WaitDialog.TYPE.WARNING)
            } else {
                ImageUtil.SaveImageToSkect(
                    this, ImageUtil.drawableToBitmap(
                        mNeed_photo!!.drawable
                    ), "SKECT_PHOTO_" + System.currentTimeMillis() + ".png"
                )
                TipDialog.show("已保存", WaitDialog.TYPE.SUCCESS)
            }
        }
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Glide.with(this).load(
                    getPath(uri)
                ).skipMemoryCache(true).into(
                    mNeed_photo!!
                )
                mNeed_lay!!.visibility = View.GONE
                isNeed = true
            } else {

            }
        }

}
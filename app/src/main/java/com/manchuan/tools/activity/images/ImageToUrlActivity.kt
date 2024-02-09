package com.manchuan.tools.activity.images

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ClipboardUtils
import com.bumptech.glide.Glide
import com.drake.net.Post
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.drake.statusbar.immersive
import com.google.android.material.appbar.MaterialToolbar
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityPhotoColoringBinding
import com.manchuan.tools.extensions.getPath
import rikka.material.app.MaterialActivity
import java.io.File

@SuppressLint("NonConstantResourceId")
class ImageToUrlActivity : MaterialActivity() {
    private lateinit var toolbar: MaterialToolbar
    private val isNeed = false
    private val imageToUrlBinding by lazy {
        ActivityPhotoColoringBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(imageToUrlBinding.root)
        toolbar = imageToUrlBinding.toolbar
        mAdd_photo = imageToUrlBinding.addPhoto
        mNeed_lay = imageToUrlBinding.needLay
        mNeed_photo = imageToUrlBinding.needPhoto
        setSupportActionBar(toolbar)
        toolbar.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        supportActionBar?.apply {
            title = "图片转链接"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(toolbar)
        if (isNeed) {
            mNeed_lay!!.visibility = View.GONE
        } else {
            mNeed_lay!!.visibility = View.VISIBLE
            mNeed_photo!!.setOnClickListener { view: View? ->
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                scopeNetLife {
                    val body = Post<String>("http://pic.sogou.com/pic/upload_pic.jsp") {
                        param("file", File(getPath(uri).toString()))
                        addUploadListener(object : ProgressListener() {
                            override fun onProgress(p: Progress) {
                                imageToUrlBinding.progressBar.post {
                                    imageToUrlBinding.progressBar.setProgressCompat(
                                        p.progress(), true
                                    )
                                    supportActionBar?.title =
                                        "已上传:${p.progress()}% 剩余大小:${p.remainSize()}"
                                }
                            }
                        })
                    }.await()
                    mNeed_lay?.visibility = View.GONE
                    imageToUrlBinding.progressBar.visibility = View.GONE
                    imageToUrlBinding.progressBar.setProgressCompat(0, false)
                    Glide.with(this@ImageToUrlActivity).load(body).into(mNeed_photo!!)
                    ClipboardUtils.copyText("ImgUrl", body)
                    TipDialog.show("已复制链接", WaitDialog.TYPE.SUCCESS)
                    supportActionBar?.title = "图片转链接"
                }
            }
        }

    private var mNeed_photo: ImageView? = null
    private var mAdd_photo: ImageView? = null
    private var mNeed_lay: LinearLayout? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
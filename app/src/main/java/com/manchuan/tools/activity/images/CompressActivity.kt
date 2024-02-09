package com.manchuan.tools.activity.images

import ando.file.core.FileUri
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.FileUtils
import com.drake.statusbar.immersive
import com.dylanc.longan.context
import com.dylanc.longan.toast
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.databinding.ActivityCompressBinding
import com.manchuan.tools.extensions.ioScope
import com.manchuan.tools.extensions.saveToAlbum
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.uiScope
import kotlinx.coroutines.launch
import rikka.material.app.MaterialActivity
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.util.*


class CompressActivity : MaterialActivity() {
    private var toolbar: Toolbar? = null
    private var path: String? = null
    private var compressBinding: ActivityCompressBinding? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compressBinding = ActivityCompressBinding.inflate(LayoutInflater.from(this))
        setContentView(compressBinding?.root)
        toolbar = compressBinding?.toolbar
        mXztp = compressBinding?.xztp
        mYstp = compressBinding?.ystp
        mTp1 = compressBinding?.tp1
        mTp2 = compressBinding?.tp2
        mTxt1 = compressBinding?.txt1
        mTxt2 = compressBinding?.txt2
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "图片压缩"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(toolbar!!)
        mXztp!!.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        mYstp!!.setOnClickListener {
            if (path.isNullOrEmpty()) {
                PopTip.show("未选择图片")
            } else {
                Luban.with(this).load(path?.let { it1 -> File(it1) }).ignoreBy(100)
                    .setTargetDir(com.dylanc.longan.externalPicturesDirPath).filter { path ->
                        !(TextUtils.isEmpty(path) || path.lowercase(Locale.getDefault())
                            .endsWith(".gif"))
                    }.setCompressListener(object : OnCompressListener {
                        override fun onStart() {
                            // TODO 压缩开始前调用，可以在方法内启动 loading UI
                            WaitDialog.show("压缩中...")
                        }

                        override fun onSuccess(file: File?) {
                            TipDialog.show("压缩完成", WaitDialog.TYPE.SUCCESS)
                            ioScope.launch {
                                file?.inputStream()?.apply {
                                    saveToAlbum(context, success = {
                                        file.delete()
                                        uiScope.launch {
                                            toast("已保存到相册")
                                        }
                                    }, failed = {
                                        uiScope.launch {
                                            toast("保存到相册失败")
                                        }
                                    })
                                }
                            }
                        }

                        override fun onError(e: Throwable?) {
                            // TODO 当压缩过程出现问题时调用
                            WaitDialog.dismiss()
                            PopTip.show("出现错误：$e")
                        }
                    }).launch()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            mTxt1!!.text = "大小:${FileUtils.getSize(FileUri.getPathByUri(uri))}"
            path = FileUri.getPathByUri(uri)
        } else {
            snack("已取消选择")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private var mTp1: ImageView? = null
    private var mTp2: ImageView? = null
    private var mXztp: MaterialButton? = null
    private var mYstp: MaterialButton? = null
    private var mTxt1: TextView? = null
    private var mTxt2: TextView? = null
}
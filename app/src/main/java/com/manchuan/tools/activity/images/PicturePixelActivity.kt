package com.manchuan.tools.activity.images

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.crazylegend.view.toBitmap
import com.drake.statusbar.immersive
import com.dylanc.longan.context
import com.dylanc.longan.toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.kongzue.dialogx.dialogs.PopTip
import com.luoye.fpic.util.ConvertUtils
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityPhotoPixelBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.saveToAlbum
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.utils.ImageUtil
import java.io.File
import java.util.*

class PicturePixelActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var seekbar1: Slider? = null
    private var xztp: MaterialButton? = null
    private var bctp: MaterialButton? = null
    private var tp: ImageView? = null
    private val pixelBinding by lazy {
        ActivityPhotoPixelBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(pixelBinding.root)
        toolbar = pixelBinding.toolbar
        seekbar1 = pixelBinding.seekbar1
        bctp = pixelBinding.bctp
        xztp = pixelBinding.xztp
        tp = pixelBinding.tp
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        immersive(toolbar!!)
        xztp!!.setOnClickListener { view: View? ->
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        bctp!!.setOnClickListener { view: View? ->
            if (tp!!.drawable == null) {
                snack("请先选择图片")
            } else {
                try {
                    tp!!.refreshDrawableState()
                    val fileName = "PICTURE_PIXEL_" + System.currentTimeMillis() + ".png"
                    tp!!.setImageBitmap(
                        ConvertUtils.getBlockBitmap(
                            ImageUtil.drawableToBitmap(
                                tp!!.drawable
                            ), seekbar1!!.value.toInt()
                        )
                    )
                    tp!!.drawable.toBitmap().saveToAlbum(context, success = {
                        toast("保存成功")
                    }, failed = {
                        snack("保存失败")
                    })
                } catch (e: Exception) {
                    snack("保存失败")
                }
            }
        }
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                tp!!.refreshDrawableState()
                Glide.with(this@PicturePixelActivity).load(
                    File(
                        getPath(uri).toString()
                    )
                ).into(
                    tp!!
                )
            } else {
                snack("已取消图片选择")
            }
        }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.title = "图片像素化"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10002 && resultCode == RESULT_OK) {
            val fileUri = data!!.data
            tp!!.refreshDrawableState()
            Glide.with(this@PicturePixelActivity).load(
                File(
                    getPath(fileUri!!)
                )
            ).into(
                tp!!
            )
            //tp.setImageBitmap(BitmapFactory.decodeFile(listToString02(mSelected)));
        } else if (requestCode == 10002 && resultCode == RESULT_CANCELED) {
            //mSelected = Matisse.Companion.obtainPathResult(data);
            PopTip.show(R.mipmap.ic_voyage, "已取消选择")
            //SnackbarToast.makeText("已取消选择", this.getWindow().getDecorView());
            //mActivity_qrcode_logo_edit.setText(listToString02(mSelected));
        }
    }

    fun listToString02(list: List<String?>?): String {
        var resultString = ""
        if (null == list) {
            println("list内容为空！")
        } else {
            val sb = StringBuilder()
            var flag = false
            for (str in list) {
                if (flag) {
                    sb.append(",")
                } else {
                    flag = true
                }
                sb.append(str)
            }
            resultString = sb.toString()
            //System.out.println("最后拼接的字符串结果：" + resultString);
        }
        return resultString
    }
}
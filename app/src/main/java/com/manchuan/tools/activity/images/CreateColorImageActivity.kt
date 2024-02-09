package com.manchuan.tools.activity.images

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.crazylegend.kotlinextensions.bitmap.createColoredBitmap
import com.crazylegend.kotlinextensions.bitmap.makeCircle
import com.crazylegend.kotlinextensions.bitmap.toDrawable
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.serialize.serialize.serialLazy
import com.drake.statusbar.immersive
import com.dylanc.longan.context
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.InputDialog
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityCreateColorImageBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.saveToAlbum
import com.manchuan.tools.widget.colorpicker.ColorPickerView
import com.manchuan.tools.widget.colorpicker.builder.ColorPickerDialogBuilder

class CreateColorImageActivity : BaseActivity() {

    private val binding by viewBinding(ActivityCreateColorImageBinding::inflate)

    private var colorImageColor by serialLazy(Color.WHITE)
    private var colorImageWidth by serialLazy(400)
    private var colorImageHeight by serialLazy(400)
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersive(binding.toolbar, !isAppDarkMode)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "纯色图生成"
        }
        binding.apply {
            widthContent.text = colorImageWidth.toString()
            heightContent.text = colorImageHeight.toString()
            imageColor.chipIcon =
                createColoredBitmap(colorImageColor, 200, 200).makeCircle()?.toDrawable(context)
            create.throttleClick {
                bitmap = createColoredBitmap(colorImageColor, colorImageWidth, colorImageHeight)
                image.load(bitmap, isCrossFade = true)
                bitmap.saveToAlbum(context, success = {
                    toast("保存到相册成功")
                }, failed = {
                    toast("保存到相册失败")
                })
            }
            imageColor.throttleClick {
                ColorPickerDialogBuilder.with(this@CreateColorImageActivity).setTitle("选择颜色")
                    .showColorEdit(true).initialColor(colorImageColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(12)
                    .setPositiveButton("确定") { dialog: DialogInterface?, selectedColor: Int, allColors: Array<Int?>? ->
                        imageColor.chipIcon =
                            createColoredBitmap(selectedColor, 200, 200).makeCircle()
                                ?.toDrawable(context)
                        colorImageColor = selectedColor
                    }.setNegativeButton("取消") { dialog: DialogInterface?, which: Int -> }.build()
                    .show()
            }
            imageHeight.throttleClick {
                InputDialog(
                    "图片高度",
                    null,
                    "确定",
                    "取消",
                    heightContent.textString.ifBlank { "" }).setCancelable(
                    false
                ).setOkButton { baseDialog: InputDialog, v: View, inputStr: String ->
                    if (inputStr.isNotBlank()) {
                        heightContent.text = inputStr
                        colorImageHeight = inputStr.toInt()
                    } else {
                        baseDialog.show()
                        toast("不能为空")
                    }
                    false
                }.show()
            }
            imageWidth.throttleClick {
                InputDialog(
                    "图片宽度",
                    null,
                    "确定",
                    "取消",
                    widthContent.textString.ifBlank { "" }).setCancelable(
                    false
                ).setOkButton { baseDialog: InputDialog, v: View, inputStr: String ->
                    if (inputStr.isNotBlank()) {
                        widthContent.text = inputStr
                        colorImageWidth = inputStr.toInt()
                    } else {
                        baseDialog.show()
                        toast("不能为空")
                    }
                    false
                }.show()
            }
        }
    }


}
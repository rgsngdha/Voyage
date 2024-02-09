package com.manchuan.tools.activity.images

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.crazylegend.kotlinextensions.bitmap.createColoredBitmap
import com.crazylegend.kotlinextensions.bitmap.makeCircle
import com.crazylegend.kotlinextensions.bitmap.toDrawable
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.serialize.serialize.serialLazy
import com.dylanc.longan.context
import com.dylanc.longan.externalCacheDirPath
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPhotoTextBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.saveToAlbum
import com.manchuan.tools.user.timeMills
import com.manchuan.tools.widget.colorpicker.ColorPickerView
import com.manchuan.tools.widget.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

@SuppressLint("NonConstantResourceId")
class PhotoTextActivity : BaseActivity() {
    private var input: File? = null

    private val binding by viewBinding(ActivityPhotoTextBinding::inflate)
    private var textImageColor by serialLazy(Color.TRANSPARENT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.apply {
            textColor.chipIcon =
                createColoredBitmap(this@PhotoTextActivity.textImageColor, 200, 200).makeCircle()
                    ?.toDrawable(context)
            select.throttleClick {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            save.throttleClick {
                if (input != null) {
                    if (content.textString.isNotBlank()) {
                        convert(input, text = content.textString, fontSize = size.value.toInt())
                    } else {
                        toast("未输入文字内容")
                    }
                } else {
                    toast("请先选择图片")
                }
            }
            edit.throttleClick {
                InputDialog(
                    "文字内容",
                    null,
                    "确定",
                    "取消",
                    content.textString.ifBlank { "" }).setCancelable(
                    false
                ).setOkButton { baseDialog: InputDialog, v: View, inputStr: String ->
                    //toast("输入的内容：" + inputStr);
                    if (inputStr.isNotBlank()) {
                        content.text = inputStr
                    } else {
                        baseDialog.show()
                        toast("内容不能为空")
                    }
                    false
                }.show()
            }
            textColor.throttleClick {
                ColorPickerDialogBuilder.with(this@PhotoTextActivity).setTitle("选择颜色")
                    .showColorEdit(true).initialColor(textImageColor)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER).density(12)
                    .setPositiveButton("确定") { dialog: DialogInterface?, selectedColor: Int, allColors: Array<Int?>? ->
                        //chip2.setChipIconTint(new ColorStateList(null,new int[]{selectedColor}));
                        textColor.chipIcon =
                            createColoredBitmap(selectedColor, 200, 200).makeCircle()
                                ?.toDrawable(context)
                        this@PhotoTextActivity.textImageColor = selectedColor
                    }.setNegativeButton("取消") { dialog: DialogInterface?, which: Int -> }.build()
                    .show()
            }
        }
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            input = File(getPath(uri))
            binding.image.load(input, isCrossFade = true, skipMemory = true)
            binding.save.animateVisible()
            //Log.d("PhotoPicker", "Selected URI: $uri")
        } else {
            binding.save.animateGone()
            //Log.d("PhotoPicker", "No media selected")
        }
    }

    private var style = 0

    /**
     * 转换
     *
     * @param input
     * @param output
     * @param text
     * @param fontSize
     */
    private fun convert(
        input: File?,
        output: File? = File(externalCacheDirPath, "$timeMills.png"),
        text: String?,
        fontSize: Int,
    ) {
        WaitDialog.show("生成中")
        ioScope.launch {
            val bitmap = BitmapFactory.decodeFile(input!!.absolutePath)
            val target: Bitmap = if (style == 0) {
                getTextBitmap(bitmap, textImageColor, text, fontSize)
            } else {
                getBlockBitmap(bitmap, fontSize)
            }
            var fileOutputStream: FileOutputStream? = null
            var byteArrayOutputStream: ByteArrayOutputStream? = null
            runCatching {
                fileOutputStream = FileOutputStream(output)
                byteArrayOutputStream = ByteArrayOutputStream()
                target.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream!!)
                val data = byteArrayOutputStream!!.toByteArray()
                fileOutputStream!!.write(data, 0, data.size)
                fileOutputStream!!.flush()
                output?.inputStream()?.apply {
                    saveToAlbum(context, success = {
                        uiScope.launch {
                            binding.image.load(
                                it, isCrossFade = true, skipMemory = true
                            )
                            output.delete()
                            TipDialog.show("生成完成", WaitDialog.TYPE.SUCCESS)
                            toast("已保存到相册")
                        }
                    }, failed = {
                        uiScope.launch {
                            TipDialog.show("生成失败", WaitDialog.TYPE.ERROR)
                            toast("保存到相册失败")
                        }
                    })
                }
                return@launch
            }.onFailure {
                it.printStackTrace()
                TipDialog.show(it.message, WaitDialog.TYPE.ERROR)
            }.onSuccess {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar?.title = "图片文字化"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getBlockBitmap(bitmap: Bitmap?, blockSize: Int): Bitmap {
        requireNotNull(bitmap) { "Bitmap cannot be null." }
        val picWidth = bitmap.width
        val picHeight = bitmap.height
        val back = Bitmap.createBitmap(
            if (bitmap.width % blockSize == 0) bitmap.width else bitmap.width / blockSize * blockSize,
            if (bitmap.height % blockSize == 0) bitmap.height else bitmap.height / blockSize * blockSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(back)
        canvas.drawColor(0xfff)
        var y = 0
        while (y < picHeight) {
            var x = 0
            while (x < picWidth) {
                val colors = getPixels(bitmap, x, y, blockSize, blockSize)
                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = getAverage(colors)
                paint.style = Paint.Style.FILL
                val left = x
                val top = y
                val right = x + blockSize
                val bottom = y + blockSize
                canvas.drawRect(
                    left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint
                )
                x += blockSize
            }
            y += blockSize
        }
        return back
    }

    private fun getTextBitmap(
        bitmap: Bitmap?,
        backColor: Int,
        text: String?,
        fontSize: Int,
    ): Bitmap {
        requireNotNull(bitmap) { "Bitmap cannot be null." }
        val picWidth = bitmap.width
        val picHeight = bitmap.height
        val back = Bitmap.createBitmap(
            if (bitmap.width % fontSize == 0) bitmap.width else bitmap.width / fontSize * fontSize,
            if (bitmap.height % fontSize == 0) bitmap.height else bitmap.height / fontSize * fontSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(back)
        canvas.drawColor(backColor)
        var idx = 0
        var y = 0
        while (y < picHeight) {
            var x = 0
            while (x < picWidth) {
                val colors = getPixels(bitmap, x, y, fontSize, fontSize)
                val paint = Paint()
                paint.isAntiAlias = true
                paint.color = getAverage(colors)
                paint.textSize = fontSize.toFloat()
                val fontMetrics = paint.fontMetrics
                val padding =
                    if (y == 0) fontSize + fontMetrics.ascent else (fontSize + fontMetrics.ascent) * 2
                canvas.drawText(text!![idx++].toString(), x.toFloat(), y - padding, paint)
                if (idx == text.length) {
                    idx = 0
                }
                x += fontSize
            }
            y += fontSize
        }
        return back
    }

    private fun getPixels(bitmap: Bitmap, x: Int, y: Int, w: Int, h: Int): IntArray {
        val colors = IntArray(w * h)
        var idx = 0
        var i = y
        while (i < h + y && i < bitmap.height) {
            var j = x
            while (j < w + x && j < bitmap.width) {
                val color = bitmap.getPixel(j, i)
                colors[idx++] = color
                j++
            }
            i++
        }
        return colors
    }

    /**
     * 求取多个颜色的平均值
     *
     * @param colors
     * @return
     */
    private fun getAverage(colors: IntArray): Int {
        //int alpha=0;
        var red = 0
        var green = 0
        var blue = 0
        for (color in colors) {
            red += color and 0xff0000 shr 16
            green += color and 0xff00 shr 8
            blue += color and 0x0000ff
        }
        val len = colors.size.toFloat()
        //alpha=Math.round(alpha/len);
        red = (red / len).roundToInt()
        green = (green / len).roundToInt()
        blue = (blue / len).roundToInt()
        return Color.argb(0xff, red, green, blue)
    }
}
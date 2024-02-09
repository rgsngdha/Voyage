package com.manchuan.tools.activity.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.crazylegend.viewbinding.viewBinding
import com.drake.net.utils.runMain
import com.drake.net.utils.withIO
import com.drake.net.utils.withMain
import com.dylanc.longan.context
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.click
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityHideImageBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.rootView
import com.manchuan.tools.extensions.saveToAlbum
import com.manchuan.tools.utils.BitmapPixelUtil
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class HideImageActivity : BaseActivity() {

    private val binding by viewBinding(ActivityHideImageBinding::inflate)
    private var mScreenWidth by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "隐藏图制作"
            setDisplayHomeAsUpEnabled(true)
        }
        mScreenWidth = rootView.measuredWidth
        immerseStatusBar(!isAppDarkMode)
        binding.apply {
            top.click {
                topImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            bottom.click {
                bottomImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            create.click {
                if (tp1.drawable != null && tp2.drawable != null) {
                    Thread {
                        var bitmap1 = (tp1.drawable as BitmapDrawable).bitmap
                        var bitmap2 = (tp2.drawable as BitmapDrawable).bitmap
                        if (bitmap1!!.byteCount > bitmap2!!.byteCount) {
                            bitmap1 =
                                BitmapPixelUtil.scaleBitmap(bitmap1, bitmap2.width, bitmap2.height)
                        } else if (bitmap1.byteCount < bitmap2.byteCount) {
                            bitmap2 =
                                BitmapPixelUtil.scaleBitmap(bitmap2, bitmap1.width, bitmap1.height)
                        }
                        runMain {
                            WaitDialog.show("处理中...")
                        }
                        ioScope.launch {
                            val resultBitmap = BitmapPixelUtil.makeHideImage(
                                bitmap1, bitmap2
                            ) { progress: Float ->
                                loge(progress)
                                runMain {
                                    WaitDialog.show("处理中", progress)
                                }
                            }
                            withMain {
                                resultImage.load(resultBitmap, isCrossFade = true)
                            }
                            resultBitmap.saveToAlbum(context, failed = {
                                TipDialog.show("保存失败", WaitDialog.TYPE.ERROR)
                            }, success = {
                                TipDialog.show("已保存到相册", WaitDialog.TYPE.SUCCESS)
                            })
                        }
                    }.start()
                } else {
                    toast("上层或下层图片为空")
                }
            }
        }
    }

    val topImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                //Log.d("PhotoPicker", "Selected URI: $uri")
                var bitmap =
                    BitmapFactory.decodeFile(getPath(uri)).copy(Bitmap.Config.ARGB_8888, true)
                binding.tp1.setImageBitmap(bitmap)

                val width: Int = mScreenWidth / 2
                runCatching {
                    if (bitmap.width > width) {
                        bitmap = BitmapPixelUtil.scaleBitmap(
                            bitmap, width, (width.toFloat() / bitmap.width * bitmap.height).toInt()
                        )
                        binding.tp1.setImageBitmap(bitmap)
                    }
                }
            } else {
                //Log.d("PhotoPicker", "No media selected")
            }
        }

    val bottomImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                var bitmap =
                    BitmapFactory.decodeFile(getPath(uri)).copy(Bitmap.Config.ARGB_8888, true)
                val width: Int = mScreenWidth / 2
                binding.tp2.setImageBitmap(bitmap)
                runCatching {
                    if (bitmap.width > width) {
                        bitmap = BitmapPixelUtil.scaleBitmap(
                            bitmap, width, (width.toFloat() / bitmap.width * bitmap.height).toInt()
                        )
                        binding.tp2.setImageBitmap(bitmap)
                    }
                }
            } else {

            }
        }

}
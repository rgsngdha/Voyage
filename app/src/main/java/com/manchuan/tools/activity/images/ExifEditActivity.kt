package com.manchuan.tools.activity.images

import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.drake.engine.utils.throttleClick
import com.drake.statusbar.immersive
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.google.android.material.datepicker.MaterialDatePicker
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.toDateString
import com.manchuan.tools.activity.images.models.ExifModels
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityExifBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.utils.ExifUtils

class ExifEditActivity : BaseActivity() {

    private val binding by lazy {
        ActivityExifBinding.inflate(layoutInflater)
    }

    private lateinit var exifModels: ExifModels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.apply {
            immersive(appbar)
            save.throttleClick {
                saveExifAttribute()
            }
            select.throttleClick {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            rotateLayout.throttleClick {

            }
            timeLayout.throttleClick {
                val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("选择日期")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
                datePicker.addOnPositiveButtonClickListener {
                    binding.exifDate.text = it.toDateString()
                }
                datePicker.show(supportFragmentManager, "选择日期")
            }
        }
    }

    private var path = ""

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            binding.fileImage.load(uri, isCrossFade = true, skipMemory = true)
            path = getPath(uri)
            refreshExifInfo(path)
        } else {
            //Log.d("PhotoPicker", "No media selected")
        }
    }


    private fun saveExifAttribute() {
        runCatching {
            binding.apply {
                ExifUtils.setExif(
                    if (exifRotate.textString.contains("无")) null else exifRotate.textString,
                    if (exifDate.textString.contains("无")) null else exifDate.textString,
                    if (exifBrand.textString.contains("无")) null else exifBrand.textString,
                    if (exifModel.textString.contains("无")) null else exifModel.textString,
                    if (exifFlash.textString.contains("无")) null else exifFlash.textString,
                    if (exifLength.textString.contains("无")) null else exifLength.textString,
                    if (exifWidth.textString.contains("无")) null else exifWidth.textString,
                    if (exifLatitude.textString.contains("无")) null else exifLatitude.textString,
                    if (exifLongitude.textString.contains("无")) null else exifLongitude.textString,
                    if (exifExposure.textString.contains("无")) null else exifExposure.textString,
                    if (exifAperture.textString.contains("无")) null else exifAperture.textString,
                    if (exifIso.textString.contains("无")) null else exifIso.textString,
                    if (exifDigitized.textString.contains("无")) null else exifDigitized.textString,
                    if (exifAltitude.textString.contains("无")) null else exifAltitude.textString
                )
                ExifUtils.saveAttributes()
                refreshExifInfo(path)
            }
        }.onFailure {
            snack("保存失败")
        }.onSuccess {
            snack("保存成功")
        }
    }

    private fun refreshExifInfo(path: String) {
        runCatching {
            ExifUtils.setPath(path)
            binding.apply {
                scrollLayout.animateVisible()
                save.animateVisible()
                exifRotate.text = ExifUtils.orientation
                exifDate.text = ExifUtils.dateTime
                exifBrand.text = ExifUtils.make
                exifModel.text = ExifUtils.model
                exifFlash.text = ExifUtils.flash
                exifLength.text = ExifUtils.length
                exifWidth.text = ExifUtils.width
                exifLatitude.text = ExifUtils.latitude
                exifLongitude.text = ExifUtils.longitude
                exifExposure.text = ExifUtils.exposure
                exifAltitude.text = ExifUtils.altitude
                exifAperture.text = ExifUtils.aperture
                exifDigitized.text = ExifUtils.digitized
                exifIso.text = ExifUtils.iso
            }
        }.onFailure {
            it.printStackTrace()
            toast("图片元数据刷新失败:${it.message}")
        }
    }

}
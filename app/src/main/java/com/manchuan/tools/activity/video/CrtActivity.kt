package com.manchuan.tools.activity.video

import ando.file.core.FileUri
import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.drake.statusbar.immersive
import com.dylanc.longan.isAppDarkMode
import com.github.dhaval2404.imagepicker.ImagePicker
import com.lxj.androidktx.snackbar
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityCrtBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.snack

class CrtActivity : BaseActivity() {

    private val binding by lazy {
        ActivityCrtBinding.inflate(layoutInflater)
    }


    private val formats = arrayOf(
        "MP4",
        "WMV",
        "MPEG",
        "M4V",
        "MOV",
        "FLV",
        "AVI",
        "MKV",
    )

    private val present = arrayOf(
        "ultrafast",
        "superfast",
        "veryfast",
        "faster",
        "fast",
        "medium",
        "slow",
        "slower",
        "veryslow",
        "placebo"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLayout.toolbar)
        val formats = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, formats)
        binding.formats.setAdapter(formats)
        val presents = ArrayAdapter(this, R.layout.cat_exposed_dropdown_popup_item, present)
        binding.bytesInput.setAdapter(presents)
        supportActionBar?.apply {
            title = "视频增加字幕"
            setDisplayHomeAsUpEnabled(true)
        }
        immersive(binding.toolbarLayout.toolbar, !isAppDarkMode)
        binding.colorPicker.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
        binding.crtPicker.setOnClickListener {
            ImagePicker.with(this).galleryOnly().galleryMimeTypes(arrayOf("*/*"))
                .createIntent { intent ->
                    startForSrtResult.launch(intent)
                }
        }
    }


    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                path = getPath(uri)
                binding.colorString.text = "已选择"
            } else {
                snack("选择已取消")
            }
        }

    private val startForSrtResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    srtpath = FileUri.getPathByUri(data?.data).toString()
                    binding.titleString.text = "已选择"
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }

                else -> {
                    snackbar("选择已取消")
                }
            }
        }

    private var path = ""
    private var srtpath = ""


}
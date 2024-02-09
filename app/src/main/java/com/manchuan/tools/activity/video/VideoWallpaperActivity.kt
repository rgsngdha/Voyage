package com.manchuan.tools.activity.video

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.crazylegend.viewbinding.viewBinding
import com.lxj.androidktx.core.click
import com.lxj.androidktx.snackbar
import com.manchuan.tools.R
import com.manchuan.tools.activity.video.wallpaper.VideoLiveWallpaperService
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityVideoWallpaperBinding
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.snack

class VideoWallpaperActivity : BaseActivity() {

    private var path = ""
    private val binding by viewBinding(ActivityVideoWallpaperBinding::inflate)

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "视频壁纸"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.colorPicker.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
        binding.create.setOnClickListener {
            if (path.isEmpty()) {
                snackbar("未选择文件或文件路径为空")
            } else {
                //formatVideo()
            }
        }
        binding.colorPicker.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
        binding.mute.isChecked = Global.isVideoWallpaperMute
        binding.wallpaperMute.click {
            val isMute = Global.isVideoWallpaperMute.not()
            binding.mute.isChecked = isMute
            Global.isVideoWallpaperMute = isMute
            runCatching {
                when (isMute) {
                    true -> {
                        VideoLiveWallpaperService.muteMusic(applicationContext)
                    }

                    false -> {
                        VideoLiveWallpaperService.unmuteMusic(applicationContext)
                    }
                }
            }
        }
        binding.create.click {
            this@VideoWallpaperActivity.openFileOutput(
                "video_live_wallpaper_file_path", Context.MODE_PRIVATE
            ).use {
                it.write(path.toByteArray())
            }
            VideoLiveWallpaperService.setToWallPaper(this@VideoWallpaperActivity)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.settings -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
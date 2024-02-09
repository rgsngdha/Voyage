package com.manchuan.tools.activity.images

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.crazylegend.viewbinding.viewBinding
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.fileProviderAuthority
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.toast
import com.github.panpf.sketch.displayImage
import com.github.panpf.sketch.request.DisplayRequest
import com.github.panpf.sketch.request.DisplayResult
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityImagePreviewBinding
import com.manchuan.tools.extensions.addAlpha
import com.manchuan.tools.extensions.progress
import com.manchuan.tools.extensions.publicPicturesDirPath
import com.manchuan.tools.extensions.savePic
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.user.timeMills
import com.manchuan.tools.utils.setWallpaper
import rikka.html.text.HtmlCompat
import java.io.File

class ImagePreviewActivity : BaseActivity() {

    private val binding by viewBinding(ActivityImagePreviewBinding::inflate)

    private val imageUrl by safeIntentExtras<String>("url")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersionBar {
            transparentBar()
            titleBar(binding.toolbar)
            statusBarDarkFont(false)
        }
        //设置状态栏文字颜色及图标为深色
        supportActionBar?.apply {
            title = HtmlCompat.fromHtml("<font color='#FFFFFF'>图片预览</font>")
            setDisplayHomeAsUpEnabled(true)
        }
        with(binding) {
            toolbar.setBackgroundColor(Color.BLACK.addAlpha(0.7f))
        }
        if (imageUrl.isNotEmpty()) {
            with(binding) {
                image.displayImage(imageUrl) {
                    crossfade()
                    listener(
                        onStart = { request: DisplayRequest ->
                            // ...
                            binding.progress.show()
                            binding.progress.isIndeterminate = false
                        },
                        onSuccess = { request: DisplayRequest, result: DisplayResult.Success ->
                            // ...
                            binding.progress.hide()
                            binding.progress.isIndeterminate = true
                        },
                        onError = { request: DisplayRequest, result: DisplayResult.Error ->
                            // ...
                        },
                        onCancel = { request: DisplayRequest ->
                            // ...
                        },
                    )
                    progressListener { request: DisplayRequest, totalLength: Long, completedLength: Long ->
                        // ...
                        binding.progress.progress((completedLength / totalLength).toInt())
                    }
                }
            }
        } else {
            snack("图片链接为空")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_wallpaper -> {
                scopeNetLife {
                    val file = Get<File>(imageUrl) {
                        setDownloadDir(publicPicturesDirPath)
                        setDownloadFileName("$timeMills.png")
                        setDownloadFileNameDecode(true)
                        setDownloadFileNameConflict(true)
                        setDownloadMd5Verify(true)
                        addDownloadListener(object : ProgressListener() {
                            override fun onProgress(p: Progress) {
                            }

                        })
                    }.await()
                    toast("加载完成")
                    setWallpaper(file.absolutePath, fileProviderAuthority)
                }.catch {
                    toast("加载失败")
                }
            }

            R.id.save_image -> {
                savePic(imageUrl)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_image_viewer, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
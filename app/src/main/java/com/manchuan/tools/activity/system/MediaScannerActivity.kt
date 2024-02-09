package com.manchuan.tools.activity.system

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.drake.statusbar.immersive
import com.manchuan.tools.databinding.ActivityMediaScannerBinding
import com.manchuan.tools.extensions.publicAudiosDirPath
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.extensions.publicMoviesDirPath
import com.manchuan.tools.extensions.publicPicturesDirPath
import com.mcxiaoke.koi.ext.addToMediaStore
import com.mcxiaoke.koi.ext.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class MediaScannerActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMediaScannerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersive(binding.toolbar)
        supportActionBar?.apply {
            title = "媒体库刷新"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.layout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.fab.setOnClickListener {
            binding.progress.show()
            supportActionBar?.apply {
                title = "扫描中"
            }
            runBlocking {
                val scan = launch(Dispatchers.IO) {
                    File(publicPicturesDirPath).listFiles()?.forEach { file ->
                        if (file.isDirectory) {
                            file.listFiles()?.forEach {
                                addToMediaStore(it)
                            }
                        } else {
                            addToMediaStore(file)
                        }
                    }
                    File(publicMoviesDirPath).listFiles()?.forEach { file ->
                        if (file.isDirectory) {
                            file.listFiles()?.forEach {
                                addToMediaStore(it)
                            }
                        } else {
                            addToMediaStore(file)
                        }
                    }
                    File(publicDownloadsDirPath).listFiles()?.forEach { file ->
                        if (file.isDirectory) {
                            file.listFiles()?.forEach {
                                addToMediaStore(it)
                            }
                        } else {
                            addToMediaStore(file)
                        }
                    }
                    File(publicAudiosDirPath).listFiles()?.forEach { file ->
                        if (file.isDirectory) {
                            file.listFiles()?.forEach {
                                addToMediaStore(it)
                            }
                        } else {
                            addToMediaStore(file)
                        }
                    }
                }
                scan.join()
                binding.progress.hide()
                toast("Scan Completed")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
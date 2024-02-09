package com.manchuan.tools.activity.movies

import ando.file.core.FileOpener
import ando.file.core.FileUtils
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.addModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.engine.utils.throttleClick
import com.drake.net.utils.runMain
import com.dylanc.longan.doOnClick
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.toast
import com.itxca.spannablex.spannable
import com.jeffmony.downloader.VideoDownloadManager
import com.jeffmony.downloader.listener.IDownloadInfosCallback
import com.jeffmony.downloader.model.VideoTaskItem
import com.jeffmony.downloader.model.VideoTaskState
import com.lxj.androidktx.addOnTabSelectedListener
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.drawable
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityMovieDownloadBinding
import com.manchuan.tools.databinding.ItemM3u8Binding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.errorColor
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.progress


class MovieDownloadActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMovieDownloadBinding.inflate(layoutInflater)
    }
    private val tasksList = arrayListOf<VideoTaskItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "影视下载任务列表"
            setDisplayHomeAsUpEnabled(true)
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
        VideoDownloadManager.getInstance().fetchDownloadItems(infoCallback)
        binding.tab.addOnTabSelectedListener(onTabSelected = { tab ->
            when (tab.text) {
                "全部" -> {
                    binding.recyclerView.models = tasksList
                }

                "下载中" -> {
                    binding.recyclerView.models =
                        tasksList.filter { it.taskState == VideoTaskState.DOWNLOADING }
                }

                "已完成" -> {
                    binding.recyclerView.models =
                        tasksList.filter { it.taskState == VideoTaskState.SUCCESS }
                }
            }
        })
        binding.recyclerView.linear(1).setup {
            addType<VideoTaskItem>(R.layout.item_m3u8)
            onBind {
                val binding = ItemM3u8Binding.bind(itemView)
                val item = getModel<VideoTaskItem>()
                binding.title.text = item.title
                when (item.taskState) {
                    VideoTaskState.DEFAULT -> {

                    }

                    VideoTaskState.PENDING -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.action.icon = drawable(R.drawable.ic_baseline_stop_24)
                        binding.summary.text = resources.getString(R.string.waiting)
                    }

                    VideoTaskState.PREPARE -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.action.icon = drawable(R.drawable.ic_baseline_stop_24)
                        binding.summary.text = resources.getString(R.string.waiting)
                        binding.progress.animateGone()
                    }

                    VideoTaskState.START -> {
                        binding.action.icon = drawable(R.drawable.ic_baseline_stop_24)
                        binding.action.doOnClick {
                            VideoDownloadManager.getInstance().pauseDownloadTask(item)
                            binding.action.icon = drawable(R.drawable.ic_baseline_play_arrow_24)
                        }
                        binding.progress.animateVisible()
                        binding.progress.isIndeterminate = false
                    }

                    VideoTaskState.DOWNLOADING -> {
                        binding.summary.text =
                            "已下载:${item.downloadSizeString} 速度:${item.speedString}"
                        binding.progress.progress(item.percent.toInt())
                    }

                    VideoTaskState.PAUSE -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.action.icon = drawable(R.drawable.ic_baseline_play_arrow_24)
                        binding.action.doOnClick {
                            VideoDownloadManager.getInstance().resumeDownload(item.url)
                            binding.action.icon = drawable(R.drawable.ic_baseline_stop_24)
                        }
                        binding.progress.animateGone()
                        binding.summary.text = String.format(
                            resources.getString(R.string.download_paused_downloaded_size),
                            item.downloadSizeString
                        )
                    }

                    VideoTaskState.SUCCESS -> {
                        //playBtn.visibility = View.VISIBLE
                        binding.action.apply {
                            icon = drawable(R.drawable.ic_baseline_check_24)
                            doOnClick {

                            }
                        }
                        binding.item.throttleClick {

                        }
                        binding.progress.animateGone()
                        binding.summary.text = String.format(
                            resources.getString(R.string.download_completed_total_size),
                            item.downloadSizeString
                        )
                    }

                    VideoTaskState.ERROR -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.progress.animateGone()
                        binding.action.apply {
                            icon = drawable(R.drawable.ic_baseline_error_outline_24)
                            iconTint = ColorStateList.valueOf(errorColor())
                        }
                        binding.summary.text =
                            resources.getString(R.string.download_error).spannable {
                                color(errorColor())
                            }
                    }

                    else -> {
                        //playBtn.visibility = View.INVISIBLE
                        binding.summary.text = resources.getString(R.string.not_downloaded)
                    }
                }
            }
            onClick(R.id.item) {
                val item = getModel<VideoTaskItem>()
                when (item.taskState) {
                    VideoTaskState.PENDING, VideoTaskState.PREPARE -> {

                    }

                    VideoTaskState.START, VideoTaskState.DOWNLOADING -> {
                        VideoDownloadManager.getInstance().pauseDownloadTask(item)
                    }

                    VideoTaskState.PAUSE -> {
                        VideoDownloadManager.getInstance().resumeDownload(item.url)
                    }

                    VideoTaskState.SUCCESS -> {
                        item.filePath
                    }

                    VideoTaskState.ERROR -> {

                    }

                    else -> {

                    }
                }
            }
            onLongClick(R.id.item) {
                val model = getModel<VideoTaskItem>()
                val actions = listOf("删除", "打开文件")
                alertDialog {
                    title = "操作"
                    items(actions) { dialogInterface, i ->
                        when (actions[i]) {
                            "删除" -> {
                                var deleteOrigin = false
                                val deleteRequires = listOf("是", "否")
                                alertDialog {
                                    title = "删除"
                                    message =
                                        "确定要删除这个下载任务吗？如果确定要删除，请选择是否删除源文件。"
                                    singleChoiceItems(
                                        deleteRequires, 1
                                    ) { dialog: DialogInterface, index: Int ->
                                        when (deleteRequires[index]) {
                                            "是" -> deleteOrigin = true
                                            "否" -> deleteOrigin = false
                                        }
                                    }
                                    okButton {
                                        runCatching {
                                            VideoDownloadManager.getInstance()
                                                .deleteVideoTask(model, deleteOrigin)
                                        }.onFailure {
                                            toast("删除失败:${it.message}")
                                        }.onSuccess {
                                            toast("删除成功")
                                            mutable.removeAt(mutable.indexOf(model))
                                            binding.recyclerView.bindingAdapter.notifyItemRemoved(
                                                mutable.indexOf(model)
                                            )
                                        }
                                    }
                                    cancelButton()
                                }.show()
                            }
                        }
                    }
                }.show()
            }
        }
        receiveEvent<VideoTaskItem>("movie") {
            runMain {
                runCatching {
                    binding.recyclerView.bindingAdapter.notifyItemChanged(
                        tasksList.indexOf(it)
                    )
                }
            }
        }
    }

    private val infoCallback: IDownloadInfosCallback = IDownloadInfosCallback { items ->
        items.forEach {
            runMain {
                tasksList.add(it)
                binding.recyclerView.addModels(listOf(it))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoDownloadManager.getInstance().removeDownloadInfosCallback(infoCallback)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}
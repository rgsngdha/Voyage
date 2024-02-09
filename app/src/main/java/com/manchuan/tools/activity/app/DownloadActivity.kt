package com.manchuan.tools.activity.app

import android.os.Bundle
import android.util.Log
import com.arialyy.annotations.Download
import com.arialyy.annotations.DownloadGroup
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.download.DownloadEntity
import com.arialyy.aria.core.task.DownloadGroupTask
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.util.ALog
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.net.utils.scope
import com.dylanc.longan.TAG
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.toast
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityDownloadBinding
import com.manchuan.tools.databinding.ItemDownloadTaskBinding
import com.manchuan.tools.user.verifyRole
import timber.log.Timber


class DownloadActivity : BaseActivity() {

    private val binding by viewBinding(ActivityDownloadBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!isAppDarkMode)
        Aria.download(this).register()
        with(binding) {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                title = "下载管理"
                subtitle = "管理软件下载任务"
                setDisplayHomeAsUpEnabled(true)
            }
        }
        verifyRole(Global.token.value.toString(), success = {
            with(binding) {
                recycler.linear().divider {
                    orientation = DividerOrientation.HORIZONTAL
                    setDivider(16, true)
                }.setup {
                    addType<DownloadEntity>(R.layout.item_download_task)
                    onBind {
                        val model = getModel<DownloadEntity>()
                        val binding = getBinding<ItemDownloadTaskBinding>()
                        with(binding) {
                            title.text = model.fileName
                        }
                    }
                }.models = Aria.download(this@DownloadActivity).taskList
                state.onRefresh {
                    scope {
                        recycler.models = Aria.download(this@DownloadActivity).taskList
                    }
                }.autoRefresh()
            }
        }, failed = {
            finish()
            toast("当前不在测试功能白名单中，无法使用该功能。")
        })
    }

    @Download.onPre
    fun onPre(task: DownloadTask) {
        //mAdapter.updateState(task.entity)
        Timber.tag(TAG).d("%s%s", task.taskName + ", ", task.state)
    }

    @Download.onWait
    fun onWait(task: DownloadTask) {
        //mAdapter.updateState(task.entity)
    }

    @Download.onTaskStart
    fun taskStart(task: DownloadTask) {
        Log.d(TAG, task.taskName + ", " + task.state)
        //mAdapter.updateState(task.entity)
    }

    @Download.onTaskResume
    fun taskResume(task: DownloadTask) {
        Log.d(TAG, task.taskName + ", " + task.state)
        //mAdapter.updateState(task.entity)
    }

    @Download.onTaskStop
    fun taskStop(task: DownloadTask) {
        //mAdapter.updateState(task.entity)
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask) {
        //mAdapter.updateState(task.entity)
        val tasks = Aria.download(this).allNotCompleteTask
        if (tasks != null) {
            ALog.d(TAG, "未完成的任务数：" + tasks.size)
        }
    }

    @Download.onTaskFail
    fun taskFail(task: DownloadTask?) {
        if (task == null || task.entity == null) {
            return
        }
        //mAdapter.updateState(task.entity)
    }

    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        //mAdapter.updateState(task.entity)
    }

    @Download.onTaskRunning
    fun taskRunning(task: DownloadTask) {
        //mAdapter.setProgress(task.entity)
    }


    //////////////////////////////////// 下面为任务组的处理 /////////////////////////////////////////
    @DownloadGroup.onPre
    fun onGroupPre(task: DownloadGroupTask) {
//        mAdapter.updateState(task.entity)
    }

    @DownloadGroup.onTaskStart
    fun groupTaskStart(task: DownloadGroupTask) {
//        mAdapter.updateState(task.entity)
    }

    @DownloadGroup.onWait
    fun groupTaskWait(task: DownloadGroupTask) {
        ALog.d(TAG, String.format("group【%s】wait---", task.taskName))
//        mAdapter.updateState(task.entity)
    }

    @DownloadGroup.onTaskResume
    fun groupTaskResume(task: DownloadGroupTask) {
//        mAdapter.updateState(task.entity)
    }

    @DownloadGroup.onTaskStop
    fun groupTaskStop(task: DownloadGroupTask) {
        ALog.d(TAG, String.format("group【%s】stop", task.taskName))
//        mAdapter.updateState(task.entity)
    }

    @DownloadGroup.onTaskCancel
    fun groupTaskCancel(task: DownloadGroupTask) {
//        mAdapter.updateState(task.entity)
    }

    @DownloadGroup.onTaskFail
    fun groupTaskFail(task: DownloadGroupTask?) {
        if (task != null) {
            ALog.d(TAG, String.format("group【%s】fail", task.taskName))
            //mAdapter.updateState(task.entity)
        }
    }

    @DownloadGroup.onTaskComplete
    fun groupTaskComplete(task: DownloadGroupTask) {
//        mAdapter.updateState(task.entity)
    }

    @DownloadGroup.onTaskRunning
    fun groupTaskRunning(task: DownloadGroupTask) {
        ALog.d(TAG, String.format("group【%s】running", task.taskName))
//        mAdapter.setProgress(task.entity)
    }


}
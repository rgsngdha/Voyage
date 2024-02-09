package com.manchuan.tools.activity.app.log

import ando.file.core.FileUri
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.net.utils.scope
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.internalFileDirPath
import com.dylanc.longan.shareFile
import com.dylanc.longan.startActivity
import com.lxj.androidktx.core.doOnlyOnce
import com.lxj.androidktx.core.toDateString
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityLogListBinding
import com.manchuan.tools.databinding.ItemLogBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.utils.UiUtils
import java.io.File

class LogListActivity : BaseActivity() {

    private val binding by lazy {
        ActivityLogListBinding.inflate(layoutInflater)
    }

    private val fileList = arrayListOf<FileModel>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!UiUtils.isDarkMode())
        supportActionBar?.apply {
            title = "崩溃日志"
            setDisplayHomeAsUpEnabled(true)
        }
        File(internalFileDirPath + File.separator + "tombstones").listFiles()?.forEach {
            fileList.add(FileModel(it.name, it.lastModified().toDateString(), it.absolutePath))
        }
        doOnlyOnce("welcome_log_manage_central", action = {
            alertDialog {
                title = "欢迎"
                message =
                    "欢迎访问崩溃日志管理中心，在这里，你可以将使用本应用过程中所产生的崩溃日志发送给开发者，或将其删除。"
                okButton {

                }
            }.build()
        })
        binding.recyclerView.linear().setup {
            addType<FileModel>(R.layout.item_log)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemLogBinding.bind(itemView)
                val model = getModel<FileModel>(modelPosition)
                binding.title.text = model.name
                binding.summary.text = "创建时间:${model.time}"
            }
            onClick(R.id.item) {
                val model = getModel<FileModel>(modelPosition)
                startActivity<ViewerActivity>("file" to model.path)
            }
            onLongClick(R.id.item) {
                val model = getModel<FileModel>(modelPosition)
                selector(listOf("分享", "删除"), "操作") { dialogInterface, s, i ->
                    when (s) {
                        "分享" -> FileUri.getShareUri(File(model.path))
                            ?.let { it1 -> shareFile(it1) }

                        "删除" -> {
                            runCatching {
                                File(model.path).apply {
                                    delete()
                                }
                                snack("删除成功")
                                fileList.remove(model)
                                binding.recyclerView.mutable.removeAt(modelPosition)
                                binding.recyclerView.bindingAdapter.notifyItemRemoved(modelPosition)
                                binding.refresh.refreshing()
                            }.onFailure {
                                snack("删除失败:${it.message}")
                            }
                        }
                    }
                }
            }
        }
        binding.refresh.onRefresh {
            scope {
                if (fileList.isNotEmpty()) {
                    binding.recyclerView.models = fileList.sortedByDescending { it.time }
                } else {
                    binding.refresh.showEmpty()
                }
            }
        }.autoRefresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    data class FileModel(var name: String, var time: String, var path: String)

}
package com.manchuan.tools.activity.app

import ando.file.core.FileUri
import ando.file.core.FileUtils
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blankj.utilcode.util.AppUtils
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.addModels
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.brv.utils.staggered
import com.drake.engine.utils.dp
import com.drake.net.utils.withMain
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.shareFile
import com.dylanc.longan.startActivity
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itxca.spannablex.spannable
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.lxj.androidktx.core.string
import com.lxj.xpopup.XPopup
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.models.AppItem
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityAppBinding
import com.manchuan.tools.databinding.ItemListAppBinding
import com.manchuan.tools.extensions.checkShizukuPermission
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.utils.RootCmd.CheckRootPathSU
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import rikka.shizuku.Shizuku
import java.io.DataOutputStream
import java.io.File

class AppManagerActivity : BaseActivity() {


    private val allApps = mutableListOf<AppItem>()
    private lateinit var appFlow: Flow<AppItem>
    private val binding by viewBinding(ActivityAppBinding::inflate)

    private val loadingDialog by lazy {
        XPopup.Builder(this).dismissOnBackPressed(false).dismissOnTouchOutside(false)
            .asLoading(string(com.drake.net.R.string.srl_footer_loading))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!isAppDarkMode)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "应用管理"
            setDisplayHomeAsUpEnabled(true)
        }
        with(binding) {
            FastScrollerBuilder(rvApp).useMd2Style().build()
            rvApp.staggered(2).setup {
                setAnimation(AnimationType.ALPHA)
                addType<AppItem>(R.layout.item_list_app)
                onBind {
                    val model = getModel<AppItem>()
                    val binding = getBinding<ItemListAppBinding>()
                    binding.appIcon.load(model.app_icon, isCrossFade = true)
                    binding.appName.text = spannable {
                        model.app_name.text()
                        if (!model.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                            margin(8.dp)
                            newline()
                            "[SPLIT APKS]".span {
                                color(colorPrimary())
                            }
                        }
                    }
                    binding.packageName.text = model.package_name
                }
                onClick(R.id.app) {
                    val model = getModel<AppItem>()
                    startActivity<AppInformationActivity>(
                        "packageName" to model.package_name, "appName" to model.app_name
                    )
                }
                onLongClick(R.id.app) {
                    onItemLongClickListeners(getModel())
                }
            }
        }
        val packageInfos =
            packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        loadingDialog.show()
        appFlow = flow {
            packageInfos.forEach { flags ->
                val appItem = AppItem(
                    flags.applicationInfo.loadIcon(packageManager),
                    flags.applicationInfo.loadLabel(packageManager).toString(),
                    flags.applicationInfo.packageName,
                    if (AppUtils.isAppSystem(flags.packageName)) 1 else 0,
                    flags.applicationInfo
                )
                allApps.add(appItem)
                emit(appItem)
            }
            allApps.sortBy { it.app_name }
        }.onCompletion {
            loge("完成")
            withMain {
                loadingDialog.dismiss()
                binding.rvApp.mutable.sortBy { (it as AppItem).app_name }
                binding.rvApp.models = binding.rvApp.mutable
            }
        }.flowOn(Dispatchers.IO)
        allApps.sortBy { it.app_name }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                appFlow.filter {
                    it.appType == 0
                }.collect {
                    loge(it)
                    binding.rvApp.addModels(listOf(it))
                }
            }
        }
        runCatching {
            Shizuku.requestPermission(1)
        }
        binding.textField.setEndIconOnClickListener {
            if (allApps.isNotEmpty()) {
                binding.rvApp.models = binding.rvApp.mutable.filter {
                    (it as AppItem).app_name.lowercase()
                        .contains(binding.editText.textString.lowercase())
                }
            } else {
                toast("加载完后才能搜索")
            }
        }
    }


    fun run(command: String) {
        if (checkShizukuPermission(0)) {
            val shizuku = Shizuku.newProcess(arrayOf("sh"), null, null).outputStream
            shizuku.write((command).toByteArray())
            shizuku.flush()
            shizuku.close()
        } else {
            try {
                val ps = Runtime.getRuntime().exec("su")
                val writer = DataOutputStream(ps.outputStream)
                writer.writeBytes("$command\nexit\n")
                writer.flush()
                ps.waitFor()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isEnabled(packageName: String?): Boolean {
        var state = false
        try {
            state = this.packageManager.getPackageInfo(packageName!!, 0).applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return state
    }

    /**
     * 设置应用禁用或者解禁
     * @param packageName
     * @param enabled
     */
    private fun setAppState(packageName: String, enabled: Boolean) {
        var command = "pm "
        command += if (enabled) {
            "enable $packageName"
        } else {
            "disable $packageName"
        }
        this.run(command)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_app_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.user_app -> binding.rvApp.models = allApps.filter { it.appType != 1 }

            R.id.system_app -> binding.rvApp.models = allApps.filter { it.appType == 1 }

            R.id.action_settings -> {
                //startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onItemLongClickListeners(position: AppItem) {
        val freeze: String = if (isEnabled(position.package_name)) {
            "冻结"
        } else if (!isEnabled(position.package_name)) {
            "解冻"
        } else {
            "冻结"
        }
        BottomMenu.show(arrayOf("打开", "卸载", freeze, "分享"))
            .setTitle("对 " + position.app_name + " 进行操作")
            .setOnMenuItemClickListener { dialog: BottomMenu?, text: CharSequence, index: Int ->
                when (text.toString()) {
                    "打开" -> AppUtils.launchApp(
                        position.package_name
                    )

                    "卸载" -> if (AppUtils.isAppSystem(position.package_name)) {
                        MaterialAlertDialogBuilder(this@AppManagerActivity).setTitle("Warning")
                            .setMessage("系统App无法卸载").setPositiveButton("确定", null).create()
                            .show()
                    } else {
                        AppUtils.uninstallApp(position.package_name)
                    }

                    "冻结" -> if (CheckRootPathSU() || checkShizukuPermission(0)) {
                        setAppState(position.package_name, false)
                    } else {
                        PopTip.show("无ROOT权限或未激活Shizuku")
                    }

                    "解冻" -> if (CheckRootPathSU() || checkShizukuPermission(0)) {
                        setAppState(position.package_name, true)
                    } else {
                        PopTip.show("无ROOT权限或未激活Shizuku")
                    }

                    "分享" -> {
                        //IntentUtils.
                        runCatching {
                            position.applicationInfo.sourceDir?.let { sourcesDir ->
                                val file = FileUtils.copyFile(
                                    File(sourcesDir),
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                                    position.app_name + ".apk"
                                )
                                PopNotification.show(
                                    "远航工具箱", "安装包已保存到 ${
                                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                                    }"
                                )
                                FileUri.getShareUri(file)?.let { uri ->
                                    shareFile(uri)
                                }
                            }
                        }.onFailure {
                            PopTip.show("提取失败:" + it.message)
                        }
                    }
                }
                false
            }
    }

}
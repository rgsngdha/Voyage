package com.manchuan.tools.activity.app.fragments

import android.animation.LayoutTransition
import android.content.pm.PackageManager
import com.blankj.utilcode.util.AppUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.crazylegend.kotlinextensions.packageutils.whoInstalledMyApp
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.drake.engine.utils.throttleClick
import com.dylanc.longan.arguments
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.google.android.material.chip.Chip
import com.kongzue.dialogx.dialogs.PopTip
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.string
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.AppInformationActivity
import com.manchuan.tools.databinding.FragmentAppInfoBinding
import com.manchuan.tools.databinding.ItemAppCategoryBinding
import com.manchuan.tools.databinding.ItemAppInfoBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.androidString
import com.manchuan.tools.extensions.checkShizukuPermission
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.tryWith
import com.manchuan.tools.utils.RootUtil
import rikka.shizuku.Shizuku
import java.io.DataOutputStream


class AppInfoFragment : EngineFragment<FragmentAppInfoBinding>(R.layout.fragment_app_info) {
    private var AppType: String = ""
    private var versionName = "系统应用"
    private val appType by lazy {
        Chip(context)
    }
    private val appStatus by lazy {
        Chip(context)
    }

    private val packageName by arguments<String>("packageName")

    override fun initView() {
        AppType = if (AppUtils.isAppSystem(packageName)) {
            "系统应用"
        } else {
            "用户应用"
        }
        versionName = tryWith {
            "版本:" + AppUtils.getAppVersionName(packageName)
        }.toString()
        Glide.with(requireActivity()).load(AppUtils.getAppIcon(packageName)).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE).into(
                binding.appIcon
            )
        binding.appHeader.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.appName.text(AppInformationActivity.app_name.toString())
        binding.appPack.text(packageName.toString())
        binding.appVersion.text(versionName)
        binding.appInfoRecycler.linear().setup {
            addType<AppInfoCategory>(R.layout.item_app_category)
            addType<AppInfoItem>(R.layout.item_app_info)
            setAnimation(AnimationType.ALPHA)
            onBind {
                when (itemViewType) {
                    R.layout.item_app_category -> {
                        val model = getModel<AppInfoCategory>()
                        val binding = getBinding<ItemAppCategoryBinding>()
                        binding.category.text = model.name
                    }

                    R.layout.item_app_info -> {
                        val model = getModel<AppInfoItem>()
                        val binding = getBinding<ItemAppInfoBinding>()
                        binding.apply {
                            name.text = model.name
                            summary.text = model.summary
                        }
                    }
                }
            }
        }
        runCatching {
            val applicationInfo = requireActivity().packageManager.getApplicationInfo(
                packageName!!, 0
            )
            val appBanner = requireActivity().packageManager.getApplicationBanner(applicationInfo)
            appBanner?.let {
                binding.bannerImage.animateVisible()
                binding.bannerImage.load(appBanner, isCrossFade = true)
            }
            applicationInfo.apply {
                binding.appInfoRecycler.models = mutableListOf(
                    AppInfoCategory("属性"),
                    AppInfoItem("起始平台", "${androidString(minSdkVersion)} ($minSdkVersion)"),
                    AppInfoItem(
                        "目标平台", "${androidString(targetSdkVersion)} ($targetSdkVersion)"
                    ),
                    AppInfoItem("安装来源", "${
                        requireContext().whoInstalledMyApp(packageName)?.let {
                            requireActivity().packageManager.getApplicationInfo(
                                it, 0
                            ).loadLabel(requireActivity().packageManager).toString()
                        }
                    } (${requireContext().whoInstalledMyApp(packageName)})"),
                    AppInfoItem("用户标识符", uid.toString()),
                    AppInfoCategory("目录"),
                    AppInfoItem("数据目录", dataDir),
                    AppInfoItem("原生库目录", nativeLibraryDir),
                )
            }

            appType.text = AppType
            val status = if (isEnabled(packageName)) {
                string(R.string.enabled)
            } else {
                string(R.string.disabled)
            }
            appStatus.text = status
            appStatus.throttleClick {
                if (RootUtil.isDeviceRooted || checkShizukuPermission(1)) {
                    when (appStatus.textString) {
                        string(R.string.enabled) -> {
                            requireContext().alertDialog {
                                title = "操作提示"
                                message = "确定要禁用 \"${AppUtils.getAppName(packageName)}\" 吗？${
                                    if (AppUtils.isAppSystem(
                                            applicationInfo.packageName
                                        )
                                    ) "该应用是系统应用，请确认禁用该应用之后不会对系统造成任何损害，如：系统无法启动、系统无响应。" else ""
                                }"
                                okButton {
                                    packageName?.let { packageName ->
                                        setAppState(
                                            packageName, false
                                        )
                                        PopTip.show("已禁用")
                                        postDelayed({
                                            refreshStatus()
                                        }, 300)
                                    }
                                }
                            }.show()
                        }

                        string(R.string.disabled) -> {
                            packageName?.let { packageName ->
                                setAppState(
                                    packageName, enabled = true
                                )
                                PopTip.show("已启用")
                                postDelayed({
                                    refreshStatus()
                                }, 300)
                            }
                        }
                    }
                } else {
                    toast("无权限进行冻结/解冻操作")
                }
            }
            binding.chipGroup.addView(appType)
            binding.chipGroup.addView(appStatus)
        }
    }

    data class AppInfoCategory(var name: String)
    data class AppInfoItem(var name: String, var summary: String)

    override fun initData() {

    }

    private fun refreshStatus() {
        val status = if (isEnabled(packageName)) {
            "启用"
        } else {
            "停用"
        }
        appStatus.text = status
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

    private fun setAppState(packageName: String, enabled: Boolean) {
        var command = "pm "
        command += if (enabled) {
            "enable $packageName"
        } else {
            "disable $packageName"
        }
        this.run(command)
    }

    fun isEnabled(packageName: String?): Boolean {
        var state = false
        try {
            state = requireActivity().packageManager.getPackageInfo(
                packageName!!, 0
            ).applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return state
    }

}
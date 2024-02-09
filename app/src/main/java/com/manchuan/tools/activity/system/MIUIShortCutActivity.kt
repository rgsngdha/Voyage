package com.manchuan.tools.activity.system

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.grantReadUriPermission
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.toast
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityMiuishortCutBinding
import com.manchuan.tools.model.MiuiShortcutModel
import com.manchuan.tools.utils.RootUtil
import com.manchuan.tools.utils.UiUtils
import com.manchuan.tools.utils.XiaomiUtilities
import com.topjohnwu.superuser.Shell

class MIUIShortCutActivity : BaseActivity() {
    private val binding by lazy {
        ActivityMiuishortCutBinding.inflate(layoutInflater)
    }
    private val SETTINGS_PACKAGE_NAME = "com.android.settings"
    private val SECURITY_CENTER_PACKAGE_NAME = "com.miui.securitycenter"
    private val MIUI_SYSTEMUI_PLUGIN = "miui.systemui.plugin"
    private val CIT_ENGINE = "com.miui.cit"
    private val PHONE_DIALER = "com.android.phone"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "MIUI快捷方式"
            setDisplayHomeAsUpEnabled(true)
        }
        if (!XiaomiUtilities.isMIUI) {
            BaseAlertDialogBuilder(this).setTitle("警告")
                .setMessage("当前设备类型不是小米，绝大部分功能无法使用")
                .setPositiveButton("确定", null).show()
        }
        binding.recyclerView.grid(2).divider {
            orientation = DividerOrientation.GRID
            includeVisible = true
            setDivider(12, true)
        }.setup {
            addType<MiuiShortcutModel>(R.layout.item_shortcuts)
            onClick(R.id.shortcut) {
                val model = getModel<MiuiShortcutModel>()
                runCatching {
                    if (RootUtil.isDeviceRooted.not()) {
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_LAUNCHER)
                        val cn = ComponentName(
                            model.packageName, model.className
                        )
                        intent.component = cn
                        intent.grantReadUriPermission()
                        startActivity(intent)
                    } else {
                        Shell.cmd("am start -n ${model.packageName}/${model.className}").exec()
                    }
                }.onFailure {
                    toast("系统不支持")
                }
            }
            onLongClick(R.id.shortcut) {
                runCatching {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    val cn = ComponentName(
                        getModel<MiuiShortcutModel>().packageName,
                        getModel<MiuiShortcutModel>().className
                    )
                    intent.component = cn
                    val shortcut = ShortcutInfoCompat.Builder(context, modelPosition.toString())
                        .setShortLabel(getModel<MiuiShortcutModel>().name)
                        .setLongLabel(getModel<MiuiShortcutModel>().name).setIntent(intent).build()
                    ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
                }.onFailure {
                    toast("快捷方式添加失败")
                }.onSuccess {
                    toast("快捷方式添加成功,在桌面长按本应用图标即可显示")
                }
            }
        }.models = getShortcutsData()
    }

    private fun getShortcutsData(): List<MiuiShortcutModel> {
        return listOf(
            MiuiShortcutModel(
                "通知设置",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$ConfigureNotificationSettingsActivity"
            ), MiuiShortcutModel(
                "CIT 工程测试", CIT_ENGINE, "$CIT_ENGINE.home.HomeActivity"
            ), MiuiShortcutModel(
                "设置无线装置频道模式", PHONE_DIALER, "$PHONE_DIALER.settings.MiuiBandMode"
            ), MiuiShortcutModel(
                "Button Navigation Settings",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$ButtonNavigationSettingsActivity"
            ), MiuiShortcutModel(
                "媒体",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$MediaControlsSettingsActivity"
            ), MiuiShortcutModel(
                "智能多网络加速",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.wifi.linkturbo.WifiLinkTurboSettings"
            ), MiuiShortcutModel(
                "WLAN工具",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$WifiInfoActivity"
            ), MiuiShortcutModel(
                "关于手机",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$MyDeviceInfoActivity"
            ), MiuiShortcutModel(
                "Android Beam",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$AndroidBeamSettingsActivity"
            ), MiuiShortcutModel(
                "电量与性能",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$PowerUsageSummaryActivity"
            ), MiuiShortcutModel(
                "动态画面补偿",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.display.ScreenEnhanceEngineMemcActivity"
            ), MiuiShortcutModel(
                "视频画质增强",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.display.ScreenEnhanceEngineS2hActivity"
            ), MiuiShortcutModel(
                "AI智能场景优化",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.display.ScreenEnhanceEngineAiActivity"
            ), MiuiShortcutModel(
                "超分辨率增强",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.display.ScreenEnhanceEngineSrActivity"
            ), MiuiShortcutModel(
                "AI大师画质引擎",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.display.ScreenEnhanceEngineActivity"
            ), MiuiShortcutModel(
                "气泡通知",
                SECURITY_CENTER_PACKAGE_NAME,
                "com.miui.bubbles.settings.BubbleSettingsActivity"
            ), MiuiShortcutModel(
                "原生应用管理",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$ManageApplicationsActivity"
            ), MiuiShortcutModel(
                "快充加速",
                SECURITY_CENTER_PACKAGE_NAME,
                "com.miui.powercenter.fastCharge.FastChargeActivity"
            ), MiuiShortcutModel(
                "NFC设置", SETTINGS_PACKAGE_NAME, "$SETTINGS_PACKAGE_NAME.Settings\$MiuiNfcActivity"
            ), MiuiShortcutModel(
                "极暗",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$ReduceBrightColorsSettingsActivity"
            ), MiuiShortcutModel(
                "原生隐私",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.Settings\$PrivacyDashboardActivity"
            ), MiuiShortcutModel(
                "壁纸来源",
                SETTINGS_PACKAGE_NAME,
                "$SETTINGS_PACKAGE_NAME.wallpaper.WallpaperSuggestionActivity"
            ), MiuiShortcutModel(
                "游戏场景光效",
                SECURITY_CENTER_PACKAGE_NAME,
                "com.miui.gamebooster.shoulderkey.ShoulderKeyLightEffectActivity"
            ), MiuiShortcutModel(
                "虚拟身份ID授权管理",
                SECURITY_CENTER_PACKAGE_NAME,
                "com.miui.permcenter.permissions.OAIDAppsActivity"
            ), MiuiShortcutModel(
                "无线反向充电",
                SECURITY_CENTER_PACKAGE_NAME,
                "com.miui.powercenter.wirelesscharge.WirelessReverseActivity"
            ), MiuiShortcutModel(
                "快捷回复",
                SECURITY_CENTER_PACKAGE_NAME,
                "com.miui.gamebooster.ui.QuickReplySettingsActivity"
            ), MiuiShortcutModel(
                "智能充电保护",
                SECURITY_CENTER_PACKAGE_NAME,
                "com.miui.powercenter.nightcharge.NightChargeSettings"
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}
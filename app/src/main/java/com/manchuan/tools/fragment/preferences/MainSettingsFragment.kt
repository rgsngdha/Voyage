package com.manchuan.tools.fragment.preferences

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.blankj.utilcode.util.AppUtils
import com.drake.serialize.serialize.serialize
import com.dylanc.longan.relaunchApp
import com.google.android.material.color.DynamicColors
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.lxj.androidktx.core.putBoolean
import com.lxj.androidktx.core.putString
import com.lxj.androidktx.core.sp
import com.lxj.androidktx.core.string
import com.manchuan.tools.R
import com.manchuan.tools.database.DEFAULT_LAUNCH
import com.manchuan.tools.database.Global
import com.manchuan.tools.extensions.checkShizukuPermission
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.utils.SettingsLoader
import rikka.material.preference.MaterialSwitchPreference
import rikka.preference.SimpleMenuPreference
import rikka.shizuku.Shizuku


class MainSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        super.onCreate(savedInstanceState)
    }


    private fun onShizukuRequestPermissionsResult(requestCode: Int, grantResult: Int) {
        val granted = grantResult == PackageManager.PERMISSION_GRANTED
        // Do stuff based on the result and the request code
        if (granted) {
            shizukuPreference.summary = "已授权"
        }
    }


    private val shizukuResult: (Int, Int) -> Unit = this::onShizukuRequestPermissionsResult

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuResult)
    }

    private lateinit var shizukuPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_perference, rootKey)
        val themePreference = findPreference<Preference>("theme_style") as ListPreference?
        val glidePreference = findPreference<Preference>("glide_cache") as ListPreference?
        val userInputPreference = findPreference<MaterialSwitchPreference>("userInput")
        val dialogPreference = findPreference<MaterialSwitchPreference>("dialogBlur")
        val defaultLaunch = findPreference<SimpleMenuPreference>("default_launch")
        shizukuPreference = findPreference("shizuku")!!
        val dynamicColors = findPreference<MaterialSwitchPreference>("dynamicColors")
        val smallSpeakCache = findPreference<MaterialSwitchPreference>("small_speak_cache")
        themePreference!!.summary = themePreference.entry
        glidePreference!!.summary = glidePreference.entry
        dialogPreference?.setOnPreferenceChangeListener { preference, newValue ->
            Global.isEnabledDialogBlur = newValue as Boolean
            true
        }
        shizukuPreference.summary = when (checkShizukuPermission(0)) {
            true -> "已授权"
            false -> "未授权"
        }
        Shizuku.addRequestPermissionResultListener(shizukuResult);
        userInputPreference?.setOnPreferenceChangeListener { preference, newValue ->
            Global.isCanUserInput.value = newValue as Boolean
            true
        }

        //默认启动
        with(defaultLaunch!!) {
            summary = defaultLaunch.entry
            onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    val indexOfValue = findIndexOfValue(newValue.toString())
                    summary = if (indexOfValue >= 0) entries[indexOfValue] else null
                    when (newValue) {
                        "main" -> sp().putString(DEFAULT_LAUNCH, "main")
                        "movie" -> sp().putString(DEFAULT_LAUNCH, "movie")
                        "media_player" -> sp().putString(DEFAULT_LAUNCH, "media_player")
                    }
                    context.apply {
                        snack(message = "这项更改将会在应用下一次启动后生效。",
                            actionText = string(android.R.string.ok),
                            action = {
                                relaunchApp(true)
                            })
                    }
                    true
                }
        }

        //Shizuku 服务
        shizukuPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            runCatching {
                Shizuku.requestPermission(0)
            }
            false
        }


        smallSpeakCache?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                Global.smallSpeakCache.value = newValue as Boolean
                context?.apply {
                    snack(message = "这项更改将会在应用下一次启动后生效。",
                        actionText = string(android.R.string.ok),
                        action = {
                            relaunchApp(true)
                        })
                }
                true
            }
        dynamicColors?.setDefaultValue(DynamicColors.isDynamicColorAvailable())
        dynamicColors?.isChecked = Global.isEnabledDynamicColors.value == true
        dynamicColors?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                serialize("isEnabledDynamicColors" to newValue)
                AppUtils.relaunchApp(true)
                true
            }

        //主题
        themePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                val indexOfValue = themePreference.findIndexOfValue(newValue.toString())
                themePreference.summary =
                    if (indexOfValue >= 0) themePreference.entries[indexOfValue] else null
                //restartApp();
                SettingsLoader.nightMode?.let { AppCompatDelegate.setDefaultNightMode(it) }
                context?.apply {
                    snack(message = "这项更改将会在应用下一次启动后生效。",
                        actionText = string(android.R.string.ok),
                        action = {
                            relaunchApp(true)
                        })
                }
                true
            }
        glidePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                val indexOfValue = glidePreference.findIndexOfValue(newValue.toString())
                glidePreference.summary =
                    if (indexOfValue >= 0) glidePreference.entries[indexOfValue] else null
                when (newValue) {
                    "enabled" -> sp().putBoolean("glide_cache", true)
                    "disabled" -> sp().putBoolean("glide_cache", false)
                }
                true
            }
    }
}
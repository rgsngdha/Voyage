package com.manchuan.tools.activity.movies.settings

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.drake.statusbar.immersive
import com.dylanc.longan.toast
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityMoviesSettingsBinding
import com.manchuan.tools.extensions.loadData
import com.manchuan.tools.extensions.saveData
import com.manchuan.tools.utils.UiUtils
import rikka.material.preference.MaterialSwitchPreference
import rikka.preference.SimpleMenuPreference

private const val TITLE_TAG = "settingsActivityTitle"

class MoviesSettingsActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private val binding by lazy {
        ActivityMoviesSettingsBinding.inflate(layoutInflater)
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme.applyStyle(
            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true
        )
    }

    private var uiSettings = UserInterfaceSettings()

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immersive(binding.toolbar, !UiUtils.isDarkMode())
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_movies_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference,
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader, pref.fragment!!
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction().replace(R.id.settings, fragment)
            .addToBackStack(null).commit()
        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.movies_header_preferences, rootKey)
        }
    }

    class VideoPlayerSettingsFragment : PreferenceFragmentCompat() {
        private val player = "player_settings"
        private var settings = PlayerSettings()
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.video_player_preferences, rootKey)
            settings = loadData(player, toast = false) ?: PlayerSettings().apply {
                saveData(
                    player, this
                )
            }
            val hdrPreference = findPreference<SimpleMenuPreference>("video_hdr")
            val colorModePreference = findPreference<SimpleMenuPreference>("video_color_mode")
            val picturePreference = findPreference<MaterialSwitchPreference>("picture_in_picture")
            val videoInfoPreference = findPreference<MaterialSwitchPreference>("video_info")
            val autoSkipPreference = findPreference<MaterialSwitchPreference>("auto_skip")
            val autoFullscreenPreference =
                findPreference<MaterialSwitchPreference>("auto_fullscreen")
            val skipTimePreference = findPreference<EditTextPreference>("skip_time")
            skipTimePreference?.isVisible = autoSkipPreference?.isChecked ?: false
            hdrPreference!!.summary = hdrPreference.entry
            colorModePreference!!.summary = colorModePreference.entry
            skipTimePreference?.setDefaultValue(settings.autoSkipTime)
            skipTimePreference?.summary =
                "播放视频时默认跳过的开头时间 (单位:秒)\n当前设定值:${settings.autoSkipTime}秒"
            skipTimePreference?.setDefaultValue(settings.autoSkipTime)
            autoFullscreenPreference?.isChecked = settings.autoFullscreen
            autoSkipPreference?.isChecked = settings.autoSkip
            picturePreference?.isChecked = settings.pip
            picturePreference?.isEnabled =
                context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                    ?: false
            videoInfoPreference?.isChecked = settings.videoInfo
            hdrPreference.setOnPreferenceChangeListener { preference, newValue ->
                val indexOfValue = hdrPreference.findIndexOfValue(newValue.toString())
                hdrPreference.summary =
                    if (indexOfValue >= 0) hdrPreference.entries[indexOfValue] else null
                when (newValue.toString()) {
                    "auto" -> {
                        Global.isEnabledVideoHDR = false
                    }

                    "always" -> {
                        Global.isEnabledVideoHDR = true
                    }
                }
                true
            }
            colorModePreference.setOnPreferenceChangeListener { preference, newValue ->
                val indexOfValue = colorModePreference.findIndexOfValue(newValue.toString())
                colorModePreference.summary =
                    if (indexOfValue >= 0) colorModePreference.entries[indexOfValue] else null
                when (newValue.toString()) {
                    "hdr" -> {
                        Global.videoColorMode = ActivityInfo.COLOR_MODE_HDR
                    }

                    "wide_color" -> {
                        if (requireActivity().resources.configuration.isScreenWideColorGamut) {
                            Global.videoColorMode = ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT
                        } else {
                            colorModePreference.setValueIndex(0)
                            colorModePreference.summary = "HDR"
                            toast("您的设备或屏幕不支持广色域颜色模式，无法切换至广色域颜色模式")
                        }
                    }
                }
                true
            }
            picturePreference?.setOnPreferenceChangeListener { preference, newValue ->
                settings.pip = newValue as Boolean
                saveData(player, settings)
                true
            }
            videoInfoPreference?.setOnPreferenceChangeListener { preference, newValue ->
                settings.videoInfo = newValue as Boolean
                saveData(player, settings)
                true
            }
            autoFullscreenPreference?.setOnPreferenceChangeListener { preference, newValue ->
                settings.autoFullscreen = newValue as Boolean
                saveData(player, settings)
                true
            }
            autoSkipPreference?.setOnPreferenceChangeListener { preference, newValue ->
                skipTimePreference?.isVisible = newValue as Boolean
                settings.autoSkip = newValue
                saveData(player, settings)
                true
            }
            skipTimePreference?.setOnPreferenceChangeListener { preference, newValue ->
                skipTimePreference.summary =
                    "播放视频时默认跳过的开头时间 (单位:秒)\n当前设定值:${newValue}秒"
                runCatching {
                    settings.autoSkipTime = newValue as Int
                    saveData(player, settings)
                }
                true
            }
        }

    }

    class SyncFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey)
        }
    }
}
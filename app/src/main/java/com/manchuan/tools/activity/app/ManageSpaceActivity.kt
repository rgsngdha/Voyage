package com.manchuan.tools.activity.app

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.blankj.utilcode.util.FileUtils
import com.drake.statusbar.immersive
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityManageSpaceBinding
import com.manchuan.tools.extensions.formatSize
import java.io.File
import java.io.IOException

class ManageSpaceActivity : BaseActivity() {

    private lateinit var binding: ActivityManageSpaceBinding

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme.applyStyle(
            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true
        )
    }

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersive(binding.toolbar)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "管理空间"
        supportFragmentManager.beginTransaction().replace(R.id.layout, SettingsFragment()).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(p1: Preference, p2: Any): Boolean {
            return false
        }

        override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {}

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.storage_preference)
            val allClearPreference = findPreference<Preference>("all_clear")
            val imageCachePreference = findPreference<Preference>("clear_image_cache")
            val exoCachePreference = findPreference<Preference>("clear_video_cache")
            val dataCachePreference = findPreference<Preference>("clear_data")
            val logCachePreference = findPreference<Preference>("clear_log")
            dataCachePreference?.summary =
                "已占大小:" + formatSize(requireContext().dataDir.absolutePath)
            logCachePreference?.summary =
                "已占大小:" + formatSize(requireContext().filesDir.absolutePath + File.separator + "tombstones")
            exoCachePreference?.summary =
                "已占大小:" + formatSize(requireContext().cacheDir.absolutePath + File.separator + "exo")
            imageCachePreference?.summary =
                "已占大小:" + formatSize(requireContext().cacheDir.absolutePath + File.separator + "image_manager_disk_cache")
            allClearPreference?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { p1: Preference? ->
                    BaseAlertDialogBuilder(requireContext()).setTitle("警告")
                        .setMessage("此操作不可撤销，这会清除软件的所有数据，且所有权限需要重新授予！")
                        .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                            ManageSpaceActivity().clearAppUserData(requireContext().packageName)
                        }
                        .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, i: Int ->

                        }.create().show()
                    false
                }
        }
    }

    fun clearAppUserData(packageName: String): Process? {
        return execRuntimeProcess("pm clear $packageName")
    }

    private fun execRuntimeProcess(commond: String?): Process? {
        var p: Process? = null
        try {
            p = Runtime.getRuntime().exec(commond)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return p
    }


}
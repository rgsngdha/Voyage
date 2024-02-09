package com.manchuan.tools.fragment.preferences

import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.cache.clearImageAllCache
import com.manchuan.tools.cache.getGlideCacheSize
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.formatSize
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.toast
import com.manchuan.tools.settings.SettingsActivity
import java.io.File

class StorageFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.storage_preference, rootKey)
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
        imageCachePreference?.summary = "已占大小:" + requireContext().getGlideCacheSize()
        allClearPreference?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { p1: Preference? ->
                BaseAlertDialogBuilder(requireContext()).setTitle("警告")
                    .setMessage("此操作不可撤销，这会清除软件的所有数据，且所有权限需要重新授予！")
                    .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                        context?.packageName?.let { SettingsActivity().clearAppUserData(it) }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, i: Int ->

                    }.create().show()
                false
            }
        imageCachePreference?.setOnPreferenceClickListener {
            requireContext().alertDialog {
                title = "提示"
                message = "您确定要清除图片缓存吗？"
                okButton {
                    requireContext().clearImageAllCache()
                    toast("清除完成")
                    imageCachePreference.summary =
                        "已占大小:" + requireContext().getGlideCacheSize()
                }
                cancelButton()
            }.build()
            false
        }
    }
}
package com.manchuan.tools.settings

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.activity.addCallback
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivitySettingsBinding
import com.manchuan.tools.fragment.preferences.MainSettingsFragment
import com.manchuan.tools.utils.KeepShell


private const val TITLE_TAG = "设置"

class SettingsActivity : BaseActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val settingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme.applyStyle(
            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(settingsBinding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.list_container, MainSettingsFragment()).commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        setSupportActionBar(settingsBinding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        onBackPressedDispatcher.addCallback {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
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

    @SuppressLint("PrivateResource")
    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference,
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = pref.fragment?.let {
            supportFragmentManager.fragmentFactory.instantiate(
                classLoader, it
            ).apply {
                arguments = args
                setTargetFragment(caller, 0)
            }
        }
        // Replace the existing Fragment with the new Fragment
        if (fragment != null) {
            supportFragmentManager.beginTransaction().setCustomAnimations(
                com.google.android.material.R.anim.m3_motion_fade_enter,
                com.google.android.material.R.anim.m3_motion_fade_exit
            ).replace(android.R.id.list_container, fragment).addToBackStack("storage").commit()
        }
        supportActionBar?.title = pref.title
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishAfterTransition()
        }
        return super.onOptionsItemSelected(item)
    }

    fun clearAppUserData(packageName: String) {
        KeepShell(false).doCmdSync("pm clear $packageName")
    }
}
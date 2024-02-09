package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.Dimension
import androidx.annotation.Dimension.Companion.DP
import androidx.annotation.Dimension.Companion.SP

object SystemUiUtil {
    const val SCRIM = 0x55000000
    const val SCRIM_DARK_DIALOG = -0xf3f3f2
    const val SCRIM_LIGHT_DIALOG = -0x99999a
    fun layoutEdgeToEdge(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            val flags =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            val decorView = window.decorView
            decorView.systemUiVisibility = decorView.systemUiVisibility or flags
        }
    }

    fun setLightNavigationBar(view: View, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController!!.setSystemBarsAppearance(
                if (isLight) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var flags = view.systemUiVisibility
            flags = if (isLight) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            view.systemUiVisibility = flags
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun setLightStatusBar(view: View, isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController!!.setSystemBarsAppearance(
                if (isLight) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = if (isLight) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            view.systemUiVisibility = flags
        }
    }

    fun isDarkModeActive(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    fun isNavigationModeGesture(context: Context): Boolean {
        val NAV_GESTURE = 2
        val resources = context.resources
        @SuppressLint("DiscouragedApi") val resourceId = resources.getIdentifier(
            "config_navBarInteractionMode", "integer", "android"
        )
        val mode = if (resourceId > 0) resources.getInteger(resourceId) else 0
        return mode == NAV_GESTURE
    }

    fun isOrientationPortrait(context: Context): Boolean {
        val orientation = context.resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }

    // Unit conversions
    @JvmStatic
    fun dpToPx(context: Context, @Dimension(unit = DP) dp: Float): Int {
        val r = context.resources
        return Math.round(dp * r.displayMetrics.density)
    }

    fun spToPx(context: Context, @Dimension(unit = SP) sp: Float): Int {
        val r = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, r.displayMetrics).toInt()
    }

    // Display width
    @JvmStatic
    fun getDisplayWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    // Display height
    @JvmStatic
    fun getDisplayHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }
}
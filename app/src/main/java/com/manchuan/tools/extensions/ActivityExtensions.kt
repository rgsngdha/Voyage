package com.manchuan.tools.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebSettings
import androidx.annotation.DimenRes
import androidx.annotation.RequiresApi
import com.dylanc.longan.context
import com.dylanc.longan.topActivity
import com.lxj.androidktx.core.toBundle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json


inline fun <reified T> Activity.startActivity(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null,
) {
    val intent = Intent(this, T::class.java).apply {
        if (flag != -1) {
            this.addFlags(flag)
        }
        if (bundle != null) putExtras(bundle.toBundle())
    }
    startActivity(intent)
}

inline fun <reified T> Context.startActivity(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null,
) {
    val intent = Intent(this, T::class.java).apply {
        if (flag != -1) {
            this.addFlags(flag)
        }
        if (bundle != null) putExtras(bundle.toBundle())
    }
    startActivity(intent)
}

inline fun <reified T> startActivity(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null,
) {
    val intent = Intent(topActivity.context, T::class.java).apply {
        if (flag != -1) {
            this.addFlags(flag)
        }
        if (bundle != null) putExtras(bundle.toBundle())
    }
    topActivity.startActivity(intent)
}

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
    explicitNulls = true
    ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
    coerceInputValues = true // 如果JSON字段是Null则使用默认值
}

@RequiresApi(Build.VERSION_CODES.P)
fun Activity.neverCutOut() {
    val lp = this.window.attributes
    lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
    this.window.attributes = lp
}

inline fun <reified T : Any> Intent.extra(key: String, default: T? = null) = lazy {
    val value = extras?.get(key)
    if (value is T) value else default
}

fun Context.userAgent(): String = run {
    val systemUserAgent = System.getProperty("http.agent")
    return WebSettings.getDefaultUserAgent(this) + "__" + systemUserAgent
}

inline fun <reified T : Any> Activity.extraNotNull(key: String, default: T? = null) = lazy {
    val value = intent?.extras?.get(key)
    requireNotNull(if (value is T) value else default) { key }
}

fun Activity.dip(@DimenRes id: Int): Int {
    return resources.getDimensionPixelSize(id)
}

inline val Activity.rootView: View
    get() = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
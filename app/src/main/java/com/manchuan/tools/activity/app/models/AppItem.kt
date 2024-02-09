package com.manchuan.tools.activity.app.models

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

data class AppItem(
    var app_icon: Drawable,
    var app_name: String,
    var package_name: String,
    var appType: Int,
    var applicationInfo: ApplicationInfo
) : Comparable<AppItem> {
    override fun compareTo(other: AppItem): Int {
        return app_name.compareTo(other.app_name)
    }
}
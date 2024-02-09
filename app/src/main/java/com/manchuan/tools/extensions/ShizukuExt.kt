package com.manchuan.tools.extensions

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku


fun checkShizukuPermission(code: Int): Boolean {
    runCatching {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return false
        }
        return if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
            true
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
            false
        } else {
            // Request the permission
            false
        }
    }.onFailure {
        return false
    }
    return false
}
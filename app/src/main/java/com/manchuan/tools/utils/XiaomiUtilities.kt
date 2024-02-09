package com.manchuan.tools.utils

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.text.TextUtils
import com.manchuan.tools.utils.Utility.getAppContext
import com.manchuan.tools.utils.Utility.getSystemProperty
import timber.log.Timber

class XiaomiUtilities {
    private val isMiuiStableBuild: Boolean
        get() = if (clazz == null) false else try {
            val stableField = clazz!!.getField("IS_STABLE_VERSION")
            val isStable = stableField[null] as Boolean
            isStable
        } catch (e: NoSuchFieldException) {
            false
        } catch (e: ClassCastException) {
            false
        } catch (e: IllegalAccessException) {
            false
        }
    private val isMiuiAlphaBuild: Boolean
        get() = if (clazz == null) false else try {
            val alphaField = clazz!!.getField("IS_ALPHA_BUILD")
            val isAlpha = alphaField[null] as Boolean
            isAlpha
        } catch (e: NoSuchFieldException) {
            false
        } catch (e: ClassCastException) {
            false
        } catch (e: IllegalAccessException) {
            false
        }
    private val isMiuiDevelopBuild: Boolean
        get() = !isMiuiStableBuild && !isMiuiAlphaBuild

    companion object {
        const val TAG = "XiaomiUtilities"

        // custom permissions
        const val OP_WIFI_CHANGE = 10001
        const val OP_BLUETOOTH_CHANGE = 10002
        const val OP_DATA_CONNECT_CHANGE = 10003
        const val OP_SEND_MMS = 10004
        const val OP_READ_MMS = 10005
        const val OP_WRITE_MMS = 10006
        const val OP_BOOT_COMPLETED = 10007
        const val OP_AUTO_START = 10008
        const val OP_NFC_CHANGE = 10009
        const val OP_DELETE_SMS = 10010
        const val OP_DELETE_MMS = 10011
        const val OP_DELETE_CONTACTS = 10012
        const val OP_DELETE_CALL_LOG = 10013
        const val OP_EXACT_ALARM = 10014
        const val OP_ACCESS_XIAOMI_ACCOUNT = 10015
        const val OP_NFC = 10016
        const val OP_INSTALL_SHORTCUT = 10017
        const val OP_READ_NOTIFICATION_SMS = 10018
        const val OP_GET_TASKS = 10019
        const val OP_SHOW_WHEN_LOCKED = 10020
        const val OP_BACKGROUND_START_ACTIVITY = 10021
        const val OP_GET_INSTALLED_APPS = 10022
        const val OP_SERVICE_FOREGROUND = 10023
        const val OP_GET_ANONYMOUS_ID = 10024
        const val OP_GET_UDEVICE_ID = 10025
        const val OP_SHOW_DEAMON_NOTIFICATION = 10026
        const val OP_BACKGROUND_LOCATION = 10027
        const val OP_READ_SMS_REAL = 10028
        const val OP_READ_CONTACTS_REAL = 10029
        const val OP_READ_CALENDAR_REAL = 10030
        const val OP_READ_CALL_LOG_REAL = 10031
        const val OP_READ_PHONE_STATE_REAL = 10032
        const val OP_ACCESS_GALLERY = 10034
        const val OP_ACCESS_SOCIALITY = 10035
        val isMIUI: Boolean
            get() = !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))

        @SuppressLint("TimberArgCount")
        fun isCustomPermissionGranted(permission: Int): Boolean {
            try {
                val mgr = getAppContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val m = AppOpsManager::class.java.getMethod(
                    "checkOpNoThrow",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                val result =
                    m.invoke(mgr, permission, Process.myUid(), getAppContext().packageName) as Int
                return result == AppOpsManager.MODE_ALLOWED
            } catch (x: Exception) {
                Timber.tag(TAG).d(x, "isCustomPermissionGranted: %s")
            }
            return true
        }

        val mIUIMajorVersion: Int
            get() {
                val prop = getSystemProperty("ro.miui.ui.version.name")
                if (prop != null) {
                    try {
                        return prop.replace("V", "").toInt()
                    } catch (ignore: NumberFormatException) {
                    }
                }
                return -1
            }
        val permissionManagerIntent: Intent
            get() {
                val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                intent.putExtra("extra_package_uid", Process.myUid())
                intent.putExtra("extra_pkgname", getAppContext().packageName)
                return intent
            }
        private var clazz: Class<*>? = null

        init {
            clazz = try {
                Class.forName("miui.os.Build")
            } catch (e: ClassNotFoundException) {
                null
            }
        }
    }
}
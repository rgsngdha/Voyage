package com.manchuan.tools.interceptor

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import com.hjq.permissions.IPermissionInterceptor
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.OnPermissionPageCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.PermissionFragment
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import com.lxj.androidktx.core.string
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.database.PermissionNameConvert.getPermissionString
import com.manchuan.tools.database.PermissionNameConvert.listToString
import com.manchuan.tools.database.PermissionNameConvert.permissionsToNames
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.okButton


class PermissionInterceptor : IPermissionInterceptor {
    /** 权限申请标记  */
    private var mRequestFlag = false

    /** 权限申请说明 Popup  */
    private var mPermissionPopup: PopupWindow? = null
    override fun launchPermissionRequest(
        activity: Activity,
        allPermissions: List<String>,
        callback: OnPermissionCallback?,
    ) {
        mRequestFlag = true
        val deniedPermissions = XXPermissions.getDenied(activity, allPermissions)
        val message = activity.getString(
            R.string.common_permission_message, getPermissionString(activity, deniedPermissions)
        )
        val decorView = activity.window.decorView as ViewGroup
        val activityOrientation = activity.resources.configuration.orientation
        var showPopupWindow = activityOrientation == Configuration.ORIENTATION_PORTRAIT
        for (permission in allPermissions) {
            if (!XXPermissions.isSpecial(permission)) {
                continue
            }
            if (XXPermissions.isGranted(activity, permission)) {
                continue
            }
            // 如果申请的权限带有特殊权限，并且还没有授予的话
            // 就不用 PopupWindow 对话框来显示，而是用 Dialog 来显示
            showPopupWindow = false
            break
        }
        if (showPopupWindow) {
            PermissionFragment.launch(activity, ArrayList(allPermissions), this, callback)
            // 延迟 300 毫秒是为了避免出现 PopupWindow 显示然后立马消失的情况
            // 因为框架没有办法在还没有申请权限的情况下，去判断权限是否永久拒绝了，必须要在发起权限申请之后
            // 所以只能通过延迟显示 PopupWindow 来做这件事，如果 300 毫秒内权限申请没有结束，证明本次申请的权限没有永久拒绝
            HANDLER.postDelayed({
                if (!mRequestFlag) {
                    return@postDelayed
                }
                if (activity.isFinishing || activity.isDestroyed) {
                    return@postDelayed
                }
                showPopupWindow(activity, decorView, message)
            }, 300)
        } else {
            // 注意：这里的 Dialog 只是示例，没有用 DialogFragment 来处理 Dialog 生命周期
            BaseAlertDialogBuilder(activity).setTitle(R.string.common_permission_description)
                .setMessage(message).setCancelable(false)
                .setPositiveButton(R.string.common_permission_granted) { dialog, which ->
                    dialog.dismiss()
                    PermissionFragment.launch(
                        activity, ArrayList(allPermissions), this@PermissionInterceptor, callback
                    )
                }.setNegativeButton(R.string.common_permission_denied) { dialog, which ->
                    dialog.dismiss()
                    if (callback == null) {
                        return@setNegativeButton
                    }
                    callback.onDenied(deniedPermissions, false)
                }.show()
        }
    }

    override fun grantedPermissionRequest(
        activity: Activity, allPermissions: List<String>,
        grantedPermissions: List<String>, allGranted: Boolean,
        callback: OnPermissionCallback?,
    ) {
        if (callback == null) {
            return
        }
        callback.onGranted(grantedPermissions, allGranted)
    }

    override fun deniedPermissionRequest(
        activity: Activity, allPermissions: List<String>,
        deniedPermissions: List<String>, doNotAskAgain: Boolean,
        callback: OnPermissionCallback?,
    ) {
        callback?.onDenied(deniedPermissions, doNotAskAgain)
        if (doNotAskAgain) {
            if (deniedPermissions.size == 1 && Permission.ACCESS_MEDIA_LOCATION == deniedPermissions[0]) {
                ToastUtils.show(R.string.common_permission_media_location_hint_fail)
                return
            }
            showPermissionSettingDialog(activity, allPermissions, deniedPermissions, callback)
            return
        }
        if (deniedPermissions.size == 1) {
            val deniedPermission = deniedPermissions[0]
            if (Permission.ACCESS_BACKGROUND_LOCATION == deniedPermission) {
                ToastUtils.show(R.string.common_permission_background_location_fail_hint)
                return
            }
            if (Permission.BODY_SENSORS_BACKGROUND == deniedPermission) {
                ToastUtils.show(R.string.common_permission_background_sensors_fail_hint)
                return
            }
        }
        val message: String
        val permissionNames = permissionsToNames(activity, deniedPermissions)
        message = if (!permissionNames.isEmpty()) {
            activity.getString(
                R.string.common_permission_fail_assign_hint, listToString(activity, permissionNames)
            )
        } else {
            activity.getString(R.string.common_permission_fail_hint)
        }
        ToastUtils.show(message)
    }

    override fun finishPermissionRequest(
        activity: Activity, allPermissions: List<String>,
        skipRequest: Boolean, callback: OnPermissionCallback?,
    ) {
        mRequestFlag = false
        dismissPopupWindow()
    }

    private fun showPopupWindow(activity: Activity, decorView: ViewGroup, message: String) {
        if (mPermissionPopup == null) {
            val contentView: View = LayoutInflater.from(activity)
                .inflate(R.layout.permission_description_popup, decorView, false)
            mPermissionPopup = PopupWindow(activity)
            mPermissionPopup!!.contentView = contentView
            mPermissionPopup!!.width = WindowManager.LayoutParams.MATCH_PARENT
            mPermissionPopup!!.height = WindowManager.LayoutParams.WRAP_CONTENT
            mPermissionPopup!!.animationStyle = android.R.style.Animation_Dialog
            mPermissionPopup!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mPermissionPopup!!.isTouchable = true
            mPermissionPopup!!.isOutsideTouchable = true
        }
        val messageView =
            mPermissionPopup!!.contentView.findViewById<TextView>(R.id.tv_permission_description_message)
        messageView.text = message
        // 注意：这里的 PopupWindow 只是示例，没有监听 Activity onDestroy 来处理 PopupWindow 生命周期
        mPermissionPopup!!.showAtLocation(decorView, Gravity.TOP, 0, 0)
    }

    private fun dismissPopupWindow() {
        if (mPermissionPopup == null) {
            return
        }
        if (!mPermissionPopup!!.isShowing) {
            return
        }
        mPermissionPopup!!.dismiss()
    }

    private fun showPermissionSettingDialog(
        activity: Activity?, allPermissions: List<String>,
        deniedPermissions: List<String>, callback: OnPermissionCallback?,
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return
        }
        val message: String
        val permissionNames = permissionsToNames(activity, deniedPermissions)
        message = if (permissionNames.isNotEmpty()) {
            activity.getString(
                R.string.common_permission_manual_assign_fail_hint,
                listToString(activity, permissionNames)
            )
        } else {
            activity.getString(R.string.common_permission_manual_fail_hint)
        }

        // 这里的 Dialog 只是示例，没有用 DialogFragment 来处理 Dialog 生命周期
        activity.alertDialog {
            title = activity.string(R.string.common_permission_alert)
            this.message = message
            okButton(activity.string(R.string.common_permission_alert)) {
                XXPermissions.startPermissionActivity(
                    activity,
                    deniedPermissions,
                    object : OnPermissionPageCallback {
                        override fun onGranted() {
                            if (callback == null) {
                                return
                            }
                            callback.onGranted(allPermissions, true)
                        }

                        override fun onDenied() {
                            showPermissionSettingDialog(
                                activity,
                                allPermissions,
                                XXPermissions.getDenied(activity, allPermissions),
                                callback
                            )
                        }
                    })
            }
        }.build()
    }

    companion object {
        val HANDLER: Handler = Handler(Looper.getMainLooper())
    }
}
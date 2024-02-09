package com.manchuan.tools.activity.touch

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
import com.manchuan.tools.activity.touch.widget.MultiTouch
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import kotlin.math.max

class MainActivity : BaseActivity() {
    fun a(): FloatArray {
        val defaultDisplay =
            (applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        val displayMetrics = DisplayMetrics()
        defaultDisplay.getRealMetrics(displayMetrics)
        var i = displayMetrics.widthPixels
        var i2 = displayMetrics.heightPixels
        val i3: Int
        val supportedModes = defaultDisplay.supportedModes
        val length = supportedModes.size
        var i4 = 0
        var i5 = i2
        var i6 = i
        var i7 = 60
        while (i4 < length) {
            val mode = supportedModes[i4]
            if (mode.physicalWidth * mode.physicalHeight > i6 * i5) {
                i6 = mode.physicalWidth
                i5 = mode.physicalHeight
            }
            i4++
            i7 = max(i7.toDouble(), mode.refreshRate.toInt().toDouble()).toInt()
        }
        val i8 = i7
        i = i6
        i2 = i5
        i3 = i8
        return floatArrayOf(
            i.toFloat(),
            i2.toFloat(),
            i3.toFloat(),
            displayMetrics.xdpi,
            displayMetrics.ydpi,
            displayMetrics.widthPixels.toFloat(),
            displayMetrics.heightPixels.toFloat()
        )
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        a(this, true, "触控", -1)
        a()
        // 调整测试模式
        val b = "multi" // multi 多点触控，sample 触控采样率，pressure 压力触控，coverage覆盖测试
        val touch = MultiTouch(this)
        touch.setShowInfo(b)
        val i = "请同时用多个手指触摸屏幕，以测试最大支持的多点触摸个数。"
        setContentView(touch)
        BaseAlertDialogBuilder(this).setMessage(i).setPositiveButton("开始测试", null)
            .setOnCancelListener(null).show().setCanceledOnTouchOutside(false)
    }

    fun b() {
        finish()
    }

    companion object {
        fun a(activity: Activity, z: Boolean, str: String?, i: Int) {
            try {
                if (activity.packageManager.hasSystemFeature("android.hardware.type.watch")) {
                    activity.window.requestFeature(11)
                }
            } catch (ignored: Exception) {
            }
            if (z) {
                try {
                    activity.actionBar!!.setDisplayHomeAsUpEnabled(true)
                } catch (ignored: Exception) {
                }
            }
            if (str != null) {
                activity.title = str
            }
            if (i != -1) {
                activity.setContentView(i)
            }
        }

        fun a(activity: Activity?, str: String?) {
            BaseAlertDialogBuilder(activity!!).setMessage(str).setPositiveButton("确定", null)
                .setOnDismissListener(null).show()
        }
    }
}

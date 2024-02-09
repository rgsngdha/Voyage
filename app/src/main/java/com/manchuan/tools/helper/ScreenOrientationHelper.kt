package com.manchuan.tools.helper

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager

object ScreenOrientationHelper {
    val ORIENTATION_TYPE_0 = 0
    val ORIENTATION_TYPE_90 = 90
    val ORIENTATION_TYPE_180 = 180
    val ORIENTATION_TYPE_270 = 270
    private var mOrientationEventListener: OrientationEventListener? = null
    private var mScreenOrientationChangeListener: ScreenOrientationChangeListener? = null
    private var currentType = ORIENTATION_TYPE_0

    fun init(context: Context, listener: ScreenOrientationChangeListener) {
        mScreenOrientationChangeListener = listener
        mOrientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (mScreenOrientationChangeListener == null) {
                    return
                }
                if (orientation > 340 || orientation < 20) {
                    //0
                    if (currentType == 0) {
                        return
                    }
                    if (getScreenRotation(context) == Surface.ROTATION_0) {
                        mScreenOrientationChangeListener!!.onChange(ORIENTATION_TYPE_0)
                        currentType = ORIENTATION_TYPE_0
                    }
                } else if (orientation in 71..109) {
                    //90
                    if (currentType == 90) {
                        return
                    }
                    val angle = getScreenRotation(context)
                    if (angle == Surface.ROTATION_270) {
                        mScreenOrientationChangeListener!!.onChange(ORIENTATION_TYPE_90)
                        currentType = ORIENTATION_TYPE_90
                    }
                } else if (orientation in 161..199) {
                    //180
                    if (currentType == 180) {
                        return
                    }
                    val angle = getScreenRotation(context)
                    if (angle == Surface.ROTATION_180) {
                        mScreenOrientationChangeListener!!.onChange(ORIENTATION_TYPE_180)
                        currentType = ORIENTATION_TYPE_180
                    }
                } else if (orientation in 251..289) {
                    //270
                    if (currentType == 270) {
                        return
                    }
                    val angle = getScreenRotation(context)
                    if (angle == Surface.ROTATION_90) {
                        mScreenOrientationChangeListener!!.onChange(ORIENTATION_TYPE_270)
                        currentType = ORIENTATION_TYPE_270
                    }
                }
            }
        }
        register()
    }

    private fun getScreenRotation(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return windowManager.defaultDisplay?.rotation ?: 0
    }

    fun register() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener!!.enable()
        }
    }

    fun unRegister() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener!!.disable()
        }
    }

    interface ScreenOrientationChangeListener {
        /**
         *
         * @param orientation
         */
        fun onChange(orientation: Int)
    }
}
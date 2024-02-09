package com.manchuan.tools.view

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup

val MATCH_PARENT: Int = ViewGroup.LayoutParams.MATCH_PARENT


internal inline val Context.activityView: View?
    get() = (this as? Activity)?.window?.decorView?.findViewById(android.R.id.content)

fun detachFromParent(view: View): ViewGroup.LayoutParams {
    val layoutParams: ViewGroup.LayoutParams = view.layoutParams
    val parent = view.parent as ViewGroup
    parent.removeView(view)
    return layoutParams
}

//添加到父容器，并设置布局
fun attachAndLayout(parent: ViewGroup, view: View, layoutParams: ViewGroup.LayoutParams) {
    parent.addView(view)
    view.layoutParams = layoutParams
}

//添加并铺满父容器
fun attachAndFill(parent: ViewGroup, view: View?) {
    parent.addView(view, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
}

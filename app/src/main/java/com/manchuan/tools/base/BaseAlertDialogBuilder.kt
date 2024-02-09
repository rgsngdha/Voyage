package com.manchuan.tools.base

import android.content.Context
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.manchuan.tools.database.Global
import com.manchuan.tools.utils.atLeastS

class BaseAlertDialogBuilder : MaterialAlertDialogBuilder {
    constructor(context: Context) : super(context)
    constructor(context: Context, overrideThemeResId: Int) : super(context, overrideThemeResId)

    override fun create(): AlertDialog {
        return super.create().also { dialog ->
            if (atLeastS()) {
                dialog.window?.let {
                    if (Global.isEnabledDialogBlur) {
                        it.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                        it.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                        it.attributes.blurBehindRadius = 32
                    }
                }
            }
        }
    }
}
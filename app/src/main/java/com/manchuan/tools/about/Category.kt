package com.manchuan.tools.about

import android.graphics.drawable.Drawable
import android.view.View

/**
 * @author drakeet
 */
class Category @JvmOverloads constructor(
    val title: String,
    val actionIcon: Drawable? = null,
    val actionIconContentDescription: String? = null
) {
    var onActionClickListener: View.OnClickListener? = null

}
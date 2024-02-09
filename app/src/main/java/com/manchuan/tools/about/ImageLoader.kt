package com.manchuan.tools.about

import android.widget.ImageView

/**
 * @author drakeet
 */
interface ImageLoader {
    fun load(imageView: ImageView, url: String)
}
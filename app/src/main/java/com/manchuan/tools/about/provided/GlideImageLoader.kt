package com.manchuan.tools.about.provided

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.manchuan.tools.about.ImageLoader

/**
 * @author drakeet
 */
class GlideImageLoader : ImageLoader {
    override fun load(imageView: ImageView, url: String) {
        Glide.with(imageView.context).load(url).into(imageView)
    }
}
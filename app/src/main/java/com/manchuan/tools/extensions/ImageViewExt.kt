package com.manchuan.tools.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.panpf.sketch.displayImage
import com.lxj.androidktx.AndroidKTX
import com.lxj.androidktx.core.sp
import com.lxj.androidktx.util.SuperGlideTransformation
import com.manchuan.tools.cache.glide.key.GlideKey
import java.io.File

/**
 * Glide加载图片
 * @param url 可以是网络，可以是File，可以是资源id等等Glide支持的类型
 * @param placeholder 默认占位图
 * @param error 失败占位图
 * @param borderSize 边框粗细
 * @param borderColor 边框颜色
 * @param isCircle 是否是圆形，默认false，注意：isCircle和roundRadius两个只能有一个生效
 * @param isCenterCrop 是否设置scaleType为CenterCrop，你也可以在布局文件中设置
 * @param roundRadius 圆角角度，默认为0，不带圆角，注意：isCircle和roundRadius两个只能有一个生效
 * @param roundArray 圆角Array:[lt,rt, rb, lb]
 * @param isCrossFade 是否有过渡动画，默认没有过渡动画
 * @param isForceOriginalSize 是否强制使用原图，默认false
 */

enum class ImageEngine(name: String) {
    GLIDE("GLIDE"), SKETCH("SKETCH")
}

@SuppressLint("CheckResult")
fun ImageView.load(
    url: Any?, placeholder: Any? = 0, error: Int = 0, isCircle: Boolean = false,
    isCenterCrop: Boolean = false,
    borderSize: Int = 0,
    borderColor: Int = 0,
    blurScale: Float = 0f,
    blurRadius: Float = 20f,
    roundRadius: Int = 0,
    roundArray: FloatArray? = null,
    isCrossFade: Boolean = false,
    skipMemory: Boolean = true,
    isForceOriginalSize: Boolean = false,
    targetWidth: Int = 0,
    thumbnail: Float = 0.0f,
    imageEngine: ImageEngine = ImageEngine.GLIDE,
    targetHeight: Int = 0,
    onImageLoad: ((resource: Drawable?) -> Unit)? = null,
    onImageFail: (() -> Unit)? = null,
    diskCacheStrategy: DiskCacheStrategy = when (sp().getBoolean("glide_cache", false)) {
        true -> DiskCacheStrategy.AUTOMATIC
        false -> DiskCacheStrategy.NONE
    },
    signature: GlideKey? = null,
) {
    if (context == null) return
    if (context is Activity && ((context as Activity).isDestroyed || (context as Activity).isFinishing)) return
    var round = roundRadius
    if (isCenterCrop && scaleType != ImageView.ScaleType.CENTER_CROP) {
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
    if (isCircle && round == 0) {
        round = (measuredWidth.coerceAtLeast(layoutParams.width)) / 2
    }
    val options = RequestOptions().placeholder(placeholder as Int).error(error).apply {
        if (isForceOriginalSize) {
            override(Target.SIZE_ORIGINAL)
        }
        if (targetWidth != 0 && targetHeight != 0) {
            override(targetWidth, targetHeight)
        }
    }
    val superTransform = SuperGlideTransformation(
        isCenterCrop = isCenterCrop || scaleType == ImageView.ScaleType.CENTER_CROP,
        scale = blurScale,
        borderSize = borderSize,
        borderColor = borderColor,
        blurRadius = blurRadius,
        roundRadius = round,
        roundArray = roundArray
    )
    options.transform(superTransform)
    val glide = Glide.with(context).load(url).thumbnail(thumbnail).skipMemoryCache(skipMemory)
        .diskCacheStrategy(diskCacheStrategy).apply(options).apply {
            if (isCrossFade) transition(DrawableTransitionOptions.withCrossFade())
            if (onImageLoad != null || onImageFail != null) {
                listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        onImageFail?.invoke()
                        return onImageFail != null
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        onImageLoad?.invoke(resource)
                        return false
                    }
                })
            }

        }
    if (signature != null) {
        glide.signature(signature)
    }
    when (imageEngine) {
        ImageEngine.GLIDE -> glide.into(this)
        ImageEngine.SKETCH -> {
            if (url is Uri) {
                displayImage(url) {
                    if (isCrossFade) crossfade()
                }
            } else if (url is String) {
                displayImage(url) {
                    if (isCrossFade) crossfade()
                }
            } else if (url is File) {
                displayImage(url) {
                    if (isCrossFade) crossfade()
                }
            } else if (url is Int) {
                displayImage(url) {
                    if (isCrossFade) crossfade()
                }
            }
        }
    }
}

fun String.preloadImage(onSuccess: ((Drawable?) -> Unit)? = null, onFail: (() -> Unit)? = null) {
    Glide.with(AndroidKTX.context).load(this).diskCacheStrategy(DiskCacheStrategy.ALL)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean,
            ): Boolean {
                onFail?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean,
            ): Boolean {
                onSuccess?.invoke(resource)
                return false
            }
        }).preload()
}


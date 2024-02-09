package com.manchuan.tools.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.textview.MaterialTextView
import com.lxj.androidktx.core.string
import com.manchuan.tools.R
import com.manchuan.tools.extensions.errorColor
import com.manchuan.tools.extensions.getDimensionPixelSize

class BrainLoadFailedView(context: Context, attributeSet: AttributeSet? = null) :
    AViewGroup(context, attributeSet) {

    private val loadingView = LottieAnimationView(context).apply {
        val size = context.getDimensionPixelSize(R.dimen.lottie_anim_size)
        layoutParams = FrameLayout.LayoutParams(size, size).also {
            it.gravity = Gravity.CENTER
        }
        imageAssetsFolder = "/"
        repeatCount = LottieDrawable.INFINITE
        setAnimation("anim/brain_explosion.json")
        addView(this)
        playAnimation()
    }

    private val errorTextView = MaterialTextView(context).apply {
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 32.dp)
        }
        text = string(R.string.error)
        textSize = 18f
        setTextColor(context.errorColor())
        addView(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        loadingView.autoMeasure()
        errorTextView.autoMeasure()
        setMeasuredDimension(
            loadingView.measuredWidth.coerceAtLeast(errorTextView.measuredWidth),
            loadingView.measuredHeight + errorTextView.measuredHeight
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        loadingView.layout(loadingView.toHorizontalCenter(this), 0)
        errorTextView.layout(errorTextView.toHorizontalCenter(this), loadingView.bottom)
    }
}
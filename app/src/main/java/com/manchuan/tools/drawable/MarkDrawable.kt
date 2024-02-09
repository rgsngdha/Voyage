package com.manchuan.tools.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint

class MarkDrawable : Drawable {
    private var mPaint: TextPaint? = null
    private var mTextColor = -0x71e1e
    private var mBackgroundColor = 0xffffff
    private var mBoundRect: RectF? = null
    private val mMarkStr: String
    private var mTextSize = 40
    private val mDegree = 30

    constructor(mMarkStr: String) {
        this.mMarkStr = mMarkStr
        init()
    }

    constructor(mMarkStr: String, textColor: Int) {
        this.mMarkStr = mMarkStr
        mTextColor = textColor
        init()
    }

    constructor(mMarkStr: String, textColor: Int, backgroundColor: Int) {
        this.mMarkStr = mMarkStr
        mTextColor = textColor
        mBackgroundColor = backgroundColor
        init()
    }

    constructor(mMarkStr: String, textColor: Int, textSize: Int, backgroundColor: Int) {
        this.mMarkStr = mMarkStr
        mTextColor = textColor
        mTextSize = textSize
        mBackgroundColor = backgroundColor
        init()
    }

    private fun init() {
        mPaint = TextPaint()
        mPaint!!.textSize = mTextSize.toFloat()
        mPaint!!.isAntiAlias = true
        mPaint!!.color = mTextColor
        val width = mPaint!!.measureText(mMarkStr, 0, mMarkStr.length)
        val rect = Rect()
        mPaint!!.getTextBounds(mMarkStr, 0, mMarkStr.length, rect)
        mBoundRect = RectF()
        mBoundRect!![0f, 0f, (width * Math.cos(Math.toRadians(mDegree.toDouble()))).toFloat() + inset] =
            (width * Math.sin(Math.toRadians(mDegree.toDouble()))).toFloat() + inset
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.drawColor(mBackgroundColor)
        canvas.translate(mBoundRect!!.width() / 2, mBoundRect!!.height() / 2)
        canvas.rotate(-mDegree.toFloat())
        canvas.drawText(mMarkStr, inset / 2 - mBoundRect!!.width() / 2, 0f, mPaint!!)
        canvas.restore()
    }

    override fun setAlpha(i: Int) {
        mPaint!!.alpha = i
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint!!.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicHeight(): Int {
        return mBoundRect!!.height().toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return mBoundRect!!.width().toInt()
    }

    companion object {
        private const val inset = 80
    }
}
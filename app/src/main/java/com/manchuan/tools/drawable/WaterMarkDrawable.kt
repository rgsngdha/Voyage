package com.manchuan.tools.drawable

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable

class WaterMarkDrawable : Drawable {
    private val mMarkDrawable: MarkDrawable
    private val mBoundRect: RectF
    private val mShader: BitmapShader
    private val mPaint: Paint
    private val mMarkStr: String? = null

    constructor(markStr: String?) {
        mMarkDrawable = MarkDrawable(markStr!!)
        mBoundRect = RectF()
        val width = mMarkDrawable.intrinsicWidth
        val height = mMarkDrawable.intrinsicHeight
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        mMarkDrawable.setBounds(0, 0, width, height)
        mMarkDrawable.draw(canvas)
        mShader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPaint.shader = mShader
    }

    constructor(markStr: String?, textColor: Int) {
        mMarkDrawable = MarkDrawable(markStr!!, textColor)
        mBoundRect = RectF()
        val width = mMarkDrawable.intrinsicWidth
        val height = mMarkDrawable.intrinsicHeight
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        mMarkDrawable.setBounds(0, 0, width, height)
        mMarkDrawable.draw(canvas)
        mShader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPaint.shader = mShader
    }

    constructor(markStr: String?, textColor: Int, backgroundColor: Int) {
        mMarkDrawable = MarkDrawable(markStr!!, textColor, backgroundColor)
        mBoundRect = RectF()
        val width = mMarkDrawable.intrinsicWidth
        val height = mMarkDrawable.intrinsicHeight
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        mMarkDrawable.setBounds(0, 0, width, height)
        mMarkDrawable.draw(canvas)
        mShader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPaint.shader = mShader
    }

    constructor(markStr: String?, textColor: Int, textSize: Int, backgroundColor: Int) {
        mMarkDrawable = MarkDrawable(markStr!!, textColor, textSize, backgroundColor)
        mBoundRect = RectF()
        val width = mMarkDrawable.intrinsicWidth
        val height = mMarkDrawable.intrinsicHeight
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        mMarkDrawable.setBounds(0, 0, width, height)
        mMarkDrawable.draw(canvas)
        mShader = BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPaint.shader = mShader
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, mBoundRect.right, mBoundRect.bottom, mPaint)
    }

    override fun setAlpha(i: Int) {
        mPaint.alpha = i
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        mBoundRect[left.toFloat(), top.toFloat(), right.toFloat()] = bottom.toFloat()
    }
}
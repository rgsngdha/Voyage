package com.manchuan.tools.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.manchuan.tools.extensions.accentColor
import com.manchuan.tools.extensions.windowBackground
import com.manchuan.tools.utils.ColorUtils.statusBarColor

class RulerView : SurfaceView, SurfaceHolder.Callback {
    private var UNIT_MM = 0f
    private var RULE_HEIGHT = 0f
    private var RULE_SCALE = 0f
    private var SCREEN_W = 0
    private var SCREEN_H = 0
    private var FONT_SIZE = 0f
    private var PADDING = 0f
    private var RADIUS_BIG = 0f
    private var RADIUS_MEDIUM = 0f
    private var RADIUS_SMALL = 0f
    private var CYCLE_WIDTH = 0f
    private var DISPLAY_SIZE_BIG = 0f
    private var DISPLAY_SIZE_SMALL = 0f
    private var holder: SurfaceHolder? = null
    private var unlockLineCanvas = false
    private var lineX = 0f
    private var lineOffset = 0f
    private var startX = 0f
    private var lastX = 0f
    private var kedu = 0
    var paint: Paint? = null
    private var linePaint: Paint? = null
    private var fontPaint: Paint? = null
    fun getKedu(): Int {
        return kedu
    }

    fun setKedu(kedu: Int) {
        this.kedu = kedu
        draw()
    }

    fun getLineX(): Float {
        return lineX
    }

    fun setLineX(lineX: Float) {
        this.lineX = lineX
        draw()
    }

    private fun onTouchBegain(x: Float, y: Float) {
        lineOffset = Math.abs(x - lineX)
        if (lineOffset <= PADDING * 2) {
            startX = x
            unlockLineCanvas = true
        }
    }

    private fun onTouchMove(x: Float, y: Float) {
        if (unlockLineCanvas) {
            lineX += x - startX
            if (lineX < PADDING) {
                lineX = PADDING
            } else if (lineX > lastX) {
                lineX = lastX
            }
            kedu = Math.round((lineX - PADDING) / UNIT_MM)
            startX = x
            draw()
        }
    }

    private fun onTouchDone(x: Float, y: Float) {
        unlockLineCanvas = false
        startX = -1f
        draw()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // TODO Auto-generated method stub
        when (event.action) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> onTouchDone(event.x, event.y)
            MotionEvent.ACTION_DOWN -> onTouchBegain(event.x, event.y)
            MotionEvent.ACTION_MOVE -> onTouchMove(event.x, event.y)
        }
        return true
    }

    private fun init(context: Context) {
        val dm = DisplayMetrics()
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)
        RADIUS_BIG = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 46f,
            dm
        )
        RADIUS_MEDIUM = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            40f, dm
        )
        RADIUS_SMALL = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f, dm
        )
        CYCLE_WIDTH = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f,
            dm
        )
        DISPLAY_SIZE_BIG = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 40f, dm
        )
        DISPLAY_SIZE_SMALL = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, dm
        )
        UNIT_MM = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm)
        RULE_HEIGHT = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            30f, dm
        )
        FONT_SIZE = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f,
            dm
        )
        PADDING = FONT_SIZE / 2
        SCREEN_W = dm.widthPixels
        SCREEN_H = dm.heightPixels
        holder = getHolder()
        holder!!.addCallback(this)
        paint = Paint()
        paint!!.color = context.accentColor()
        linePaint = Paint()
        linePaint!!.color = context.accentColor()
        linePaint!!.strokeWidth = 4f
        fontPaint = Paint()
        fontPaint!!.textSize = FONT_SIZE
        fontPaint!!.isAntiAlias = true
        fontPaint!!.color = context.accentColor()
        lineX = PADDING
        kedu = 0
    }

    private fun drawDisplay(canvas: Canvas?) {
        val cm = (kedu / 10).toString()
        val mm = (kedu % 10).toString()
        val displayPaint1 = Paint()
        displayPaint1.isAntiAlias = true
        displayPaint1.color = context.accentColor()
        displayPaint1.textSize = DISPLAY_SIZE_BIG
        val cmWidth = displayPaint1.measureText(cm)
        val bounds1 = Rect()
        displayPaint1.getTextBounds(cm, 0, cm.length, bounds1)
        val displayPaint2 = Paint()
        displayPaint2.isAntiAlias = true
        displayPaint2.color = context.windowBackground()
        displayPaint2.textSize = DISPLAY_SIZE_SMALL
        val mmWidth = displayPaint2.measureText(mm)
        val bounds2 = Rect()
        displayPaint2.getTextBounds(mm, 0, mm.length, bounds2)
        canvas!!.drawLine(lineX, 0f, lineX, SCREEN_H.toFloat(), linePaint!!)
        val cyclePaint = Paint()
        cyclePaint.color = -0x1
        cyclePaint.isAntiAlias = true
        cyclePaint.style = Paint.Style.FILL
        val strokPaint = Paint()
        strokPaint.isAntiAlias = true
        strokPaint.color = -0x666667
        strokPaint.style = Paint.Style.STROKE
        strokPaint.strokeWidth = CYCLE_WIDTH
        canvas.drawCircle(
            (SCREEN_W shr 1).toFloat(),
            (SCREEN_H shr 1).toFloat(),
            RADIUS_BIG,
            cyclePaint
        )
        canvas.drawCircle(
            (SCREEN_W shr 1).toFloat(),
            (SCREEN_H shr 1).toFloat(),
            RADIUS_MEDIUM,
            cyclePaint
        )
        canvas.drawCircle(
            (SCREEN_W shr 1).toFloat(),
            (SCREEN_H shr 1).toFloat(),
            RADIUS_BIG,
            strokPaint
        )
        strokPaint.color = -0x99999a
        canvas.drawCircle(
            (SCREEN_W shr 1).toFloat(),
            (SCREEN_H shr 1).toFloat(),
            RADIUS_MEDIUM,
            strokPaint
        )
        strokPaint.color = -0x666667
        canvas.drawCircle(
            (SCREEN_W shr 1) + RADIUS_BIG, (SCREEN_H shr 1).toFloat(),
            RADIUS_SMALL, cyclePaint
        )
        canvas.drawCircle(
            (SCREEN_W shr 1) + RADIUS_BIG, (SCREEN_H shr 1).toFloat(),
            RADIUS_SMALL, strokPaint
        )
        canvas.drawText(
            cm, (SCREEN_W shr 1) - cmWidth / 2,
            (
                    (SCREEN_H shr 1) + (bounds1.height() shr 1)).toFloat(), displayPaint1
        )
        canvas.drawText(
            mm,
            (SCREEN_W shr 1) + RADIUS_BIG - mmWidth / 2,
            ((SCREEN_H shr 1) + (bounds2.height() shr 1)).toFloat(),
            displayPaint2
        )
    }

    private fun draw() {
        var canvas: Canvas? = null
        try {
            canvas = holder!!.lockCanvas()
            canvas.drawColor(statusBarColor)
            var left = PADDING
            var i = 0
            while (SCREEN_W - PADDING - left > 0) {
                RULE_SCALE = 0.5f
                if (i % 5 == 0) {
                    if (i and 0x1 == 0) {
                        RULE_SCALE = 1f
                        val txt = (i / 10).toString()
                        val bounds = Rect()
                        val txtWidth = fontPaint!!.measureText(txt)
                        fontPaint!!.getTextBounds(txt, 0, txt.length, bounds)
                        canvas.drawText(
                            txt,
                            left - txtWidth / 2,
                            RULE_HEIGHT + FONT_SIZE / 2 + bounds.height(),
                            fontPaint!!
                        )
                    } else {
                        RULE_SCALE = 0.75f
                    }
                }
                val rect = RectF()
                rect.left = left - 1
                rect.top = 0f
                rect.right = left + 1
                rect.bottom = rect.top + RULE_HEIGHT * RULE_SCALE
                canvas.drawRect(rect, paint!!)
                left += UNIT_MM
                i++
            }
            lastX = left - UNIT_MM
            drawDisplay(canvas)
        } catch (e: Exception) {
            // TODO: handle exception
        } finally {
            if (canvas != null) {
                holder!!.unlockCanvasAndPost(canvas)
            }
        }
    }

    constructor(context: Context) : super(context) {
        // TODO Auto-generated constructor stub
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        // TODO Auto-generated constructor stub
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        // TODO Auto-generated constructor stub
        init(context)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // TODO Auto-generated method stub
        object : Thread() {
            override fun run() {
                draw()
            }
        }.start()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder, format: Int, width: Int,
        height: Int
    ) {
        // TODO Auto-generated method stub
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // TODO Auto-generated method stub
    }
}
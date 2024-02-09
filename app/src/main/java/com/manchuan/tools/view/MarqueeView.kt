package com.manchuan.tools.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.TintTypedArray
import com.manchuan.tools.R

class MarqueeView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Runnable {
    private var string: String? = null //最终绘制的文本
    private var speed = 1f //移动速度
    private var textColor = Color.BLACK //文字颜色,默认黑色
    private var textSize = 12f //文字颜色,默认黑色
    private var textdistance = 0 //
    private var textDistance1 = 10 //item间距，dp单位
    private var black_count = "" //间距转化成空格距离
    private var repetType = REPET_INTERVAL //滚动模式
    private var startLocationDistance = 1.0f //开始的位置选取，百分比来的，距离左边，0~1，0代表不间距，1的话代表，从右面，1/2代表中间。
    private var isClickStop = false //点击是否暂停
    private var isResetLocation = true //默认为true
    private var xLocation = 0f //文本的x坐标
    private var contentWidth = 0 //内容的宽度
    private var isRoll = false //是否继续滚动
    private var oneBlack_width = 0f //空格的宽度
    private var paint: TextPaint? = null //画笔
    private var rect: Rect? = null
    private var repetCount = 0 //
    private var resetInit = true
    private var thread: Thread? = null
    private var content = ""
    private var textHeight = 0f

    init {
        initattrs(attrs)
        initpaint()
        initClick()
    }

    private fun initClick() {
        setOnClickListener {
            if (isClickStop) {
                if (isRoll) {
                    stopRoll()
                } else {
                    continueRoll()
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initattrs(attrs: AttributeSet?) {
        val tta = TintTypedArray.obtainStyledAttributes(
            context, attrs,
            R.styleable.MarqueeView
        )
        textColor = tta.getColor(R.styleable.MarqueeView_marqueeview_text_color, textColor)
        isClickStop =
            tta.getBoolean(R.styleable.MarqueeView_marqueeview_isclickalbe_stop, isClickStop)
        isResetLocation =
            tta.getBoolean(R.styleable.MarqueeView_marqueeview_is_resetLocation, isResetLocation)
        speed = tta.getFloat(R.styleable.MarqueeView_marqueeview_text_speed, speed)
        textSize = tta.getFloat(R.styleable.MarqueeView_marqueeview_text_size, textSize)
        textDistance1 =
            tta.getInteger(R.styleable.MarqueeView_marqueeview_text_distance, textDistance1)
        startLocationDistance = tta.getFloat(
            R.styleable.MarqueeView_marqueeview_text_startlocationdistance,
            startLocationDistance
        )
        repetType = tta.getInt(R.styleable.MarqueeView_marqueeview_repet_type, repetType)
        tta.recycle()
    }

    /**
     * 刻字机修改
     */
    private fun initpaint() {
        rect = Rect()
        paint = TextPaint(Paint.ANTI_ALIAS_FLAG) //初始化文本画笔
        paint!!.style = Paint.Style.FILL
        paint!!.color = textColor //文字颜色值,可以不设定
        paint!!.textSize = dp2px(textSize).toFloat() //文字大小
    }

    fun dp2px(dpValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (resetInit) {
            setTextDistance(textDistance1)
            if (startLocationDistance < 0) {
                startLocationDistance = 0f
            } else if (startLocationDistance > 1) {
                startLocationDistance = 1f
            }
            xLocation = width * startLocationDistance
            //            Log.e(TAG, "onMeasure: --- " + xLocation);
            resetInit = false
        }
        when (repetType) {
            REPET_INTERVAL -> {
                if (contentWidth <= -xLocation) {
                    //也就是说文字已经到头了
                    xLocation = width.toFloat()
                }
            }

            REPET_CONTINUOUS -> {
                if (xLocation < 0) {
                    val beAppend = (-xLocation / contentWidth).toInt()
                    Log.e(
                        TAG,
                        "onDraw: ---" + contentWidth + "--------" + -xLocation + "------" + beAppend
                    )
                    if (beAppend >= repetCount) {
                        repetCount++
                        //也就是说文字已经到头了
//                    xLocation = speed;//这个方法有问题，所以采取了追加字符串的 方法
                        string = string + content
                    }
                }
            }

            else -> {

                //默认一次到头好了
                if (contentWidth < -xLocation) {
                    //也就是说文字已经到头了
//                    此时停止线程就可以了
                    stopRoll()
                }
            }
        }


        //把文字画出来
        if (string != null) {
            canvas.drawText(string!!, xLocation, height / 2 + textHeight / 2, paint!!)
        }
    }

    fun setRepetType(repetType: Int) {
        this.repetType = repetType
        resetInit = true
        setContent(content)
    }

    override fun run() {
        while (isRoll && !TextUtils.isEmpty(content)) {
            try {
                Thread.sleep(10)
                xLocation = xLocation - speed
                postInvalidate() //每隔10毫秒重绘视图
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 继续滚动
     */
    fun continueRoll() {
        if (!isRoll) {
            if (thread != null) {
                thread!!.interrupt()
                thread = null
            }
            isRoll = true
            thread = Thread(this)
            thread!!.start() //开启死循环线程让文字动起来
        }
    }

    /**
     * 停止滚动
     */
    fun stopRoll() {
        isRoll = false
        if (thread != null) {
            thread!!.interrupt()
            thread = null
        }
    }

    /**
     * 点击是否暂停，默认是不
     *
     * @param isClickStop
     */
    private fun setClickStop(isClickStop: Boolean) {
        this.isClickStop = isClickStop
    }

    /**
     * 是否循环滚动
     *
     * @param isContinuable
     */
    private fun setContinueble(isContinuable: Int) {
        repetType = isContinuable
    }
    //    /**
    //     * 是否反向
    //     *
    //     * @param isResversable
    //     */
    //    private void setReversalble(boolean isResversable) {
    //        this.isResversable = isResversable;
    //    }
    /**
     * 设置文字间距  不过如果内容是List形式的，该方法不适用 ,list的数据源，必须在设置setContent之前调用此方法。
     * @param textdistance2
     */
    fun setTextDistance(textdistance2: Int) {


        //设置之后就需要初始化了
        var textdistance2 = textdistance2
        val black = " "
        oneBlack_width = blacktWidth //空格的宽度
        textdistance2 = dp2px(textdistance2.toFloat())
        var count = (textdistance2 / oneBlack_width).toInt() //空格个数，有点粗略，有兴趣的朋友可以精细
        if (count == 0) {
            count = 1
        }
        textdistance = (oneBlack_width * count).toInt()
        black_count = ""
        for (i in 0..count) {
            black_count = black_count + black //间隔字符串
        }
        setContent(content) //设置间距以后要重新刷新内容距离，不过如果内容是List形式的，该方法不适用
    }

    private val blacktWidth: Float
        /**
         * 计算出一个空格的宽度
         * @return
         */
        private get() {
            val text1 = "en en"
            val text2 = "enen"
            return getContentWidth(text1) - getContentWidth(text2)
        }

    private fun getContentWidth(black: String?): Float {
        if (black == null || black === "") {
            return 0F
        }
        if (rect == null) {
            rect = Rect()
        }
        paint!!.getTextBounds(black, 0, black.length, rect)
        textHeight = contentHeight
        return rect!!.width().toFloat()
    }

    private val contentHeight: Float
        /**
         * http://blog.csdn.net/u014702653/article/details/51985821
         * 详细解说了
         *
         * @param
         * @return
         */
        private get() {
            val fontMetrics = paint!!.fontMetrics
            return Math.abs(fontMetrics.bottom - fontMetrics.top) / 2
        }

    /**
     * 设置文字颜色
     *
     * @param textColor
     */
    fun setTextColor(textColor: Int) {
        if (textColor != 0) {
            this.textColor = textColor
            paint!!.color = textColor //文字颜色值,可以不设定
        }
    }

    /**
     * 设置文字大小
     *
     * @param textSize
     */
    fun setTextSize(textSize: Float) {
        if (textSize > 0) {
            this.textSize = textSize
            paint!!.textSize = dp2px(textSize).toFloat() //文字颜色值,可以不设定
            contentWidth = (getContentWidth(content) + textdistance).toInt() //大小改变，需要重新计算宽高
        }
    }

    /**
     * 设置滚动速度
     *
     * @param speed
     */
    fun setTextSpeed(speed: Float) {
        this.speed = speed
    }

    /**
     * |设置滚动的条目内容 ， 集合形式的
     *
     * @param strings
     */
    fun setContent(strings: List<String>?) {
        setTextDistance(textDistance1)
        var temString = ""
        if (strings != null && strings.size != 0) {
            for (i in strings.indices) {
                temString = temString + strings[i] + black_count
            }
        }
        setContent(temString)
    }

    /**
     * 设置滚动的条目内容  字符串形式的
     *
     * @parambt_control00
     */
    fun setContent(content2: String) {
        var content2 = content2
        if (TextUtils.isEmpty(content2)) {
            return
        }
        if (isResetLocation) { //控制重新设置文本内容的时候，是否初始化xLocation。
            xLocation = width * startLocationDistance
        }
        if (!content2.endsWith(black_count)) {
            content2 = content2 + black_count //避免没有后缀
        }
        content = content2

        //这里需要计算宽度啦，当然要根据模式来搞
        if (repetType == REPET_CONTINUOUS) {
//如果说是循环的话，则需要计算 文本的宽度 ，然后再根据屏幕宽度 ， 看能一个屏幕能盛得下几个文本
            contentWidth = (getContentWidth(content) + textdistance).toInt() //可以理解为一个单元内容的长度
            //从0 开始计算重复次数了， 否则到最后 会跨不过这个坎而消失。
            repetCount = 0
            val contentCount = width / contentWidth + 2
            string = ""
            for (i in 0..contentCount) {
                string = string + content //根据重复次数去叠加。
            }
        } else {
            if (xLocation < 0 && repetType == REPET_ONCETIME) {
                if (-xLocation > contentWidth) {
                    xLocation = width * startLocationDistance
                }
            }
            contentWidth = getContentWidth(content).toInt()
            string = content2
        }
        if (!isRoll) { //如果没有在滚动的话，重新开启线程滚动
            continueRoll()
        }
    }

    /**
     * 从新添加内容的时候，是否初始化位置
     *
     * @param isReset
     */
    private fun setResetLocation(isReset: Boolean) {
        isResetLocation = isReset
    }

    fun appendContent(appendContent: String?) {
//有兴趣的朋友可以自己完善，在现有的基础之上，静默追加新的 公告
    }

    companion object {
        private const val TAG = "MarqueeView"
        const val REPET_ONCETIME = 0 //一次结束
        const val REPET_INTERVAL = 1 //一次结束以后，再继续第二次
        const val REPET_CONTINUOUS = 2 //紧接着 滚动第二次
    }
}

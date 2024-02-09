package com.manchuan.tools.activity.life

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.blankj.utilcode.util.ColorUtils
import com.drake.serialize.serialize.serialLazy
import com.drake.statusbar.immersive
import com.dylanc.longan.startActivity
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.manchuan.tools.activity.normal.FullScreenMarQuee
import com.manchuan.tools.databinding.ActivityMarqueeBinding
import com.manchuan.tools.view.MarqueeView
import com.maxkeppeler.sheets.color.ColorSheet
import rikka.material.app.MaterialActivity
import java.util.Objects

class MarQueeActivity : MaterialActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var seekBar1: Slider
    private lateinit var seekBar2: Slider
    private lateinit var marqueeView: MarqueeView
    private lateinit var marqueeBinding: ActivityMarqueeBinding
    private var textColor: Int by serialLazy(Color.BLACK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        marqueeBinding = ActivityMarqueeBinding.inflate(layoutInflater)
        setContentView(marqueeBinding.root)
        toolbar = marqueeBinding.toolbar
        seekBar1 = marqueeBinding.size
        seekBar2 = marqueeBinding.speed
        marquee_content = marqueeBinding.marqueeContent
        marquee_color = marqueeBinding.marqueeColor
        marquee_start = marqueeBinding.marqueeStart
        marqueeView = marqueeBinding.marqueeView
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        marqueeView.setContent("文本预览")
        marqueeView.setTextSize(seekBar1.value)
        marqueeView.setTextSpeed(seekBar2.value)
        marqueeBinding.colorPicker.setCardBackgroundColor(textColor)
        marqueeBinding.marqueeColor.text = String.format("#%02X", textColor)
        when (ColorUtils.isLightColor(textColor)) {
            true -> {
                marqueeBinding.colorTitle.setTextColor(Color.BLACK)
                marqueeBinding.marqueeColor.setTextColor(Color.BLACK)
            }

            false -> {
                marqueeBinding.colorTitle.setTextColor(Color.WHITE)
                marqueeBinding.marqueeColor.setTextColor(Color.WHITE)
            }
        }
        marqueeBinding.colorPicker.setOnClickListener {
            ColorSheet().show(this) {
                title("选择文字颜色")
                onPositive { color ->
                    // Use color
                    marqueeBinding.colorPicker.setCardBackgroundColor(color)
                    textColor = color
                    marqueeBinding.marqueeColor.text = String.format("#%02X", color)
                    if (ColorUtils.isLightColor(color)) {
                        marqueeBinding.colorTitle.setTextColor(Color.BLACK)
                        marqueeBinding.marqueeColor.setTextColor(Color.BLACK)
                    } else {
                        marqueeBinding.colorTitle.setTextColor(Color.WHITE)
                        marqueeBinding.marqueeColor.setTextColor(Color.WHITE)
                    }
                }
            }
        }
        seekBar1.addOnChangeListener { slider, value, fromUser ->
            marqueeView.setTextSize(value)
        }
        seekBar2.addOnChangeListener { slider, value, fromUser ->
            marqueeView.setTextSpeed(value)
        }
        marquee_content!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                marqueeView.setContent(marQueeContent)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        immersive(marqueeBinding.toolbar)
        marqueeBinding.marqueeStart.setOnClickListener {
            if (marqueeBinding.marqueeContent.text.toString().isEmpty()) {
                marqueeBinding.marqueeContentlay.error = "不能为空"
            } else { // A类传递参数到B类
                startActivity<FullScreenMarQuee>(
                    "content" to marQueeContent,
                    "color" to textColor,
                    "size" to seekBar1.value,
                    "speed" to seekBar2.value
                )
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        supportActionBar!!.title = "滚动字幕"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    val marQueeContent: String
        get() = marquee_content!!.text.toString()
    private val marQueeColor: Int
        get() = Objects.requireNonNull(marquee_color!!.text).toString().toInt()
    val marQueeSize: Float
        get() = marquee_size!!.text.toString().toFloat()
    val marQueeSpeed: Float
        get() = marquee_speed!!.text.toString().toFloat()
    private var marquee_contentlay: TextInputLayout? = null
    private var marquee_content: TextInputEditText? = null
    private var mqcl: TextInputLayout? = null
    private var marquee_color: TextView? = null
    private val msize: TextInputLayout? = null
    private val marquee_size: TextInputEditText? = null
    private val mspeed: TextInputLayout? = null
    private val marquee_speed: TextInputEditText? = null
    private var marquee_start: Button? = null

    companion object {
        const val requestCode = 10003 //自定义申请码
    }
}
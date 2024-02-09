package com.manchuan.tools.activity.normal

import android.app.Activity
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.isFullScreen
import com.dylanc.longan.safeIntentExtras
import com.manchuan.tools.R
import com.manchuan.tools.view.MarqueeView

/**
 * @author pedra
 */
class FullScreenMarQuee : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        isFullScreen = true
        setContentView(R.layout.fullscreen_marquee)
        initView(this)
        val intent = intent
        val bundle = intent.extras
        val a: Any? = bundle!!.getString("content")
        runOnUiThread { marquee_view!!.setContent(safeIntentExtras<String>("content").value) }
        try {
            marquee_view!!.setTextColor(bundle.getInt("color"))
        } catch (ignored: Exception) {
        }
        if (bundle.getFloat("size") != 0F) {
            marquee_view!!.setTextSize(bundle.getFloat("size"))
        }
        if (bundle.getFloat("speed") != 0F) {
            marquee_view!!.setTextSpeed(bundle.getFloat("speed"))
        }
    }

    private var marquee_view: MarqueeView? = null
    private fun initView(activity: Activity) {
        marquee_view = activity.findViewById(R.id.marquee_view)
    }
}
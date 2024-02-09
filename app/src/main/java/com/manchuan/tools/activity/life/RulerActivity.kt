package com.manchuan.tools.activity.life

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.isFullScreen
import com.dylanc.longan.isLandscape
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.manchuan.tools.R

class RulerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLandscape = true
        isFullScreen = true
        setContentView(R.layout.activity_ruler)
        ImmersionBar.with(this).fullScreen(true).hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)
            .keyboardEnable(false).init()
    }
}
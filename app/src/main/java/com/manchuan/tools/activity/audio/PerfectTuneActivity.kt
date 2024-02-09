package com.manchuan.tools.activity.audio

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.karlotoy.perfectune.instance.PerfectTune
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.click
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPerfectTuneBinding

class PerfectTuneActivity : BaseActivity() {

    private val binding by viewBinding(ActivityPerfectTuneBinding::inflate)

    private val perfectTune by lazy {
        PerfectTune()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "自定义频率音频"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.apply {
            play.click {
                if (inputFreq.isTextNotEmpty() && inputAmplitude.isTextNotEmpty()) {
                    perfectTune.stopTune()
                    perfectTune.tuneFreq = inputFreq.textString.toDouble()
                    perfectTune.tuneAmplitude = inputAmplitude.textString.toInt()
                    perfectTune.playTune()
                    stop.animateVisible()
                } else {
                    toast("频率与振幅不能为空")
                }
            }
            stop.click {
                perfectTune.stopTune()
                it.animateGone()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        perfectTune.stopTune()
    }

}
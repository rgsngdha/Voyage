package com.manchuan.tools.extensions.floating.life

import android.content.Context
import com.drake.net.time.Interval
import com.lxj.androidktx.core.dateNow
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.manchuan.tools.R
import com.manchuan.tools.databinding.TimeFloatBinding
import com.manchuan.tools.extensions.toast
import java.util.concurrent.TimeUnit

object TimeFloating {

    fun show(context: Context) {
        EasyFloat.with(context).setLayout(R.layout.time_float) { view ->
            val binding = TimeFloatBinding.bind(view)
            Interval(1, TimeUnit.MILLISECONDS).subscribe {
                binding.time.text = dateNow("HH:mm:ss SSS")
            }.start()
            binding.card.setOnLongClickListener {
                EasyFloat.dismiss()
                true
            }
        }.setShowPattern(ShowPattern.ALL_TIME).setDragEnable(true).hasEditText(false).show()
        toast("长按即可关闭")
    }

}
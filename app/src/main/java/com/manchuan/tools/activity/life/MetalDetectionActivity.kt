package com.manchuan.tools.activity.life

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.view.MenuItem
import android.view.WindowManager
import com.crazylegend.viewbinding.viewBinding
import com.drake.statusbar.immersive
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityMetalBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.errorColor
import com.manchuan.tools.extensions.textColorPrimary
import com.nowfal.kdroidext.kex.sensorManager
import com.zhzc0x.chart.AxisInfo
import java.math.BigDecimal
import kotlin.math.sqrt

class MetalDetectionActivity : BaseActivity() {
    private var alarmLim = 0.0

    private val binding by viewBinding(ActivityMetalBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "金属探测器"
        }
        immersive(binding.toolbar)
        binding.chart.setData(
            listOf(
                AxisInfo(200f), AxisInfo(0f), AxisInfo(-200f)
            ), yAxisUnit = "μT", autoZoomYMax = true
        )
        binding.chart.setAutoZoomInterval(0.4f)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(sensorEventListener)
    }

    public override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(
            sensorEventListener, sensorManager!!.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD
            ), SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private val sensorEventListener: SensorEventListener = object : SensorEventListener {
        @SuppressLint("SetTextI18n")
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            val rawTotal: Double //未处理的数据
            if (sensorEvent.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                //保持屏幕常亮
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                //分别计算三轴磁感应强度
                val X_lateral = sensorEvent.values[0]
                val Y_lateral = sensorEvent.values[1]
                val Z_lateral = sensorEvent.values[2]
                //Log.d(TAG,X_lateral + "");
                //计算出总磁感应强度
                rawTotal =
                    sqrt((X_lateral * X_lateral + Y_lateral * Y_lateral + Z_lateral * Z_lateral).toDouble())
                //初始化BigDecimal类
                val total = BigDecimal(rawTotal)
                val res = total.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
                binding.x.text = "$X_lateral μT"
                binding.y.text = "$Y_lateral μT"
                binding.z.text = "$Z_lateral μT"
                binding.high.text = "$res μT"
                val alarmLimStr = "80"
                alarmLim = alarmLimStr.toDouble()
                binding.chart.addPoint(res.toFloat())
                runCatching {
                    binding.chart.setAutoZoomYMax(true)
                }
                if (res < alarmLim) {
                    binding.metal.setTextColor(textColorPrimary()) //设置文字颜色为黑色
                    binding.metal.text = "未探测到金属"
                    binding.desc.animateVisible()
                    val progress = (res / alarmLim * 100).toInt() //计算进度
                    binding.progress.reachBarColor = colorPrimary()
                    binding.progress.progress = progress //进度条
                    binding.progress.textColor = colorPrimary()
                } else {
                    binding.metal.setTextColor(errorColor()) //红色
                    binding.metal.text = "已探测到金属"
                    binding.desc.animateGone()
                    binding.progress.reachBarColor = errorColor()
                    binding.progress.progress = 100 //进度条满
                    binding.progress.textColor = errorColor()
                    //震动
                    vibrate()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    @SuppressLint("MissingPermission")
    private fun vibrate() {
        val vibrator = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
        this.getSystemService(VIBRATOR_SERVICE) //获得 一个震动的服务
        vibrator.vibrate(100)
    }


}
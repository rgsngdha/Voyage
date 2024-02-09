package com.manchuan.tools.activity.life

import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.NetworkUtils.OnNetworkStatusChangedListener
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.interval.Interval
import com.drake.net.utils.runMain
import com.drake.net.utils.scope
import com.dylanc.longan.isFullScreen
import com.dylanc.longan.sp
import com.lxj.androidktx.core.curDay
import com.lxj.androidktx.core.curMonth
import com.lxj.androidktx.core.curYear
import com.lxj.androidktx.core.post
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityTimeBinding
import com.manchuan.tools.extensions.hideSystemBars
import com.manchuan.tools.model.ViewModelTime
import com.manchuan.tools.utils.LunarCalender
import com.manchuan.tools.utils.SettingsLoader
import com.nlf.calendar.Lunar
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import rikka.material.preference.MaterialSwitchPreference
import rikka.preference.SimpleMenuPreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class TimeActivity : BaseActivity() {

    private val netWorkListener: OnNetworkStatusChangedListener? = null
    private var filter: IntentFilter? = null
    private var intents: Intent? = null
    private val binding by lazy {
        ActivityTimeBinding.inflate(layoutInflater)
    }
    private lateinit var interval: Interval
    private var isCharging = false

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme.applyStyle(
            rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true
        )
    }


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        isFullScreen = true
        hideSystemBars()
        initView()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
        timeLays.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        intents = intent
        filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        NetworkUtils.registerNetworkStatusChangedListener(netWorkListener)
        filter = IntentFilter()
        filter!!.addAction(Intent.ACTION_BATTERY_CHANGED)
        val sp = SPUtils.getInstance(MODE_PRIVATE)
        initObserve()
        time_view.setCharacterLists(TickerUtils.provideNumberList())
        time_view.textSize = 128.sp
        time_view.typeface = Typeface.createFromAsset(assets, "fonts/FiraCode-Regular.ttf")
        time_view.animationDuration = 1000L
        time_view.animationInterpolator = SettingsLoader.interpolator
        time_view.gravity = Gravity.CENTER
        time_view.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
        date_view.setCharacterLists(TickerUtils.provideNumberList())
        date_view.textSize = 18.sp
        date_view.animationDuration = 1000L
        date_view.typeface = Typeface.createFromAsset(assets, "fonts/FiraCode-Regular.ttf")
        date_view.animationInterpolator = SettingsLoader.interpolator
        date_view.gravity = Gravity.CENTER_VERTICAL
        date_view.setPreferredScrollingDirection(TickerView.ScrollingDirection.ANY)
        supportFragmentManager.beginTransaction().replace(R.id.settings, TimeFragment()).commit()
        post {
            when {
                sp.getString("darkMode") == "yes" -> {
                    startBackGroundAnimation(time_lay, Color.WHITE, Color.BLACK)
                    changeTickerColor(Color.WHITE, Color.BLACK)
                }

                sp.getString("darkMode") == "no" -> {
                    startBackGroundAnimation(time_lay, Color.BLACK, Color.WHITE)
                    changeTickerColor(Color.BLACK, Color.WHITE)
                }

                else -> {
                    sp.put("darkMode", "yes")
                    startBackGroundAnimation(time_lay, Color.WHITE, Color.BLACK)
                    changeTickerColor(Color.WHITE, Color.BLACK)
                }
            }
        }
        switch_mode.setOnClickListener { view: View? ->
            runMain {
                if (sp.getString("darkMode") == "yes") {
                    //PopTip.show("夜间模式:" + sp.getString("darkMode"));
                    sp.put("darkMode", "no")
                    startBackGroundAnimation(time_lay, Color.BLACK, Color.WHITE)
                    changeTickerColor(Color.BLACK, Color.WHITE)
                } else if (sp.getString("darkMode") == "no") {
                    //PopTip.show("夜间模式:" + sp.getString("darkMode"));
                    sp.put("darkMode", "yes")
                    startBackGroundAnimation(time_lay, Color.WHITE, Color.BLACK)
                    changeTickerColor(Color.WHITE, Color.BLACK)
                }
            }
        }
        settings.setOnClickListener {
            binding.drawerLayout.open()
        }
        interval = Interval(1, TimeUnit.SECONDS)
        interval.subscribe {
            val sdf_date = SimpleDateFormat("yyyy年MM月dd日 ", Locale.CHINA)
            val date = sdf_date.format(Date())
            @SuppressLint("SimpleDateFormat") val sdf_time24 = SimpleDateFormat("HH:mm:ss")
            val time24 = sdf_time24.format(Date())
            val lunar = Lunar.fromDate(Date())
            time_view.text = time24
            date_view.text = TimeUtils.getChineseWeek(Date()) + " " + date + "${
                lunar.monthInChinese
            }月${lunar.dayInChinese} " + LunarCalender().animalsYear(
                curYear
            ) + if (LunarCalender().getFestival(curYear, curMonth, curDay)
                    .isNotBlank()
            ) LunarCalender().getFestival(curYear, curMonth, curDay)
                .isNotBlank() else "" + " " + LunarCalender().getConstellation(
                curMonth, curDay
            )
            if (Global.isTimeScreenVibration) {
                time_view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        interval.cancel()
        NetworkUtils.unregisterNetworkStatusChangedListener(netWorkListener)
    }

    private fun startBackGroundAnimation(bgView: View?, startColor: Int, endColor: Int) {
        //创建动画,这里的关键就是使用ArgbEvaluator, 后面2个参数就是 开始的颜色,和结束的颜色.
        scope {
            @SuppressLint("RestrictedApi") val colorAnimator =
                ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
            colorAnimator.addUpdateListener { animation ->
                val color = animation.animatedValue as Int //之后就可以得到动画的颜色了
                bgView!!.setBackgroundColor(color)
            }
            colorAnimator.duration = 600
            colorAnimator.start()
        }
    }

    private fun changeTickerColor(startColor: Int, endColor: Int) {
        @SuppressLint("RestrictedApi") val colorAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), endColor, startColor)
        colorAnimator.addUpdateListener { animation ->
            val color = animation.animatedValue as Int //之后就可以得到动画的颜色了
            //bgView.setBackgroundColor(color);//设置一下, 就可以看到效果.
            time_view.textColor = color
            date_view.textColor = color
            //textView.setTextColor();
            switch_mode.setColorFilter(color)
            settings.setColorFilter(color)
        }
        colorAnimator.duration = 600
        colorAnimator.start()
    }

    private lateinit var time_lay: RelativeLayout
    private lateinit var switch_mode: ImageView
    private lateinit var time_view: TickerView
    private lateinit var settings: ImageView
    private lateinit var date_view: TickerView
    private lateinit var timeLays: FrameLayout
    private fun initView() {
        time_lay = binding.timeLay
        switch_mode = binding.switchMode
        time_view = binding.timeView
        date_view = binding.dateView
        settings = binding.timeFullscreenExit
        timeLays = binding.timeLays
    }

    class TimeFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(p1: Preference, p2: Any): Boolean {
            return false
        }

        override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {}
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesMode = MODE_PRIVATE
            setPreferencesFromResource(R.xml.time_settings, rootKey)
            val themePreference = findPreference<Preference>("time_style") as SimpleMenuPreference
            val bright =
                findPreference<MaterialSwitchPreference>("bright_key") as MaterialSwitchPreference
            val brightTime =
                findPreference<EditTextPreference>("bright_time_key") as EditTextPreference
            val vibrate = findPreference<MaterialSwitchPreference>("vibrate_key")
            vibrate?.setOnPreferenceChangeListener { preference, newValue ->
                Global.isTimeScreenVibration = newValue as Boolean
                true
            }
            themePreference.summary = themePreference.entry
            brightTime.isVisible = !bright.isChecked
            bright.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    brightTime.isVisible = bright.isChecked
                    true
                }
            themePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    val indexOfValue = themePreference.findIndexOfValue(newValue.toString())
                    themePreference.summary =
                        if (indexOfValue >= 0) themePreference.entries[indexOfValue] else null
                    val interpolator = when (newValue.toString()) {
                        "linear" -> LinearInterpolator()
                        "reduce" -> DecelerateInterpolator()
                        "speed_down" -> AccelerateDecelerateInterpolator()
                        "speed" -> AccelerateInterpolator()
                        "stage" -> BounceInterpolator()
                        "back_forth" -> AnticipateInterpolator()
                        else -> {
                            LinearInterpolator()
                        }
                    }
                    ViewModelTime.interpolator.postValue(interpolator)
                    true
                }
        }
    }

    private fun initObserve() {
        ViewModelTime.interpolator.observe(this) {
            time_view.animationInterpolator = it
            date_view.animationInterpolator = it
        }
    }

}
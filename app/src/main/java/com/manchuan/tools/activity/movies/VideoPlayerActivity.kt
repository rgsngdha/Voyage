package com.manchuan.tools.activity.movies

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.PictureInPictureParams
import android.app.PictureInPictureUiState
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Animatable
import android.media.AudioManager.STREAM_MUSIC
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.GestureDetector
import android.view.KeyEvent.KEYCODE_B
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_N
import android.view.KeyEvent.KEYCODE_SPACE
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.math.MathUtils.clamp
import androidx.core.view.updateLayoutParams
import androidx.databinding.BaseObservable
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.android.cast.dlna.dmc.DLNACastManager
import com.android.cast.dlna.dmc.OnDeviceRegistryListener
import com.android.cast.dlna.dmc.control.DeviceControl
import com.android.cast.dlna.dmc.control.OnDeviceControlListener
import com.bumptech.glide.Glide
import com.crazylegend.kotlinextensions.pip.checkPIPPermissions
import com.crazylegend.viewbinding.viewBinding
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.addModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.engine.utils.GB
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.cache.CacheMode
import com.drake.net.utils.scopeNet
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.activity
import com.dylanc.longan.context
import com.dylanc.longan.doOnClick
import com.dylanc.longan.externalMoviesDirPath
import com.dylanc.longan.getDimension
import com.dylanc.longan.isFullScreen
import com.dylanc.longan.isLandscape
import com.dylanc.longan.isLightStatusBar
import com.dylanc.longan.isWebUrl
import com.dylanc.longan.roundCorners
import com.dylanc.longan.safeIntentExtras
import com.dylanc.longan.startActivity
import com.dylanc.longan.statusBarColor
import com.dylanc.longan.toast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.ui.SubtitleView
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.jeffmony.downloader.VideoDownloadManager
import com.jeffmony.downloader.model.VideoTaskItem
import com.kai.sniffwebkit.sniff.SniffTool
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.flexbox
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.isVisible
import com.lxj.androidktx.core.postDelay
import com.lxj.androidktx.core.visible
import com.lzx.starrysky.cache.ExoCache
import com.lzx.starrysky.cache.ICache
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.database.SourceType
import com.manchuan.tools.activity.movies.database.SubVideoParser
import com.manchuan.tools.activity.movies.database.SubscribeList
import com.manchuan.tools.activity.movies.database.VideoParseType
import com.manchuan.tools.activity.movies.model.MovieCount
import com.manchuan.tools.activity.movies.settings.PlayerSettings
import com.manchuan.tools.activity.movies.settings.UserInterfaceSettings
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityVideoPlayerBinding
import com.manchuan.tools.databinding.ItemDevicesItemsBinding
import com.manchuan.tools.extensions.ExtendedTimeBar
import com.manchuan.tools.extensions.GesturesListener
import com.manchuan.tools.extensions.brightnessConverter
import com.manchuan.tools.extensions.circularReveal
import com.manchuan.tools.extensions.colorPrimaryContainer
import com.manchuan.tools.extensions.dp
import com.manchuan.tools.extensions.getCurrentBrightnessValue
import com.manchuan.tools.extensions.hideSystemBars
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loadData
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.rootView
import com.manchuan.tools.extensions.saveData
import com.manchuan.tools.extensions.showSystemBars
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.tryWith
import com.manchuan.tools.extensions.tryWithSuspend
import com.manchuan.tools.extensions.userAgent
import com.manchuan.tools.helper.ScreenOrientationHelper
import com.manchuan.tools.utils.atLeastR
import com.manchuan.tools.view.CheckableChipView
import com.nowfal.kdroidext.kex.audioManager
import com.nowfal.kdroidext.kex.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.RemoteDevice
import org.seimicrawler.xpath.JXDocument
import java.util.Timer
import java.util.TimerTask
import kotlin.math.min
import kotlin.math.roundToInt


class VideoPlayerActivity : BaseActivity() {

    private var player: ExoPlayer? = null

    companion object {
        lateinit var movieCount: MovieCount
        lateinit var subscribeList: SubscribeList
    }


    private lateinit var deviceControl: DeviceControl

    private lateinit var exoPlay: ImageButton
    private lateinit var exoSource: ImageButton
    private lateinit var exoSettings: ImageButton
    private lateinit var exoSubtitle: ImageButton
    private lateinit var exoSubtitleView: SubtitleView
    private lateinit var exoRotate: ImageButton
    private lateinit var exoQuality: ImageButton
    private lateinit var exoSpeed: ImageButton
    private lateinit var exoScreen: ImageButton
    private lateinit var exoNext: ImageButton
    private lateinit var exoPrev: ImageButton
    private lateinit var exoSkipOpEd: ImageButton
    private lateinit var exoPip: ImageButton
    private lateinit var exoBrightness: Slider
    private lateinit var exoVolume: Slider
    private lateinit var exoBrightnessCont: View
    private lateinit var exoVolumeCont: View
    private lateinit var exoSkip: View
    private lateinit var skipTimeButton: View
    private lateinit var skipTimeText: TextView
    private lateinit var timeStampText: TextView
    private lateinit var animeTitle: TextView
    private lateinit var videoInfo: TextView
    private lateinit var serverInfo: TextView
    private lateinit var episodeTitle: MaterialTextView
    private lateinit var exoBack: ImageButton


    var settings = PlayerSettings()

    private var uiSettings = UserInterfaceSettings()
    private var notchHeight: Int = 0
    private var episodeLength: Float = 0f
    private var isFullscreenState: Int = 0
    private var isInitialized = false
    private var isPlayerPlaying = true
    private var interacted = false

    private lateinit var videoContainerParams: ViewGroup.LayoutParams

    private val player_settings = "player_settings"
    private var pipEnabled = false
    private var orientationListener: OrientationEventListener? = null
    private var movieName = ""


    private val handler = Handler(Looper.getMainLooper())
    private val binding by viewBinding(ActivityVideoPlayerBinding::inflate)

    private val webView by lazy {
        WebView(this)
    }

    private val videoParser by safeIntentExtras<MutableList<SubVideoParser>>("videoParser")

    private var currentSourceName = ""
    private var currentEpisodeName = ""
    private var currentUrl = ""
    private var currentOriginUrl = ""
    private lateinit var link: String
    private var usedDevice: Device<*, *, *>? = null
    private var images = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.sharedElementsUseOverlay = false
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (atLeastR()) {
            window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }
        statusBarColor = Color.BLACK
        isLightStatusBar = false
        settings = loadData("player_settings") ?: PlayerSettings().apply {
            saveData(
                "player_settings", this
            )
        }
        images = safeIntentExtras<String>("image").value
        videoContainerParams = binding.videoContainer.layoutParams
        onBackPressedDispatcher.addCallback(this) {
            if (isFullscreenState == 1) {
                exitFullscreen()
            } else {
                finishAndRemoveTask()
            }
        }
        binding.videoPlayer.apply {
            exoQuality = findViewById(R.id.exo_quality)
            exoPlay = findViewById(com.google.android.exoplayer2.ui.R.id.exo_play)
            exoSource = findViewById(R.id.exo_source)
            exoSettings = findViewById(R.id.exo_settings)
            exoSubtitle = findViewById(R.id.exo_sub)
            exoSubtitleView = findViewById(com.google.android.exoplayer2.ui.R.id.exo_subtitles)
            exoRotate = findViewById(R.id.exo_rotate)
            exoSpeed = findViewById(com.google.android.exoplayer2.ui.R.id.exo_playback_speed)
            exoScreen = findViewById(R.id.exo_screen)
            exoBrightness = findViewById(R.id.exo_brightness)
            exoVolume = findViewById(R.id.exo_volume)
            exoBrightnessCont = findViewById(R.id.exo_brightness_cont)
            exoVolumeCont = findViewById(R.id.exo_volume_cont)
            exoPip = findViewById(R.id.exo_pip)
            exoSkipOpEd = findViewById(R.id.exo_skip_op_ed)
            exoSkip = findViewById(R.id.exo_skip)
            skipTimeButton = findViewById(R.id.exo_skip_timestamp)
            skipTimeText = findViewById(R.id.exo_skip_timestamp_text)
            timeStampText = findViewById(R.id.exo_time_stamp_text)
            animeTitle = findViewById(R.id.exo_anime_title)
            episodeTitle = findViewById(R.id.exo_ep_sel)
            videoInfo = findViewById(R.id.exo_video_info)
            serverInfo = findViewById(R.id.exo_server_info)
            exoBack = findViewById(R.id.exo_back)
            controllerShowTimeoutMs = 5000
        }
        if (Global.isEnabledVideoHDR) {
            window.colorMode = Global.videoColorMode
        }
        if (settings.videoInfo.not()) {
            videoInfo.gone()
            serverInfo.gone()
        }
        if (settings.pip.not()) {
            exoPip.gone()
        }
        timeStampText.text = "请勿相信视频内的任何广告"
        exoSubtitle.gone()
        exoSource.gone()
        episodeTitle.text = safeIntentExtras<String>("name").value
        movieName = safeIntentExtras<String>("name").value
        cache = ExoCache(context, externalMoviesDirPath, 20.GB)

        //返回按钮处理
        exoBack.throttleClick {
            onBackPressedDispatcher.onBackPressed()
        }
        exoScreen.throttleClick {
            autoEnterFullscreen()
        }

        ScreenOrientationHelper.init(this,
            object : ScreenOrientationHelper.ScreenOrientationChangeListener {
                override fun onChange(orientation: Int) {
                    when (orientation) {
                        ScreenOrientationHelper.ORIENTATION_TYPE_0 -> {}
                        ScreenOrientationHelper.ORIENTATION_TYPE_90 -> {}
                        ScreenOrientationHelper.ORIENTATION_TYPE_180 -> {}
                        ScreenOrientationHelper.ORIENTATION_TYPE_270 -> {}
                    }
                }
            })

        // Picture-in-picture
        pipEnabled =
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) && settings.pip
        if (pipEnabled) {
            exoPip.visibility = View.VISIBLE
            exoPip.setOnClickListener {
                checkPIPPermissions(onPermissionDenied = {
                    toast("无画中画权限")
                }, onPermissionGranted = {
                    enterPictureInPictureMode(updatePictureInPictureParams())
                })
            }
        } else exoPip.visibility = View.GONE

        exoPlay.setOnClickListener {
            if (isInitialized) {
                isPlayerPlaying = player?.isPlaying == true
                (exoPlay.drawable as Animatable?)?.start()
                if (isPlayerPlaying) {
                    Glide.with(this).load(R.drawable.anim_play_to_pause).into(exoPlay)
                    player?.pause()
                } else {
                    Glide.with(this).load(R.drawable.anim_pause_to_play).into(exoPlay)
                    player?.play()
                }
            }
        }

        if (settings.skipTime > 0) {
            exoSkip.findViewById<TextView>(R.id.exo_skip_time).text = settings.skipTime.toString()
            exoSkip.setOnClickListener {
                if (isInitialized) player?.seekTo(player?.currentPosition?.plus(settings.skipTime * 1000)!!)
            }
            exoSkip.setOnLongClickListener {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.item_seekbar_dialog)
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                if (settings.skipTime <= 120) {
                    dialog.findViewById<Slider>(R.id.seekbar)?.value = settings.skipTime.toFloat()
                } else {
                    dialog.findViewById<Slider>(R.id.seekbar)?.value = 120f
                }
                dialog.findViewById<Slider>(R.id.seekbar)?.addOnChangeListener { _, value, _ ->
                    settings.skipTime = value.toInt()
                    saveData(player_settings, settings)
                    binding.videoPlayer.findViewById<TextView>(R.id.exo_skip_time).text =
                        settings.skipTime.toString()
                    dialog.findViewById<TextView>(R.id.seekbar_value)?.text =
                        settings.skipTime.toString()
                }
                dialog.findViewById<Slider>(R.id.seekbar)
                    .addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                        override fun onStartTrackingTouch(slider: Slider) {}
                        override fun onStopTrackingTouch(slider: Slider) {
                            dialog.dismiss()
                        }
                    })
                dialog.findViewById<TextView>(R.id.seekbar_title).text = "跳过"
                dialog.findViewById<TextView>(R.id.seekbar_value).text =
                    settings.skipTime.toString()
                dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                dialog.show()
                true
            }
        } else {
            exoSkip.visibility = View.GONE
        }

        //锁定按钮
        var locked = false
        val container = binding.videoPlayer.findViewById<View>(R.id.exo_controller_cont)
        val screen = binding.videoPlayer.findViewById<View>(R.id.exo_black_screen)
        val lockButton = binding.videoPlayer.findViewById<ImageButton>(R.id.exo_unlock)
        val timeline =
            binding.videoPlayer.findViewById<ExtendedTimeBar>(com.google.android.exoplayer2.ui.R.id.exo_progress)
        binding.videoPlayer.findViewById<ImageButton>(R.id.exo_lock).setOnClickListener {
            locked = true
            screen.visibility = View.GONE
            container.visibility = View.GONE
            lockButton.visibility = View.VISIBLE
            timeline.setForceDisabled(true)
        }
        lockButton.setOnClickListener {
            locked = false
            screen.visibility = View.VISIBLE
            container.visibility = View.VISIBLE
            it.visibility = View.GONE
            timeline.setForceDisabled(false)
        }

        val gestureSpeed = (300 * uiSettings.animationSpeed).toLong()
        //播放器 UI 可见性处理程序
        val brightnessRunnable = Runnable {
            if (exoBrightnessCont.alpha == 1f) lifecycleScope.launch {
                ObjectAnimator.ofFloat(exoBrightnessCont, "alpha", 1f, 0f).setDuration(gestureSpeed)
                    .start()
                delay(gestureSpeed)
                exoBrightnessCont.visibility = View.GONE
            }
        }
        val volumeRunnable = Runnable {
            if (exoVolumeCont.alpha == 1f) lifecycleScope.launch {
                ObjectAnimator.ofFloat(exoVolumeCont, "alpha", 1f, 0f).setDuration(gestureSpeed)
                    .start()
                delay(gestureSpeed)
                exoVolumeCont.visibility = View.GONE
            }
        }
        binding.videoPlayer.setControllerVisibilityListener(StyledPlayerView.ControllerVisibilityListener { visibility ->
            if (visibility == View.GONE) {
                //hideSystemBars()
                brightnessRunnable.run()
                volumeRunnable.run()
            }
        })
        val overshoot = AnimationUtils.loadInterpolator(this, R.anim.over_shoot)
        val controllerDuration = (uiSettings.animationSpeed * 200).toLong()
        fun handleController() {
            if (!isInPictureInPictureMode) {
                if (binding.videoPlayer.isControllerFullyVisible) {
                    ObjectAnimator.ofFloat(
                        binding.videoPlayer.findViewById(R.id.exo_controller), "alpha", 1f, 0f
                    ).setDuration(controllerDuration).start()
                    ObjectAnimator.ofFloat(
                        binding.videoPlayer.findViewById(R.id.exo_bottom_cont),
                        "translationY",
                        0f,
                        128f
                    ).apply { interpolator = overshoot;duration = controllerDuration;start() }
                    ObjectAnimator.ofFloat(
                        binding.videoPlayer.findViewById(R.id.exo_timeline_cont),
                        "translationY",
                        0f,
                        128f
                    ).apply { interpolator = overshoot;duration = controllerDuration;start() }
                    ObjectAnimator.ofFloat(
                        binding.videoPlayer.findViewById(R.id.exo_top_cont),
                        "translationY",
                        0f,
                        -128f
                    ).apply { interpolator = overshoot;duration = controllerDuration;start() }
                    binding.videoPlayer.postDelayed(
                        { binding.videoPlayer.hideController() }, controllerDuration
                    )
                } else {
                    binding.videoPlayer.showController()
                    ObjectAnimator.ofFloat(
                        binding.videoPlayer.findViewById(R.id.exo_controller), "alpha", 0f, 1f
                    ).setDuration(controllerDuration).start()
                    ObjectAnimator.ofFloat(
                        binding.videoPlayer.findViewById(R.id.exo_bottom_cont),
                        "translationY",
                        128f,
                        0f
                    ).apply { interpolator = overshoot;duration = controllerDuration;start() }
                    ObjectAnimator.ofFloat(
                        binding.videoPlayer.findViewById(R.id.exo_timeline_cont),
                        "translationY",
                        128f,
                        0f
                    ).apply { interpolator = overshoot;duration = controllerDuration;start() }
                    ObjectAnimator.ofFloat(
                        binding.videoPlayer.findViewById(R.id.exo_top_cont),
                        "translationY",
                        -128f,
                        0f
                    ).apply { interpolator = overshoot;duration = controllerDuration;start() }
                }
            }
        }


        val rewindText = binding.videoPlayer.findViewById<TextView>(R.id.exo_fast_rewind_anim)
        val forwardText = binding.videoPlayer.findViewById<TextView>(R.id.exo_fast_forward_anim)
        val fastForwardCard = binding.videoPlayer.findViewById<View>(R.id.exo_fast_forward)
        val fastRewindCard = binding.videoPlayer.findViewById<View>(R.id.exo_fast_rewind)

        //寻求
        val seekTimerF = ResettableTimer()
        val seekTimerR = ResettableTimer()
        var seekTimesF = 0
        var seekTimesR = 0

        fun seek(forward: Boolean, event: MotionEvent? = null) {
            val views = if (forward) {
                forwardText.text = "+${settings.seekTime * ++seekTimesF}"
                handler.post { player?.seekTo(player?.currentPosition!! + settings.seekTime * 1000) }
                fastForwardCard to forwardText
            } else {
                rewindText.text = "-${settings.seekTime * ++seekTimesR}"
                handler.post { player?.seekTo(player?.currentPosition!! - settings.seekTime * 1000) }
                fastRewindCard to rewindText
            }
            startDoubleTapped(views.first, views.second, event, forward)
            if (forward) {
                seekTimerR.reset(object : TimerTask() {
                    override fun run() {
                        stopDoubleTapped(views.first, views.second)
                        seekTimesF = 0
                    }
                }, 850)
            } else {
                seekTimerF.reset(object : TimerTask() {
                    override fun run() {
                        stopDoubleTapped(views.first, views.second)
                        seekTimesR = 0
                    }
                }, 850)
            }
        }
        if (!settings.doubleTap) {
            binding.videoPlayer.findViewById<View>(R.id.exo_fast_forward_button_cont).visibility =
                View.VISIBLE
            binding.videoPlayer.findViewById<View>(R.id.exo_fast_rewind_button_cont).visibility =
                View.VISIBLE
            binding.videoPlayer.findViewById<ImageButton>(R.id.exo_fast_forward_button)
                .setOnClickListener {
                    if (isInitialized) {
                        seek(true)
                    }
                }
            binding.videoPlayer.findViewById<ImageButton>(R.id.exo_fast_rewind_button)
                .setOnClickListener {
                    if (isInitialized) {
                        seek(false)
                    }
                }
        }

        keyMap[KEYCODE_DPAD_RIGHT] = { seek(true) }
        keyMap[KEYCODE_DPAD_LEFT] = { seek(false) }

        //Screen Gestures
        if (settings.gestures || settings.doubleTap) {

            fun doubleTap(forward: Boolean, event: MotionEvent) {
                if (!locked && isInitialized && settings.doubleTap) {
                    seek(forward, event)
                }
            }

            //Brightness
            var brightnessTimer = Timer()
            exoBrightnessCont.visibility = View.GONE

            fun brightnessHide() {
                brightnessTimer.cancel()
                brightnessTimer.purge()
                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        handler.post(brightnessRunnable)
                    }
                }
                brightnessTimer = Timer()
                brightnessTimer.schedule(timerTask, 3000)
            }
            exoBrightness.value = (getCurrentBrightnessValue(this) * 10f)

            exoBrightness.addOnChangeListener { _, value, _ ->
                val lp = window.attributes
                lp.screenBrightness = brightnessConverter(value / 10, false)
                window.attributes = lp
                brightnessHide()
            }

            //Volume
            var volumeTimer = Timer()
            exoVolumeCont.visibility = View.GONE

            val volumeMax = audioManager?.getStreamMaxVolume(STREAM_MUSIC)
            exoVolume.value =
                audioManager?.getStreamVolume(STREAM_MUSIC)?.toFloat()!! / volumeMax!! * 10
            fun volumeHide() {
                volumeTimer.cancel()
                volumeTimer.purge()
                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        handler.post(volumeRunnable)
                    }
                }
                volumeTimer = Timer()
                volumeTimer.schedule(timerTask, 3000)
            }
            exoVolume.addOnChangeListener { _, value, _ ->
                val volume = (value / 10 * volumeMax).roundToInt()
                audioManager?.setStreamVolume(STREAM_MUSIC, volume, 0)
                volumeHide()
            }

            //FastRewind (Left Panel)
            val fastRewindDetector = GestureDetector(this, object : GesturesListener() {
                override fun onDoubleClick(event: MotionEvent?) {
                    if (event != null) {
                        doubleTap(false, event)
                    }
                }

                override fun onScrollYClick(y: Float) {
                    if (!locked && settings.gestures) {
                        exoBrightness.value = clamp(exoBrightness.value + y / 100, 0f, 10f)
                        if (exoBrightnessCont.visibility != View.VISIBLE) {
                            exoBrightnessCont.visibility = View.VISIBLE
                        }
                        exoBrightnessCont.alpha = 1f
                    }
                }

                override fun onSingleClick(event: MotionEvent?) = handleController()
            })
            val rewindArea = binding.videoPlayer.findViewById<View>(R.id.exo_rewind_area)
            rewindArea.isClickable = true
            rewindArea.setOnTouchListener { v, event ->
                fastRewindDetector.onTouchEvent(event)
                v.performClick()
                true
            }

            //FastForward (Right Panel)
            val fastForwardDetector = GestureDetector(this, object : GesturesListener() {
                override fun onDoubleClick(event: MotionEvent?) {
                    if (event != null) {
                        doubleTap(true, event)
                    }
                }

                override fun onScrollYClick(y: Float) {
                    if (!locked && settings.gestures) {
                        exoVolume.value = clamp(exoVolume.value + y / 100, 0f, 10f)
                        if (exoVolumeCont.visibility != View.VISIBLE) {
                            exoVolumeCont.visibility = View.VISIBLE
                        }
                        exoVolumeCont.alpha = 1f
                    }
                }

                override fun onSingleClick(event: MotionEvent?) = handleController()
            })
            val forwardArea = binding.videoPlayer.findViewById<View>(R.id.exo_forward_area)
            forwardArea.isClickable = true
            forwardArea.setOnTouchListener { v, event ->
                fastForwardDetector.onTouchEvent(event)
                v.performClick()
                true
            }
        }

        binding.sourceList.linear(LinearLayoutManager.HORIZONTAL).divider {
            orientation = DividerOrientation.VERTICAL
            setDivider(6, true)
        }.setup {
            addType<SourcesCount>(R.layout.item_sources)
            setAnimation(AnimationType.ALPHA)
            onClick(R.id.chip) {
                val view = this.itemView as MaterialCardView
                var checked = getModel<SourcesCount>().checked
                if (it == R.id.chip) checked = !checked
                if (checkedCount == 1) {
                    setChecked(adapterPosition, true)
                    view.isChecked = true
                }
            }
            onChecked { position, isChecked, allChecked ->
                val model = getModel<SourcesCount>(position)
                if (isChecked) {
                    binding.countRecycler.models = countList.filter { it.tile.contains(model.name) }
                    binding.countRecycler.bindingAdapter.setChecked(0, true)
                    currentSourceName = model.name
                    serverInfo.text = model.name
                }
                model.checked = isChecked
                model.notifyChange()
            }
        }
        binding.countRecycler.flexbox().setup {
            addType<MovieListCount>(R.layout.item_movies_count)
            setAnimation(AnimationType.ALPHA)
            onClick(R.id.chip) {
                val view = this.itemView as CheckableChipView
                var checked = getModel<MovieListCount>().checked
                if (it == R.id.chip) checked = !checked
                if (checkedCount == 1) {
                    setChecked(adapterPosition, true)
                    view.isChecked = true
                }
            }
            onChecked { position, isChecked, allChecked ->
                val model = getModel<MovieListCount>(position)
                if (isChecked) {
                    play(model.url)
                    currentEpisodeName = model.name
                    currentOriginUrl = model.url
                    animeTitle.text = model.name
                }
                model.checked = isChecked
                model.notifyChange()
            }
        }.models = countList
        binding.cast.doOnClick {
            if (currentUrl.isNotEmpty()) {
                if (binding.detailsLay.isVisible) {
                    binding.detailsLay.animateGone()
                    binding.dlna.rootLay.animateVisible()
                }
            } else {
                snack("无法获取当前播放链接")
            }
        }
        binding.countRecycler.bindingAdapter.apply {
            singleMode = true
            toggle()
        }
        binding.sourceList.bindingAdapter.apply {
            singleMode = true
            toggle()
        }
        binding.question.doOnClick {

        }
        binding.download.doOnClick {
            if (currentEpisodeName.isNotEmpty() and currentSourceName.isNotEmpty() and currentUrl.isNotEmpty()) {
                val videoItem = VideoTaskItem(
                    currentUrl,
                    images,
                    "$movieName—$currentEpisodeName—$currentSourceName",
                    "DownloadMovie"
                )
                VideoDownloadManager.getInstance().startDownload(videoItem);
                snack("已添加到下载任务列表", "查看") {
                    startActivity<MovieDownloadActivity>()
                }
            } else {
                snack("下载失败")
            }
        }
        binding.dlna.dlnaRecycler.linear().setup {
            addType<Device<*, *, *>>(R.layout.item_devices_items)
            addType<RemoteDevice>(R.layout.item_devices_items)
            setAnimation(AnimationType.ALPHA)
            onBind {
                val binding = ItemDevicesItemsBinding.bind(itemView)
                val model = getModel<Device<*, *, *>>()
                binding.name.text(model.details.friendlyName)
                binding.type.text(model.type.type)
            }
            R.id.item.onClick {
                val model = getModel<Device<*, *, *>>()
                runCatching {
                    deviceControl =
                        DLNACastManager.connectDevice(model, object : OnDeviceControlListener {
                            override fun onConnected(device: Device<*, *, *>) {
                                toast("成功连接: ${device.details.friendlyName}")
                                deviceControl.setAVTransportURI(currentUrl, currentEpisodeName)
                                //Toast.makeText(requireContext(), "成功连接: ${device.details.friendlyName}", Toast.LENGTH_SHORT).show()
                            }

                            override fun onDisconnected(device: Device<*, *, *>) {
                                toast("无法连接: ${device.details.friendlyName}")
                            }
                        })
                }.onFailure {
                    binding.dlna.dlnaRecycler.postDelayed({
                        runCatching {}.onFailure {
                            snack("投屏初始化未完成，请稍候重试")
                        }
                    }, 500)
                }
            }
        }
        usedDevice = null
        castListener = object : OnDeviceRegistryListener {

            override fun onDeviceAdded(device: Device<*, *, *>) {
                loge(tag = "设备添加", device)
                runOnUiThread {
                    binding.dlna.dlnaRecycler.addModels(listOf(device))
                }
            }

            override fun onDeviceRemoved(device: Device<*, *, *>) {
                runCatching {
                    binding.dlna.dlnaRecycler.mutable.removeAt(
                        binding.dlna.dlnaRecycler.mutable.indexOf(
                            device
                        )
                    )
                    runOnUiThread {
                        binding.dlna.dlnaRecycler.adapter?.notifyItemRemoved(
                            binding.dlna.dlnaRecycler.mutable.indexOf(
                                device
                            )
                        )
                    }
                }
            }

        }
        DLNACastManager.registerDeviceListener(castListener);
        binding.dlna.close.doOnClick {
            if (binding.dlna.rootLay.isVisible) {
                binding.dlna.rootLay.animateGone()
                binding.detailsLay.animateVisible()
            }
        }
        binding.dlna.close.doOnClick {
            if (binding.dlna.rootLay.isVisible) {
                binding.dlna.rootLay.animateGone()
                binding.detailsLay.animateVisible()
            }
        }
        binding.source.setBackgroundColor(colorPrimaryContainer())
        binding.info.setBackgroundColor(colorPrimaryContainer())
        binding.source.roundCorners = 4.dp.toFloat()
        binding.info.roundCorners = 4.dp.toFloat()
        binding.avatar.load(
            safeIntentExtras<String>("image").value,
            placeholder = R.drawable.placeholder,
            isCrossFade = true,
            isForceOriginalSize = true
        )
        postDelay(200) {
            binding.source.text("源:${safeIntentExtras<String>("category").value}")
            binding.name.text(safeIntentExtras<String>("name").value)
        }
        when (safeIntentExtras<SourceType>("sourcesType").value) {
            SourceType.SITE -> {
                val url = safeIntentExtras<String>("url")
                loge(url.value)
                scopeNetLife {
                    val string = Get<String>(url.value) {
                        addHeader("User-Agent", subscribeList.searchUa.ifEmpty { userAgent() })
                    }.await()
                    //loge(string)
                    val document = JXDocument.create(string)
                    val lines = document.selN(subscribeList.lineNameRule)
                    val list = document.selN(subscribeList.listRule)
                    val sources = arrayListOf<String>().apply {
                        clear()
                    }
                    lines.indices.forEach {
                        sources.add(
                            tryWithSuspend(call = { lines[it].asString() },
                                failed = { "" }).toString()
                        )
                        binding.sourceList.addModels(
                            listOf(
                                SourcesCount(
                                    tryWithSuspend(call = { lines[it].asString() },
                                        failed = { "" }).toString()
                                )
                            )
                        )
                        val count = JXDocument.create(
                            tryWithSuspend(failed = { "" },
                                call = { list[it].asString() })
                        )
                        val counts = count.selN(subscribeList.episodeNameRule)
                        val countsUrl = count.selN(subscribeList.videoUrlRule)
                        counts.indices.forEach { countInt ->
                            val videoUrl = if (!countsUrl[countInt].asString()
                                    .isWebUrl()
                            ) subscribeList.searchUrl.ifEmpty { subscribeList.searchApi } + countsUrl[countInt].asString() else countsUrl[countInt].asString()
                            countList.add(
                                MovieListCount(
                                    lines[it].asString(), counts[countInt].asString(), videoUrl
                                )
                            )
                            loge(
                                tag = "归属线路:${lines[it].asString()}",
                                message = "集数名:${counts[countInt].asString()},链接:${videoUrl}"
                            )
                        }
                    }
                    binding.countRecycler.models = countList
                    runCatching {
                        currentSourceName = sources.first()
                        binding.sourceList.bindingAdapter.setChecked(0, true)
                    }.onFailure {
                        toast("选集失败")
                    }
                }.catch {
                    it.printStackTrace()
                    loge(it)
                    toast("失败 ${it.message}")
                    finishAfterTransition()
                }
            }

            SourceType.COLLECTION -> {
                val searchUrl = safeIntentExtras<String>("searchUrl").value
                val json = JSON.parseObject(safeIntentExtras<String>("jsonString").value)
                when {
                    searchUrl.contains("http://api.kunyu77.com/api.php/") -> {
                        link = "${searchUrl}videoPlaylist?ids=${json.getString("vod_id")}"
                    }

                    searchUrl.contains("api.php/app") -> {
                        link = "${searchUrl}video_detail?id=${json.getString("vod_id")}"
                    }

                    searchUrl.contains("php/provide/vod") -> {
                        link = "${searchUrl}?ac=details&idd=${json.getString("vod_id")}"
                    }

                    searchUrl.contains("php/v1.vod") or searchUrl.contains("php/m2.vod") -> {
                        link = "${searchUrl}/detail?vod_id=${json.getString("vod_id")}"
                    }
                }
                val sourceLink = json.getString("vod_play_url")
                val links = sourceLink.split("$$$")
                val selectForm = json.getString("vod_play_from")
                val formatForm = selectForm.split("$$$")
                val sources = arrayListOf<String>().apply {
                    clear()
                }
                links.indices.forEach { i ->
                    sources.add(formatForm[i])
                    binding.sourceList.addModels(listOf(SourcesCount(name = formatForm[i])))
                    val name = formatForm[i]
                    links[i].split("#").forEach {
                        val con = it.split("$")
                        runCatching {
                            countList.add(MovieListCount(name, con.first(), con[1]))
                        }.onFailure {
                            snack("剧集添加失败")
                        }
                    }
                }
                runCatching {
                    currentSourceName = sources.first()
                    binding.sourceList.bindingAdapter.setChecked(0, true)
                }.onFailure {
                    toast("自动选集失败")
                }
            }

            else -> {
                snack("无法读取影片")
            }
        }


        //画中画
        // Listener is called immediately after the user exits PiP but before animating.
        binding.videoPlayer.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom) {
                // The playerView's bounds changed, update the source hint rect to
                // reflect its new bounds.
                runCatching {
                    val aspectRatio =
                        Rational(binding.videoPlayer.width, binding.videoPlayer.height)
                    val sourceRectHint = Rect()
                    binding.videoPlayer.getGlobalVisibleRect(sourceRectHint)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setPictureInPictureParams(
                            PictureInPictureParams.Builder().setAspectRatio(aspectRatio)
                                .setSourceRectHint(sourceRectHint).build()
                        )
                    } else {
                        setPictureInPictureParams(
                            PictureInPictureParams.Builder().setAspectRatio(aspectRatio)
                                .setSourceRectHint(sourceRectHint).build()
                        )
                    }
                }
            }
        }
    }

    private fun updatePictureInPictureParams(): PictureInPictureParams {
        // Calculate the aspect ratio of the PiP screen.
        val aspectRatio = Rational(16, 9)
        // The movie view turns into the picture-in-picture mode.
        val visibleRect = Rect()
        binding.videoPlayer.getGlobalVisibleRect(visibleRect)
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PictureInPictureParams.Builder().setAspectRatio(aspectRatio)
                // Specify the portion of the screen that turns into the picture-in-picture mode.
                // This makes the transition animation smoother.
                .setSourceRectHint(visibleRect).build()
            // The screen automatically turns into the picture-in-picture mode when it is hidden
            // by the "Home" button..build()
        } else {
            PictureInPictureParams.Builder().setAspectRatio(aspectRatio)
                // Specify the portion of the screen that turns into the picture-in-picture mode.
                // This makes the transition animation smoother.
                .setSourceRectHint(visibleRect)
                // The screen automatically turns into the picture-in-picture mode when it is hidden
                // by the "Home" button.
                .build()
        }
        setPictureInPictureParams(params)
        return params
    }

    private val countList = mutableListOf<MovieListCount>()


    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        webView.destroy()
        DLNACastManager.unbindCastService(this)
        //DLNACastManager.getInstance().unregisterListener(castListener)
        runCatching {
            SniffTool.destoryTool()
        }
    }

    private var isJiexied = false

    @SuppressLint("SetJavaScriptEnabled")
    private fun play(playUrl: String) {
        loge("播放Url", playUrl)
        isJiexied = false
        isAutoSkipped = false
        isSniffer = false
        player?.release()
        player = null
        binding.loadingView.visible()
        binding.retryView.gone()
        binding.progress.show()
        when {
            playUrl.contains(
                ".mp4", true
            ) or playUrl.contains(".m3u8", true) or playUrl.contains(
                ".mkv", true
            ) or playUrl.contains(".mov") or playUrl.contains(
                ".flv", true
            ) or playUrl.contains(
                ".avi", true
            ) -> {
                binding.loadingView.gone()
                binding.progress.show()
                binding.retryView.gone()
                currentUrl = playUrl
                initializePlayer(playUrl)
            }

            playUrl.isWebUrl().not() -> {
                if (videoParser.isNotEmpty()) {
                    if (videoParser.any { it.name.equals(currentSourceName, true) }) {
                        videoParser.filter { it.name.equals(currentSourceName, true) }
                            .forEach { parser ->
                                parser.videoParse.forEach { parsers ->
                                    when (parsers.type) {
                                        VideoParseType.JSON -> {
                                            scopeNet {
                                                val subVideoParser =
                                                    Get<String>("${parsers.url}$playUrl") {
                                                        setCacheMode(CacheMode.READ_THEN_REQUEST)
                                                        setHeader("User-Agent",
                                                            parsers.ua.ifBlank { userAgent() })
                                                    }.await()
                                                val json = JSON.parseObject(subVideoParser)
                                                if (json.getIntValue("code") == 200) {
                                                    binding.loadingView.gone()
                                                    binding.progress.hide()
                                                    currentUrl = json.getString("url")
                                                    isJiexied = true
                                                    if (isJiexied) {
                                                        initializePlayer(json.getString("url"))
                                                    }
                                                } else {
                                                    isJiexied = false
                                                    toast(
                                                        tryWith(call = { json.getString("msg") },
                                                            failed = { "解析失败" })
                                                    )
                                                }
                                            }.catch {

                                            }
                                        }

                                        VideoParseType.SNIFFING -> {
                                            webViewSniffer("${parsers.url}$playUrl")
                                        }
                                    }
                                }
                            }
                    } else {
                        binding.loadingView.visible()
                        binding.progress.gone()
                        binding.retryView.visible()
                        binding.retry.throttleClick {
                            snack("不是有效的播放地址，请尝试更换播放源")
                        }
                        binding.retry.performClick()
                    }
                }
            }

            else -> {
                if (videoParser.isNotEmpty()) {
                    if (videoParser.any { it.name.equals(currentSourceName, true) }) {
                        videoParser.filter { it.name.equals(currentSourceName, true) }
                            .forEach { parser ->
                                parser.videoParse.forEach { parsers ->
                                    when (parsers.type) {
                                        VideoParseType.JSON -> {
                                            scopeNet {
                                                val subVideoParser =
                                                    Get<String>("${parsers.url}$playUrl") {
                                                        setCacheMode(CacheMode.READ_THEN_REQUEST)
                                                        setHeader("User-Agent",
                                                            parsers.ua.ifBlank { userAgent() })
                                                    }.await()
                                                val json = JSON.parseObject(subVideoParser)
                                                if (json.getIntValue("code") == 200) {
                                                    binding.loadingView.gone()
                                                    binding.progress.hide()
                                                    currentUrl = json.getString("url")
                                                    isJiexied = true
                                                    if (isJiexied) {
                                                        initializePlayer(json.getString("url"))
                                                    }
                                                } else {
                                                    isJiexied = false
                                                    toast(
                                                        tryWith(call = { json.getString("msg") },
                                                            failed = { "解析失败" })
                                                    )
                                                }
                                            }.catch {

                                            }
                                        }

                                        VideoParseType.SNIFFING -> {
                                            webViewSniffer("${parsers.url}$playUrl")
                                        }
                                    }
                                }
                            }
                    } else {
                        webViewSniffer(playUrl)
                    }
                } else {
                    webViewSniffer(playUrl)
                }
            }
        }
    }

    private var isNeedSniffing = true

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewSniffer(url: String) {

        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.javaScriptEnabled = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?,
            ) {
                super.onReceivedSslError(view, handler, error)
                handler?.proceed()
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                url?.let { media ->
                    loge(tag = "嗅探", media)
                    val kv: HashMap<String, String> = HashMap()
                    @Suppress("DEPRECATED_IDENTITY_EQUALS") if (media.indexOf(".css") === -1 || media.indexOf(
                            ".gif"
                        ) === -1 || media.indexOf(
                            ".log"
                        ) === -1 || media.indexOf(
                            ".png"
                        ) === -1 || media.indexOf(
                            ".js"
                        ) === -1
                    ) {
                        kv["url"] = media
                        URLArrayList.add(0, kv)
                        if (media.indexOf(
                                ".m4a", ignoreCase = true
                            ) !== -1 || media.indexOf(
                                ".mp4", ignoreCase = true
                            ) !== -1 || media.indexOf(
                                ".mkv", ignoreCase = true
                            ) !== -1 || media.indexOf(
                                ".flv", ignoreCase = true
                            ) !== -1 || media.indexOf(
                                ".m3u8", ignoreCase = true
                            ) !== -1 || media.indexOf(
                                ".m3u", ignoreCase = true
                            ) !== -1
                        ) {
                            isJiexied = true
                            webView.stopLoading()
                            if (!isSniffer) {
                                isSniffer = true
                                loge(tag = "媒体嗅探", media)
                                binding.loadingView.gone()
                                currentUrl = media
                                initializePlayer(media)
                            }
                        }
                    }
                }
            }

        }
        webView.loadUrl(url)
    }


    private var URLArrayList: ArrayList<Map<String, String>> = ArrayList()

    private var isSniffer = false


    override fun onStart() {
        super.onStart()
        DLNACastManager.bindCastService(this)
    }

    override fun onStop() {
        super.onStop()
    }

    private lateinit var castListener: OnDeviceRegistryListener

    //双击动画
    private fun startDoubleTapped(
        v: View,
        text: TextView,
        event: MotionEvent? = null,
        forward: Boolean,
    ) {
        ObjectAnimator.ofFloat(text, "alpha", 1f, 1f).setDuration(600).start()
        ObjectAnimator.ofFloat(text, "alpha", 0f, 1f).setDuration(150).start()

        (text.compoundDrawables[1] as Animatable).apply {
            if (!isRunning) start()
        }

        if (event != null) {
            binding.videoPlayer.hideController()
            v.circularReveal(event.x.toInt(), event.y.toInt(), !forward, 800)
            ObjectAnimator.ofFloat(v, "alpha", 1f, 1f).setDuration(800).start()
            ObjectAnimator.ofFloat(v, "alpha", 0f, 1f).setDuration(300).start()
        }
    }

    private fun stopDoubleTapped(v: View, text: TextView) {
        handler.post {
            ObjectAnimator.ofFloat(v, "alpha", v.alpha, 0f).setDuration(150).start()
            ObjectAnimator.ofFloat(text, "alpha", 1f, 0f).setDuration(150).start()
        }
    }


    private fun onPiPChanged(isInPictureInPictureMode: Boolean) {
        binding.videoPlayer.useController = !isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            orientationListener?.disable()
            binding.content.gone()
        } else {
            orientationListener?.enable()
            binding.content.visible()
        }
        if (isInitialized) {
            //saveData("${media.id}_${episode.number}", player?.currentPosition, this)
            player?.play()
        }
    }

    override fun onPictureInPictureUiStateChanged(pipState: PictureInPictureUiState) {
        super.onPictureInPictureUiStateChanged(pipState)
        onPiPChanged(isInPictureInPictureMode)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        onPiPChanged(isInPictureInPictureMode)
    }

    private var isAutoSkipped = false

    private val eventListener by lazy { ExoPlayerEventListener() }

    private inner class ExoPlayerEventListener : Player.Listener {

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)
            videoInfo.text = "${videoSize.width}x${videoSize.height}"
        }


        private var isBuffering = true
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_READY) {
                if (episodeLength == 0f) {
                    episodeLength = player?.duration!!.toFloat()
                }
                if (settings.autoFullscreen) {
                    if (isFullscreenState == 0) {
                        enterFullscreen()
                    }
                }
                if (settings.autoSkip) {
                    if (!isAutoSkipped) {
                        player?.seekTo(settings.autoSkipTime * 1000L)
                        isAutoSkipped = true
                    }
                }
            }
            isBuffering = playbackState == Player.STATE_BUFFERING
            if (playbackState == Player.STATE_ENDED && settings.autoPlay) {
                if (interacted) exoNext.performClick()
                else toast("Autoplay cancelled, no Interaction for more than 1 Hour.")
            }
            super.onPlaybackStateChanged(playbackState)
        }

        override fun onPlayerError(error: PlaybackException) {
            error.printStackTrace()
            val what: String? = error.message
            toast(what)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (!isBuffering) {
                isPlayerPlaying = isPlaying
                binding.videoPlayer.keepScreenOn = isPlaying
                (exoPlay.drawable as Animatable?)?.start()
                if (!activity.isDestroyed) Glide.with(context)
                    .load(if (isPlaying) R.drawable.anim_play_to_pause else R.drawable.anim_pause_to_play)
                    .into(exoPlay)
            }
        }


    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player?.isPlaying == true) {
            enterPictureInPictureMode(updatePictureInPictureParams())
        }
    }

    override fun onAttachedToWindow() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            val displayCutout = window.decorView.rootWindowInsets.displayCutout
            if (displayCutout != null) {
                if (displayCutout.boundingRects.size > 0) {
                    notchHeight = min(
                        displayCutout.boundingRects[0].width(),
                        displayCutout.boundingRects[0].height()
                    )
                }
            }
        }
        super.onAttachedToWindow()
    }

    private lateinit var cache: ICache


    //智能进入全屏
    private fun autoEnterFullscreen() {
        if (isFullscreenState == 0) {
            enterFullscreen()
        } else {
            exitFullscreen()
        }
    }

    //进入全屏
    private fun enterFullscreen() {
        isLandscape = true
        isFullScreen = true
        isFullscreenState = 1
        hideSystemBars()
        binding.videoPlayer.resizeMode =
            if (player?.videoSize?.height!! > player?.videoSize?.width!!) AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH else AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
        exoScreen.setImageResource(R.drawable.round_fullscreen_exit_24)
        val fullParams = binding.videoContainer.layoutParams
        fullParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        fullParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        binding.videoContainer.layoutParams = fullParams
        if (atLeastR()) {
            rootView.setOnApplyWindowInsetsListener { v, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    insets.displayCutout?.apply {
                        binding.videoPlayer.findViewById<View>(R.id.exo_controller_margin)
                            .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                topMargin = 0
                                marginStart = safeInsetLeft
                                marginEnd = 0
                            }
                        binding.videoPlayer.findViewById<CircularProgressIndicator>(com.google.android.exoplayer2.ui.R.id.exo_buffering)
                            .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                topMargin = safeInsetTop
                                marginStart = 0
                                marginEnd = safeInsetRight
                                bottomMargin = safeInsetBottom
                            }
                        binding.videoContainer.apply {
                            // 设置padding，防止内容显示到非安全区域
                            setPadding(
                                0, safeInsetTop, safeInsetRight, safeInsetBottom
                            )
                        }
                    }
                }
                // 不消费，直接返回原始对象
                insets
            }
        }
    }

    //退出全屏
    private fun exitFullscreen() {
        showSystemBars()
        isFullscreenState = 0
        isLandscape = false
        isFullScreen = false
        binding.videoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        exoScreen.setImageResource(R.drawable.ic_round_fullscreen_24)
        val originParams = binding.videoContainer.layoutParams
        originParams.height = getDimension(com.nowfal.kdroidext.R.dimen._248mdp).toInt()
        originParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        binding.videoContainer.layoutParams = originParams
        // 如果最低版本支持小于20，需要增加判断，防止在低版本系统运行时找不到系统API崩溃
        binding.videoPlayer.findViewById<View>(R.id.exo_controller_margin)
            .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = 0
                marginStart = 0
                marginEnd = 0
            }
        if (atLeastR()) {
            rootView.setOnApplyWindowInsetsListener { v, insets ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    insets.displayCutout?.apply {
                        binding.videoContainer.apply {
                            // 设置padding，防止内容显示到非安全区域
                            setPadding(
                                safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom
                            )
                        }
                        binding.videoPlayer.findViewById<CircularProgressIndicator>(com.google.android.exoplayer2.ui.R.id.exo_buffering)
                            .updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                topMargin = 0
                                marginStart = 0
                                marginEnd = 0
                            }
                    }
                }
                // 不消费，直接返回原始对象
                insets
            }
        }
    }

    private fun initializePlayer(source: String) {
        val mediaSource = if (source.contains(".m3u8", true) or source.contains("m3u", true)) {
            MediaItem.Builder().setUri(Uri.parse(source)).setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
        } else {
            MediaItem.Builder().setUri(Uri.parse(source)).setMimeType(MimeTypes.APPLICATION_MP4)
                .build()
        }
        player = ExoPlayer.Builder(this).build()
        player?.setMediaItem(mediaSource)
        binding.videoPlayer.player = player
        player?.addListener(eventListener)
        player?.prepare()
        player?.playWhenReady = true
        isInitialized = true
    }

    private val keyMap: MutableMap<Int, (() -> Unit)?> = mutableMapOf(KEYCODE_DPAD_RIGHT to null,
        KEYCODE_DPAD_LEFT to null,
        KEYCODE_SPACE to { exoPlay.performClick() },
        KEYCODE_N to { exoNext.performClick() },
        KEYCODE_B to { exoPrev.performClick() })
}

/**
 * @param tile 归属路线
 * @param name 集数名
 * @param url 播放路线
 * @param checked 是否默认选中
 */
data class MovieListCount(
    val tile: String,
    val name: String,
    val url: String,
    var checked: Boolean = false,
) : BaseObservable()

/**
 * @param name 线路名
 * @param checked 是否默认选中
 */
data class SourcesCount(
    val name: String,
    var checked: Boolean = false,
) : BaseObservable()
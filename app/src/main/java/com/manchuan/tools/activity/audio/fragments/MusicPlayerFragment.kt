package com.manchuan.tools.activity.audio.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.dirror.lyricviewx.LyricEntry
import com.drake.channel.sendEvent
import com.drake.engine.base.EngineFragment
import com.drake.engine.utils.throttleClick
import com.drake.serialize.serialize.serialLazy
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.addStatusBarHeightToPaddingTop
import com.dylanc.longan.grantReadUriPermission
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isLightStatusBar
import com.dylanc.longan.isXiaomiRom
import com.dylanc.longan.toast
import com.litao.slider.NiftySlider
import com.litao.slider.effect.AnimationEffect
import com.lxj.androidktx.core.dp
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.visible
import com.lxj.androidktx.snackbar
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.utils.formatTime
import com.manchuan.tools.R
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.FragmentMusicPlayerBinding
import com.manchuan.tools.extensions.addAlpha
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.colorPrimaryInverse
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.glideDrawable
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.singleChoiceSelector
import com.manchuan.tools.extensions.text
import com.manchuan.tools.utils.StackBlurUtils
import com.maxkeppeler.sheets.time.TimeFormat
import com.maxkeppeler.sheets.time.TimeSheet
import java.util.concurrent.TimeUnit


class MusicPlayerFragment :
    EngineFragment<FragmentMusicPlayerBinding>(R.layout.fragment_music_player) {
    override fun initData() {
        runCatching {
            val songInfo = StarrySky.with().getNowPlayingSongInfo()
            binding.lyric.loadLyric(emptyList())
            val lyricEntry = arrayListOf<LyricEntry>()
            songInfo?.lrclist?.forEach {
                lyricEntry.add(
                    LyricEntry(
                        (it.time.toDouble() * 1000L).toLong(), it.lineLyric
                    )
                )
            }
            songInfo?.apply {
                if (translateLyric?.isNotBlank() == true) {
                    binding.lyric.loadLyric(lyric, translateLyric)
                } else {
                    binding.lyric.loadLyric(lyric)
                }
            }
        }
    }

    private val MIUI_SYSTEMUI_PLUGIN: String = "miui.systemui.plugin"

    private fun refreshColor(@ColorInt color: Int) {
        binding.title.setTextColor(color)
        binding.sub.setTextColor(color)
        binding.currentTime.setTextColor(color)
        binding.totalTime.setTextColor(color)
        binding.play.setColorFilter(color)
        binding.previous.iconTint = ColorStateList.valueOf(color)
        binding.next.iconTint = ColorStateList.valueOf(color)
        binding.shuffle.setColorFilter(color)
        binding.snooze.setColorFilter(color)
        binding.playList.setColorFilter(color)
        binding.speed.setColorFilter(color)
        binding.miPlay.setColorFilter(color)
        when (color) {
            Color.BLACK -> {
                isLightStatusBar = true
            }

            Color.WHITE -> {
                isLightStatusBar = false
            }
        }
    }


    override fun initView() {
        binding.rootLay.setBackgroundColor(
            if (isAppDarkMode) Color.parseColor("#ff212121")
                .addAlpha(0.9f) else Color.WHITE.addAlpha(
                0.9f
            )
        )
        binding.moreAction.addNavigationBarHeightToMarginBottom()
        binding.titleLay.addStatusBarHeightToPaddingTop()
        binding.miPlay.addStatusBarHeightToMarginTop()
        enableTransitionTypes(binding.titleLay, binding.rootLay)
        if (isXiaomiRom) {
            binding.miPlay.visible()
        } else {
            binding.miPlay.gone()
        }
        with(binding) {
            //thumb为半透明颜色时可能会将track背景映射出来,需要按需根据自己的背景对颜色进行下转换
            val thumbColor = ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(Color.WHITE, 0x55), Color.BLACK
            )
            val trackColor = ColorUtils.setAlphaComponent(colorPrimary(), 0x33)
            val inactiveColor = ColorUtils.setAlphaComponent(colorPrimary(), 0x11)

            val animEffect = AnimationEffect(seekBar).apply {
                srcTrackHeight = 3.dp
                srcThumbHeight = 6.dp
                srcThumbWidth = 6.dp
                srcThumbRadius = 3.dp
                srcThumbColor = thumbColor
                srcTrackColor = trackColor
                srcInactiveTrackColor = inactiveColor

                targetTrackHeight = 12.dp
                targetThumbHeight = 16.dp
                targetThumbWidth = 8.dp
                targetThumbRadius = 5.dp
                targetThumbColor = Color.WHITE
                targetTrackColor = ColorUtils.setAlphaComponent(colorPrimary(), 0xDD)
                targetInactiveTrackColor = ColorUtils.setAlphaComponent(colorPrimary(), 0x33)

                animationListener = object : AnimationEffect.OnAnimationChangeListener {
                    override fun onEnd(slider: NiftySlider) {
//                        Toast.makeText(requireContext(), "do something on animation end", Toast.LENGTH_SHORT).show()
                    }
                }
                setInterpolator(FastOutLinearInInterpolator())
            }

            seekBar.apply {
                effect = animEffect
                setTrackTintList(ColorStateList.valueOf(trackColor))
                setTrackInactiveTintList(ColorStateList.valueOf(inactiveColor))
                setThumbTintList(ColorStateList.valueOf(thumbColor))
                setThumbShadowColor(Color.BLACK)
            }
        }
        binding.miPlay.throttleClick {
            runCatching {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                val cn = ComponentName(
                    MIUI_SYSTEMUI_PLUGIN, "miui.systemui.miplay.MiPlayDetailActivity"
                )
                intent.component = cn
                intent.grantReadUriPermission()
                startActivity(intent)
            }.onFailure {
                toast("启动小米妙享中心失败")
            }
        }
        binding.lyric.setIsDrawTranslation(true)
        binding.lyric.setNormalTextSize(60f)
        binding.lyric.setCurrentTextSize(75f)
        binding.lyric.setTranslateTextScaleValue(0.5f)
        binding.lyric.setHorizontalOffsetPercent(0.4f)
        binding.lyric.setItemOffsetPercent(0f)
        binding.lyric.setIsEnableBlurEffect(false)
        binding.lyric.apply {
            setNormalColor(if (isAppDarkMode) colorPrimary() else colorPrimaryInverse())
            setCurrentColor(if (isAppDarkMode) colorPrimaryInverse() else colorPrimary())
        }
        binding.playLay.setOnClickListener {
            sendEvent(1, "change_page")
        }
        binding.playList.throttleClick {
            sendEvent("", "open_drawer")
        }
        refreshColor(if (isAppDarkMode) Color.WHITE else Color.BLACK)
        val repeatMode = StarrySky.with().getRepeatMode()
        when (repeatMode.repeatMode) {
            RepeatMode.REPEAT_MODE_NONE -> binding.shuffle.setImageResource(R.drawable.ic_baseline_repeat_24)
            RepeatMode.REPEAT_MODE_ONE -> binding.shuffle.setImageResource(R.drawable.ic_baseline_repeat_one_24)
            RepeatMode.REPEAT_MODE_SHUFFLE -> binding.shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
        }
        binding.shuffle.setOnClickListener {
            val model = StarrySky.with().getRepeatMode()
            when (model.repeatMode) {
                RepeatMode.REPEAT_MODE_NONE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, true)
                    Global.repeatMode = RepeatMode.REPEAT_MODE_ONE
                    binding.shuffle.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                    //snack("单曲循环")
                }

                RepeatMode.REPEAT_MODE_ONE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_SHUFFLE, false)
                    Global.repeatMode = RepeatMode.REPEAT_MODE_SHUFFLE
                    binding.shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
                    //snack("随机播放")
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, true)
                    Global.repeatMode = RepeatMode.REPEAT_MODE_ONE
                    binding.shuffle.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                    //snack("单曲循环")
                }

                RepeatMode.REPEAT_MODE_SHUFFLE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, true)
                    Global.repeatMode = RepeatMode.REPEAT_MODE_SHUFFLE
                    binding.shuffle.setImageResource(R.drawable.ic_baseline_repeat_24)
                    //snack("顺序播放")
                }
            }
        }
        binding.play.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                binding.play.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }
            if (StarrySky.with().isPlaying()) {
                StarrySky.with().pauseMusic()
            } else {
                StarrySky.with().restoreMusic()
            }
        }
        StarrySky.with().setOnPlayProgressListener(object : OnPlayProgressListener {
            @SuppressLint("SetTextI18n")
            override fun onPlayProgress(currPos: Long, duration: Long) {
                runCatching {
                    binding.seekBar.setValue(currPos.toFloat(), true)
                    binding.currentTime.text = currPos.formatTime()
                    binding.totalTime.text = duration.formatTime()
                    binding.lyric.updateTime(currPos + 1000L, true)
                }
            }
        }, "music")
        runCatching {
            StarrySky.with().getNowPlayingSongInfo()?.let { song ->
                binding.cover.load(song.songCover,
                    placeholder = R.drawable.umeng_socialize_share_music,
                    onImageLoad = {
                        it?.let { drawable ->
                            binding.flowView.setFlowingLight(
                                StackBlurUtils.processWithCache(drawable.toBitmap(), 20)!!
                            )
                        }
                    })
                binding.title.text(song.songName)
                binding.sub.text(song.artist)
            }
        }
        binding.seekBar.setOnSliderTouchListener(object : NiftySlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: NiftySlider) {
                //Toast.makeText(context, "Start Tracking Touch", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(slider: NiftySlider) {
                //Toast.makeText(context, "Stop Tracking Touch", Toast.LENGTH_SHORT).show()
                StarrySky.with().seekTo(slider.value.toLong(), true)
            }

        })
        binding.previous.setOnClickListener {
            if (StarrySky.with().isSkipToPreviousEnabled()) {
                StarrySky.with().skipToPrevious()
            } else {
                toast("已经是第一首歌了")
            }
        }
        binding.next.setOnClickListener {
            if (StarrySky.with().isSkipToNextEnabled()) {
                StarrySky.with().skipToNext()
            } else {
                toast("已经是最后一首歌了")
            }
        }
        binding.speed.throttleClick {
            selectSpeed()
        }
        binding.snooze.throttleClick {
            snoozeDialog()
        }
        StarrySky.with().playbackState().observe(this) { playbackStage ->
            when (playbackStage.stage) {
                PlaybackStage.SWITCH -> {
                    binding.seekBar.valueTo = playbackStage.songInfo?.duration?.toFloat() ?: 1f
                    StarrySky.with().getNowPlayingSongInfo()?.let { song ->
                        binding.cover.load(song.songCover,
                            placeholder = R.drawable.umeng_socialize_share_music,
                            onImageLoad = {
                                it?.let { drawable ->
                                    binding.flowView.setFlowingLight(
                                        StackBlurUtils.processWithCache(drawable.toBitmap(), 20)!!
                                    )
                                }
                            })
                        binding.title.text(song.songName)
                        binding.sub.text(song.artist)
                        binding.lyric.loadLyric(emptyList())
                        val lyricEntry = arrayListOf<LyricEntry>()
                        song.lrclist?.forEach {
                            lyricEntry.add(
                                LyricEntry(
                                    (it.time.toDouble() * 1000).toLong(), it.lineLyric
                                )
                            )
                        }
                        requireContext().glideDrawable(song.songCover)?.toBitmap()
                            ?.let { binding.flowView.setFlowingLight(it) }
                        song.apply {
                            if (translateLyric?.isNotBlank() == true) {
                                binding.lyric.loadLyric(lyric, translateLyric)
                            } else {
                                binding.lyric.loadLyric(lyric)
                            }
                        }
                        runCatching {
                            binding.seekBar.valueTo = song.duration.toFloat()
                        }
                    }
                }

                PlaybackStage.PLAYING -> {
                    binding.play.setImageResource(R.drawable.ic_baseline_stop_24)
                    binding.seekBar.valueTo = playbackStage.songInfo?.duration?.toFloat() ?: 1f
                }

                PlaybackStage.PAUSE -> {
                    binding.play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }

                PlaybackStage.IDLE -> {
                    binding.play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }

                PlaybackStage.ERROR -> {
                    toast("播放失败：" + playbackStage.errorMsg)
                }
            }
        }
    }

    private fun snoozeDialog() {
        TimeSheet().show(requireContext()) {
            title("睡眠定时")
            format(TimeFormat.HH_MM)
            currentTime(
                TimeUnit.HOURS.toSeconds(0)
                    .plus(TimeUnit.MINUTES.toSeconds(60).plus(TimeUnit.SECONDS.toSeconds(12)))
            )
            onPositive { durationTimeInMillis: Long ->
                // Handle selected time
                StarrySky.with()
                    .stopByTimedOff(durationTimeInMillis, isPause = false, isFinishCurrSong = true)
            }
        }
    }

    private fun refreshSpeed(speed: Float) {
        StarrySky.with().onDerailleur(false, speed)
    }

    private var checkedSpeed by serialLazy("1倍速")
    private var speedInt by serialLazy(1F)

    private fun selectSpeed() {
        val countries = listOf(
            "0.5倍速", "1倍速", "1.5倍速", "2倍速"
        )
        val speed = listOf(0.5F, 1F, 1.5F, 2F)
        val checkedIndex = countries.indexOf(checkedSpeed)
        singleChoiceSelector(countries, checkedIndex, "选择倍速") { dialog, i ->
            checkedSpeed = countries[i]
            speedInt = speed[i]
            refreshSpeed(speedInt)
            snackbar("已切换为${countries[i]}", "确定") {}
            dialog.dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
    }

}
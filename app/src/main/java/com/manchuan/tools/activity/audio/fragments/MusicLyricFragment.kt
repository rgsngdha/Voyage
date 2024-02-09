package com.manchuan.tools.activity.audio.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.OnPlayClickListener
import com.drake.channel.sendEvent
import com.drake.engine.base.EngineFragment
import com.drake.engine.utils.throttleClick
import com.dylanc.longan.addStatusBarHeightToPaddingTop
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.toast
import com.litao.slider.NiftySlider
import com.litao.slider.effect.AnimationEffect
import com.lxj.androidktx.core.dp
import com.lxj.androidktx.core.drawable
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.manager.PlaybackStage
import com.manchuan.tools.R
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.FragmentMusicLyricBinding
import com.manchuan.tools.extensions.addAlpha
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.colorPrimaryInverse
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.windowBackground
import com.manchuan.tools.utils.StackBlurUtils
import java.math.BigDecimal

class MusicLyricFragment :
    EngineFragment<FragmentMusicLyricBinding>(R.layout.fragment_music_lyric) {
    override fun initData() {
        runCatching {
            val songInfo = StarrySky.with().getNowPlayingSongInfo()
            binding.lyric.loadLyric(emptyList())
            val lyricEntry = arrayListOf<LyricEntry>()
            songInfo?.lrclist?.forEach {
                lyricEntry.add(
                    LyricEntry(
                        BigDecimal.valueOf(it.time.toDouble()).multiply(BigDecimal.valueOf(1000L))
                            .toLong(), it.lineLyric
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

    override fun initView() {
        binding.titleLay.addStatusBarHeightToPaddingTop()
        binding.lyric.apply {
            setIsDrawTranslation(true)
            setNormalTextSize(70f)
            setIsEnableBlurEffect(Global.isEnabledLyricBlur)
            setCurrentTextSize(85f)
            setTranslateTextScaleValue(0.8f)
            setHorizontalOffsetPercent(0.2f)
            setItemOffsetPercent(0f)
            setNormalColor(if (isAppDarkMode) colorPrimary() else colorPrimaryInverse())
            setCurrentColor(if (isAppDarkMode) colorPrimaryInverse() else colorPrimary())
            setDraggable(true, object : OnPlayClickListener {
                override fun onPlayClick(time: Long): Boolean {
                    StarrySky.with().seekTo(time)
                    return true
                }

            }

            )
        }
        binding.titleLay.setOnClickListener {
            sendEvent(0, "change_page")
        }
        with(binding) {
            //thumb为半透明颜色时可能会将track背景映射出来,需要按需根据自己的背景对颜色进行下转换
            val thumbColor = ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(Color.WHITE, 0x55), Color.BLACK
            )
            val trackColor = ColorUtils.setAlphaComponent(colorPrimary(), 0x33)
            val inactiveColor = ColorUtils.setAlphaComponent(colorPrimary(), 0x11)

            val animEffect = AnimationEffect(volume).apply {
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

            volume.apply {
                setValue(StarrySky.with().getVolume(), true)
                effect = animEffect
                setTrackTintList(ColorStateList.valueOf(trackColor))
                setTrackInactiveTintList(ColorStateList.valueOf(inactiveColor))
                setThumbTintList(ColorStateList.valueOf(thumbColor))
                setThumbShadowColor(Color.BLACK)
                addOnSliderTouchStopListener {
                    StarrySky.with().setVolume(it.value)
                    Global.defaultVolume = it.value
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
        binding.blur.icon =
            drawable(if (Global.isEnabledLyricBlur) R.drawable.outline_blur_on_24 else R.drawable.outline_blur_off_24)
        StarrySky.with().setOnPlayProgressListener(object : OnPlayProgressListener {
            @SuppressLint("SetTextI18n")
            override fun onPlayProgress(currPos: Long, duration: Long) {
                runCatching {
                    binding.lyric.updateTime(currPos + 1000L, true)
                }
            }
        }, "lyric")
        binding.blur.throttleClick {
            Global.isEnabledLyricBlur = !Global.isEnabledLyricBlur
            binding.lyric.setIsEnableBlurEffect(Global.isEnabledLyricBlur)
            binding.blur.icon =
                drawable(if (Global.isEnabledLyricBlur) R.drawable.outline_blur_on_24 else R.drawable.outline_blur_off_24)
        }
        binding.rootLay.setBackgroundColor(windowBackground().addAlpha(0.9f))
    }

    override fun onStart() {
        super.onStart()
        runCatching {
            StarrySky.with().getNowPlayingSongInfo()?.let { song ->
                binding.cover.load(
                    song.songCover, placeholder = R.drawable.umeng_socialize_share_music
                )
                binding.title.text(song.songName)
                binding.sub.text(song.artist)
            }
        }
        StarrySky.with().playbackState().observe(this) { playbackStage ->
            when (playbackStage.stage) {
                PlaybackStage.SWITCH -> {
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
                                    (it.time.toFloat() * 1000).toLong(), it.lineLyric
                                )
                            )
                        }
                        song.apply {
                            if (translateLyric?.isNotBlank() == true) {
                                binding.lyric.loadLyric(lyric, translateLyric)
                            } else {
                                binding.lyric.loadLyric(lyric)
                            }
                        }
                    }
                }

                PlaybackStage.PLAYING -> {
                    binding.play.icon = drawable(R.drawable.ic_baseline_stop_24)
                }

                PlaybackStage.PAUSE -> {
                    binding.play.icon = drawable(R.drawable.ic_baseline_play_arrow_24)
                }

                PlaybackStage.IDLE -> {
                    binding.play.icon = drawable(R.drawable.ic_baseline_play_arrow_24)
                }

                PlaybackStage.ERROR -> {
                    toast("播放失败：" + playbackStage.errorMsg)
                }
            }
        }
    }

}
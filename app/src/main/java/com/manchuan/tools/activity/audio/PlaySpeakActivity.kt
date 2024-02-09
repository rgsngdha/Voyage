package com.manchuan.tools.activity.audio

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.palette.graphics.Palette
import com.blankj.utilcode.util.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.drake.brv.layoutmanager.HoverLinearLayoutManager
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.engine.utils.throttleClick
import com.drake.interval.Interval
import com.drake.serialize.serialize.serialLazy
import com.dylanc.longan.activity
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.addStatusBarHeightToPaddingTop
import com.dylanc.longan.context
import com.dylanc.longan.doOnClick
import com.dylanc.longan.isLightStatusBar
import com.dylanc.longan.isXiaomiRom
import com.gyf.immersionbar.ImmersionBar
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.tip
import com.lxj.androidktx.core.visible
import com.lxj.androidktx.snackbar
import com.lzx.starrysky.OnPlayProgressListener
import com.lzx.starrysky.SongInfo
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.lzx.starrysky.manager.PlaybackStage
import com.lzx.starrysky.utils.formatTime
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityPlaySpeakBinding
import com.manchuan.tools.databinding.ItemsSpeaksBinding
import com.manchuan.tools.extensions.getColorByAttr
import com.manchuan.tools.extensions.selector
import com.manchuan.tools.extensions.singleChoiceSelector
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.model.PlayList
import com.maxkeppeler.sheets.time.TimeFormat
import com.maxkeppeler.sheets.time.TimeSheet
import com.pawegio.kandroid.fromApi
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class PlaySpeakActivity : BaseActivity() {

    private val binding by lazy {
        ActivityPlaySpeakBinding.inflate(layoutInflater)
    }
    private val playList = ArrayList<PlayList>()
    private val playerList = mutableListOf<SongInfo>()

    private val MIUI_SYSTEMUI_PLUGIN: String = "miui.systemui.plugin"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ImmersionBar.with(this).transparentBar().init()
        binding.appbar.addStatusBarHeightToPaddingTop()
        binding.moreAction.addNavigationBarHeightToMarginBottom()
        binding.titleLay.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        val bundle = intent.extras!!
        val image = bundle.getString("image")
        val title = bundle.getString("title")
        val author = bundle.getString("author")
        val startUrl = bundle.getString("startUrl")
        binding.title.text = title
        binding.sub.text = author
        fromApi(Build.VERSION_CODES.TIRAMISU, action = {
            XXPermissions.with(this).permission(Permission.POST_NOTIFICATIONS)
                .request { permissions, all ->
                    if (!all) {
                        snack("权限被拒绝，将无媒体通知")
                    }
                }
        })
        if (isXiaomiRom) {
            binding.miPlay.visible()
        } else {
            binding.miPlay.gone()
        }
        binding.miPlay.throttleClick {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val cn = ComponentName(
                MIUI_SYSTEMUI_PLUGIN, "miui.systemui.miplay.MiPlayDetailActivity"
            )
            intent.component = cn
            startActivity(intent)
        }
        binding.recyclerView.linear(HoverLinearLayoutManager.VERTICAL).setup {
            addType<SongInfo>(R.layout.items_speaks)
            onBind {
                val items = ItemsSpeaksBinding.bind(itemView)
                items.title.text = getModel<SongInfo>().songName
                items.id.text = "第${modelPosition + 1}集"
            }
            onClick(R.id.item) {
                StarrySky.with().playMusicById(getModel<SongInfo>(modelPosition).songId)
            }
            onLongClick(R.id.item) {
                val model = getModel<SongInfo>()
                selector(listOf("下载"), model.songName) { dialogInterface, s, i ->
                    when (s) {
                        "下载" -> {

                        }
                    }
                }
            }
        }.models = playerList
        binding.recyclerView.addStatusBarHeightToMarginTop()
        binding.actionBars.addNavigationBarHeightToMarginBottom()
        WaitDialog.show("加载中...")
        if (image != null) {
            loadAlums(image)
        }
        binding.playList.doOnClick {
            binding.drawerLayout.open()
        }
        runCatching {
            Thread {
                playerList.clear()
                val document = Jsoup.connect(startUrl.toString()).get()
                val row = document.getElementsByClass("ul-36 clearfix").first()
                for (items in row?.getElementsByTag("li")!!) {
                    playList.add(
                        PlayList(
                            title = items?.select("a")?.text()!!,
                            url = items.select("a").attr("abs:href")
                        )
                    )
                }
                for (audio in 0 until playList.size) {
                    runCatching {
                        val documents = Jsoup.connect(playList[audio].url).get()
                        val playerTow = StringUtils.substringBetween(
                            documents.toString(), "var now=\"", "\";var pn="
                        )
                        if (playerTow != null) {
                            val info = SongInfo()
                            info.songId = (audio + 1).toString()
                            info.songUrl = playerTow
                            info.songName = playList[audio].title
                            info.artist = if (author.isNullOrEmpty()) "未知" else author
                            info.songCover = if (image.isNullOrEmpty()) "" else image
                            playerList.add(info)
                            runOnUiThread {
                                StarrySky.with().updatePlayList(playerList)
                                binding.recyclerView.bindingAdapter.notifyItemInserted(playerList.size)
                            }
                        }
                    }
                }
            }.start()
        }.onFailure {
            WaitDialog.dismiss()
            tip("错误")
        }
        Interval(1, TimeUnit.SECONDS).subscribe {
            if (playerList.size >= 1) {
                WaitDialog.dismiss()
                this.cancel()
                StarrySky.with().playMusic(playerList, 0)
            }
        }.start()
        StarrySky.with().playbackState().observe(this) {
            when (it.stage) {
                PlaybackStage.PLAYING -> {
                    binding.title.text =
                        "$title - 第${StarrySky.with().getNowPlayingSongInfo()?.songId}集"
                    binding.play.setImageResource(R.drawable.ic_baseline_stop_24)
                }

                PlaybackStage.PAUSE -> {
                    binding.play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }

                PlaybackStage.IDLE -> {
                    binding.play.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }

                PlaybackStage.ERROR -> {
                    snack("播放失败：" + it.errorMsg)
                }
            }
        }
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
                    binding.shuffle.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                    snack("单曲循环")
                }

                RepeatMode.REPEAT_MODE_ONE -> if (model.isLoop) {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_SHUFFLE, false)
                    binding.shuffle.setImageResource(R.drawable.ic_baseline_shuffle_24)
                    snack("随机播放")
                } else {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_ONE, true)
                    binding.shuffle.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                    snack("单曲循环")
                }

                RepeatMode.REPEAT_MODE_SHUFFLE -> {
                    StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, false)
                    binding.shuffle.setImageResource(R.drawable.ic_baseline_repeat_24)
                    snack("顺序播放")
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
                if (binding.seekBar.max.toLong() != duration) {
                    binding.seekBar.max = duration.toInt()
                }
                binding.seekBar.setProgress(currPos.toInt(), true)
                binding.currentTime.text = currPos.formatTime()
                binding.totalTime.text = duration.formatTime()
            }
        })
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                StarrySky.with().seekTo(seekBar.progress.toLong(), true)
            }
        })
        binding.previous.setOnClickListener {
            StarrySky.with().skipToPrevious()
        }
        binding.next.setOnClickListener {
            StarrySky.with().skipToNext()
        }
        binding.speed.doOnClick {
            selectSpeed()
        }
        refreshSpeed(speedInt)
        binding.snooze.doOnClick {
            snoozeDialog()
        }
    }

    private fun snoozeDialog() {
        TimeSheet().show(context) {
            title("睡眠定时")
            format(TimeFormat.HH_MM)
            minTime(3600)
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
        val countries = listOf("0.5倍速", "1倍速", "1.5倍速", "2倍速")
        val speed = listOf(0.5F, 1F, 1.5F, 2F)
        val checkedIndex = countries.indexOfFirst { it == checkedSpeed }
        singleChoiceSelector(countries, checkedIndex, "选择倍速") { dialog, i ->
            checkedSpeed = countries[i]
            speedInt = speed[i]
            refreshSpeed(speedInt)
            snackbar("已切换为${countries[i]}", "确定") {}
            dialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        StarrySky.with().stopMusic()
        StarrySky.with().clearPlayList()
        StarrySky.closeNotification()
        runCatching {
            playerList.clear()
            playList.clear()
        }
    }

    private fun loadAlums(bitmap: String) {
        Glide.with(this).asBitmap().optionalCenterCrop()
            .placeholder(R.drawable.umeng_socialize_share_music).load(bitmap).skipMemoryCache(true)
            .listener(object : RequestListener<Bitmap> {
                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    runCatching {
                        binding.flowView.setFlowingLight(resource)
                    }
                    Palette.from(resource).generate { palette ->
                        palette?.let {
                            val defaultColor =
                                getColorByAttr(com.google.android.material.R.attr.colorAccent)
                            activity.isLightStatusBar = !ColorUtils.isLightColor(
                                it.getVibrantColor(
                                    defaultColor
                                )
                            )
                            if (ColorUtils.isLightColor(
                                    it.getVibrantColor(
                                        defaultColor
                                    )
                                )
                            ) {
                                refreshColor(Color.WHITE)
                            } else {
                                refreshColor(Color.BLACK)
                            }
                        }
                    }
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>,
                    isFirstResource: Boolean
                ): Boolean {
                    loadAlums(R.drawable.umeng_socialize_share_music)
                    return false
                }

            }).transition(BitmapTransitionOptions.withCrossFade()).into(binding.imageView5)
    }

    private fun refreshColor(@ColorInt color: Int) {
        binding.title.setTextColor(color)
        binding.sub.setTextColor(color)
        binding.currentTime.setTextColor(color)
        binding.totalTime.setTextColor(color)
        binding.play.setColorFilter(color)
        binding.previous.setColorFilter(color)
        binding.next.setColorFilter(color)
        binding.shuffle.setColorFilter(color)
        binding.snooze.setColorFilter(color)
        binding.playList.setColorFilter(color)
        binding.speed.setColorFilter(color)
        binding.miPlay.setColorFilter(color)
    }

    private fun loadAlums(@DrawableRes resId: Int) {
        Glide.with(this).asBitmap().load(resId).skipMemoryCache(false)
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(object : SimpleTarget<Bitmap?>() {

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?,
                ) {
                    runCatching {
                        binding.imageView5.setImageBitmap(resource)
                        binding.flowView.setFlowingLight(resource)
                    }
                    Palette.from(resource).generate { palette ->
                        palette?.let {
                            val defaultColor =
                                getColorByAttr(com.google.android.material.R.attr.colorAccent)
                            ImmersionBar.with(this@PlaySpeakActivity).transparentBar()
                                .statusBarDarkFont(
                                    !ColorUtils.isLightColor(
                                        it.getVibrantColor(
                                            defaultColor
                                        )
                                    )
                                ).titleBar(binding.appbar).init()
                            if (ColorUtils.isLightColor(
                                    it.getVibrantColor(
                                        defaultColor
                                    )
                                )
                            ) {
                                refreshColor(Color.WHITE)
                            } else {
                                refreshColor(Color.BLACK)
                            }
                        }
                    }
                }

            })
    }
}
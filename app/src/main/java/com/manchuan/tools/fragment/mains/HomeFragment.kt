package com.manchuan.tools.fragment.mains

import android.os.Build
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.amap.api.location.AMapLocation
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.addModels
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.engine.base.EngineFragment
import com.drake.engine.utils.throttleClick
import com.drake.interval.Interval
import com.drake.net.Get
import com.drake.net.utils.runMain
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.dp
import com.dylanc.longan.startActivity
import com.dylanc.longan.textString
import com.itxca.spannablex.spannable
import com.jinrishici.sdk.android.JinrishiciClient
import com.jinrishici.sdk.android.listener.JinrishiciCallback
import com.jinrishici.sdk.android.model.JinrishiciRuntimeException
import com.jinrishici.sdk.android.model.PoetySentence
import com.lxj.androidktx.core.doOnlyOnce
import com.lxj.androidktx.core.toDateString
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.SearchFunctionsActivity
import com.manchuan.tools.activity.audio.SearchMusicActivity
import com.manchuan.tools.activity.game.GamesActivity
import com.manchuan.tools.activity.hide.HideFunctionsActivity
import com.manchuan.tools.activity.images.AvatarsCategoryActivity
import com.manchuan.tools.activity.images.ImageParagraphActivity
import com.manchuan.tools.activity.images.WallpaperCategoryActivity
import com.manchuan.tools.activity.life.AllInOneJiexiActivity
import com.manchuan.tools.activity.life.BingWallpaperActivity
import com.manchuan.tools.activity.life.HistoryPersonActivity
import com.manchuan.tools.activity.life.SearchSoftwareActivity
import com.manchuan.tools.activity.movies.MoviesMainActivity
import com.manchuan.tools.activity.news.TopHotActivity
import com.manchuan.tools.activity.normal.LanzouActivity
import com.manchuan.tools.activity.site.SitesShareActivity
import com.manchuan.tools.activity.site.WebActivity
import com.manchuan.tools.activity.video.ShortVideoActivity
import com.manchuan.tools.activity.vivo.ThemeActivity
import com.manchuan.tools.bean.AdsBean
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.FragmentHomeBinding
import com.manchuan.tools.databinding.ItemFunctionBinding
import com.manchuan.tools.extensions.NowTimeString
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.balloon
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.joinGroup
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.rootView
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.viewQQPersonal
import com.manchuan.tools.interfaces.HomeBannerAdapter
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.model.banner.BannerModel
import com.qweather.sdk.bean.weather.WeatherNowBean.NowBaseBean
import com.skydoves.balloon.BalloonAlign
import com.skydoves.whatif.whatIfNotNull
import com.youth.banner.indicator.RoundLinesIndicator
import java.util.concurrent.TimeUnit

class HomeFragment : EngineFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    override fun initData() {

    }

    private lateinit var containers: LinearLayout
    lateinit var root: View
    private lateinit var adsList: MutableList<AdsBean>

    private val sentencesInterval by lazy {
        Interval(5, TimeUnit.MINUTES)
    }


    override fun initView() {
        adsList = ArrayList()
        containers = binding.containers
        enableTransitionTypes(containers)
        activity?.rootView?.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                insets.displayCutout?.apply {
                    binding.root.apply {
                        // 设置padding，防止内容显示到非安全区域
                        setPadding(
                            safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom
                        )
                    }
                }
            } else {
                binding.root.addStatusBarHeightToMarginTop()
            }
            // 不消费，直接返回原始对象
            insets
        }
        with(binding) {
            searchBar.throttleClick {
                startActivity<SearchFunctionsActivity>()
            }
            subtitle.text = Global.localSentence
            sentencesInterval.subscribe {
                if (title.textString != NowTimeString) {
                    title.text(NowTimeString)
                }
                val client = JinrishiciClient.getInstance()
                client.getOneSentenceBackground(object : JinrishiciCallback {
                    override fun done(poetySentence: PoetySentence) {
                        //在这里进行你的逻辑处理
                        subtitle.text(poetySentence.data.content)
                    }

                    override fun error(e: JinrishiciRuntimeException) {

                    }
                })
            }.start()
            specialRecycler.grid(2).divider { // 水平间距
                orientation = DividerOrientation.GRID
                setDivider(12, true)
            }.setup {
                addType<SpecialFunction>(R.layout.item_function)
                onBind {
                    val model = getModel<SpecialFunction>()
                    val binding = ItemFunctionBinding.bind(itemView)
                    binding.icon.setImageResource(model.icon)
                    binding.title.text = model.title
                    binding.summary.text = model.summary

                }
                onClick(R.id.item) {
                    val model = getModel<SpecialFunction>()
                    model.action.invoke()
                }
            }.models = specialFunctions()
            defaultRecycler.grid(2).divider { // 水平间距
                orientation = DividerOrientation.GRID
                setDivider(12, true)
            }.setup {
                addType<SpecialFunction>(R.layout.item_function)
                onBind {
                    val model = getModel<SpecialFunction>()
                    val binding = ItemFunctionBinding.bind(itemView)
                    binding.icon.setImageResource(model.icon)
                    binding.title.text = model.title
                    binding.summary.text = model.summary

                }
                onClick(R.id.item) {
                    val model = getModel<SpecialFunction>()
                    model.action.invoke()
                }
            }.models = defaultFunctions()
        }
        Global.isEnabledHideFunction.observe(viewLifecycleOwner) {
            if (it) {
                binding.specialRecycler.addModels(
                    listOf(SpecialFunction(
                        "隐藏功能",
                        "包含一些不对外开放的功能",
                        R.drawable.ic_alpha_f_box_outline,
                    ) {
                        startActivity<HideFunctionsActivity>()
                    })
                )
            } else {
                (binding.specialRecycler.mutable as MutableList<SpecialFunction>).find { it.title == "隐藏功能" }
                    .whatIfNotNull {
                        binding.specialRecycler.bindingAdapter.notifyItemRemoved(
                            binding.specialRecycler.mutable.indexOf(
                                it
                            )
                        )
                        binding.specialRecycler.mutable.remove(it)
                    }
            }
        }
        scopeNetLife {
            val bannerData = Get<BannerModel>("https://sites.zhongyi.team/banner.php") {
                converter = SerializationConverter("", "", "")
            }.await()
            binding.banner.setAdapter(HomeBannerAdapter(bannerData.data))
                .setIndicator(RoundLinesIndicator(requireContext())).setBannerRound(28.dp)
                .setOnBannerListener { data, position ->
                    val data = data as BannerModel.Data
                    when (data.clickType) {
                        "url" -> {
                            startActivity<WebActivity>("url" to data.clickUrl)
                        }

                        "qq_personal" -> {
                            viewQQPersonal(data.clickUrl)
                        }

                        "qq_group" -> {
                            joinGroup(data.clickUrl)
                        }
                    }
                }.setIntercept(false)
        }
        binding.toolbar.throttleClick {
            sendEvent("", "open_drawer")
        }
        var locationData = AMapLocation("")
        receiveEvent<AMapLocation>("amap_location_data") {
            locationData = it
        }
        receiveEvent<NowBaseBean>("weather_bean") {
            runMain {
                with(it) {
                    binding.weatherIcon.load(icon, isCrossFade = true)
                    binding.weatherIcon.throttleClick {
                        requireContext().alertDialog {
                            title = "天气情况"
                            message = spannable {
                                "状况:".text()
                                text.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "风向:".text()
                                windDir.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "风力等级:".text()
                                windScale.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "级".text()
                                newline()
                                "风速:".text()
                                windSpeed.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "km/h".text()
                                newline()
                                "温度:".text()
                                temp.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "℃".text()
                                newline()
                                "体感温度:".text()
                                feelsLike.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "℃".text()
                                newline()
                                "相对湿度:".text()
                                humidity.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "%".text()
                                newline()
                                "降水量:".text()
                                precip.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "mm".text()
                                newline()
                                "大气压强:".text()
                                pressure.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "hPa".text()
                                newline()
                                "能见度:".text()
                                vis.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "公里".text()
                                newline()
                                "云量:".text()
                                cloud.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                "%".text()
                                newline(2)
                                "定位数据".span {
                                    absoluteSize(24, true)
                                    color(colorPrimary())
                                }
                                newline()
                                "国家:".text()
                                locationData.country.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "省:".text()
                                locationData.province.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "城市:".text()
                                locationData.city.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "城区:".text()
                                locationData.district.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "地址:".text()
                                locationData.address.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "地区编码:".text()
                                locationData.adCode.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "城市编码:".text()
                                locationData.cityCode.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "定位时间:".text()
                                locationData.time.toDateString().span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                                newline()
                                "定位类型:".text()
                                when (locationData.locationType) {
                                    0 -> "定位失败"
                                    1 -> "GPS定位"
                                    3 -> "上次定位"
                                    4 -> "缓存定位"
                                    5 -> "WIFI定位"
                                    6 -> "基站定位"
                                    8 -> "离线定位"
                                    9 -> "最后缓存"
                                    11 -> "模糊定位"
                                    else -> "无结果"
                                }.span {
                                    color(colorPrimary())
                                    absoluteSize(18, true)
                                }
                            }
                            okButton { }
                        }.build()
                    }
                }
            }
        }
        with(binding) {
            doOnlyOnce("home_tips", action = {
                weatherIcon.post {
                    weatherIcon.balloon(
                        "同意定位请求后点击此处可显示天气数据", BalloonAlign.BOTTOM, 2000L
                    )
                }
            })
        }
    }

    private fun defaultFunctions(): MutableList<SpecialFunction> {
        return mutableListOf(
            SpecialFunction(
                "通知公告", "最新开发动态消息", R.drawable.outline_mark_chat_unread_24,
            ) {
                startActivity<WebActivity>("url" to "https://blog.fengmuchuan.cn/index.php/远航工具箱通知公告页面/")
            },
            SpecialFunction(
                "常见问题", "有问必答", R.drawable.outline_question_answer_24,
            ) {
                startActivity<WebActivity>("url" to "https://support.qq.com/product/320218/faqs-more")
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        sentencesInterval.stop()
    }

    override fun onPause() {
        super.onPause()
        sentencesInterval.pause()
    }

    override fun onResume() {
        super.onResume()
        sentencesInterval.resume()
    }

    private fun specialFunctions(): MutableList<SpecialFunction> {
        return mutableListOf(
            SpecialFunction(
                "实时热搜", "汇集全网热搜榜单", R.drawable.fire,
            ) {
                startActivity<TopHotActivity>()
            },
            SpecialFunction(
                "音乐搜索器", "免费下载付费音乐", R.drawable.music_note_music,
            ) {
                startActivity<SearchMusicActivity>()
            },
            SpecialFunction(
                "软件搜索", "海量破解应用/游戏任你搜索", R.drawable.outline_widgets_24,
            ) {
                startActivity<SearchSoftwareActivity>()
            },
            SpecialFunction(
                "实用网站分享", "专注分享实用、好用的网站", R.drawable.twotone_web_black_24dp,
            ) {
                startActivity<SitesShareActivity>()
            },
            SpecialFunction(
                "壁纸大全", "多分类高清壁纸", R.drawable.ic_image_outline,
            ) {
                startActivity<WallpaperCategoryActivity>()
            },
            SpecialFunction(
                "头像大全", "多分类高清头像", R.drawable.ic_account_circle_outline,
            ) {
                startActivity<AvatarsCategoryActivity>()
            },
            SpecialFunction(
                "电影大全", "海量电影资源", R.drawable.ic_baseline_local_movies_24,
            ) {
                startActivity<MoviesMainActivity>()
            },
            SpecialFunction(
                "人物志", "权威历史人物传记", R.drawable.account_details,
            ) {
                startActivity<HistoryPersonActivity>()
            },
            SpecialFunction(
                "聚合解析", "解析常见社交软件的图集与视频", R.drawable.outline_inbox_24,
            ) {
                startActivity<AllInOneJiexiActivity>()
            },
            SpecialFunction(
                "聚合图集解析", "可解析常见短视频软件的图集", R.drawable.ic_image_outline,
            ) {
                startActivity<ImageParagraphActivity>()
            },
            SpecialFunction(
                "聚合短视频解析", "可解析常见短视频软件的视频", R.drawable.ic_video,
            ) {
                startActivity<ShortVideoActivity>()
            },
            SpecialFunction(
                "必应壁纸", "一批来自世界各地的精美图片", R.drawable.microsoft_bing,
            ) {
                startActivity<BingWallpaperActivity>()
            },
            SpecialFunction(
                "怀旧游戏", "愿我们找回童年的快乐", R.drawable.gamepad_up,
            ) {
                startActivity<GamesActivity>()
            },
            SpecialFunction(
                "蓝奏云解析", "快速提取直连", R.drawable.material_download,
            ) {
                startActivity<LanzouActivity>()
            },
            SpecialFunction(
                "i 主题下载", "免费下载付费主题", R.drawable.ic_round_color_24,
            ) {
                startActivity<ThemeActivity>()
            },
        )
    }

    data class SpecialFunction(
        val title: String,
        val summary: String,
        @DrawableRes val icon: Int,
        val action: () -> Unit,
    )


}
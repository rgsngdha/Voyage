package com.manchuan.tools.activity.app

import ando.file.core.FileUri
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.cache.CacheMode
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.drake.net.utils.runMain
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.activity
import com.dylanc.longan.appVersionName
import com.dylanc.longan.browse
import com.dylanc.longan.context
import com.dylanc.longan.doOnClick
import com.dylanc.longan.installAPK
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.lifecycleOwner
import com.dylanc.longan.pressBackTwiceToExitApp
import com.dylanc.longan.toUri
import com.dylanc.longan.topActivity
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.itxca.spannablex.activateClick
import com.itxca.spannablex.spannable
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.views.ActivityScreenShotImageView
import com.lxj.androidktx.FragmentStateAdapter
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.curDay
import com.lxj.androidktx.core.disable
import com.lxj.androidktx.core.doOnlyOnce
import com.lxj.androidktx.core.drawable
import com.lxj.androidktx.core.enable
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.startActivity
import com.lxj.androidktx.core.string
import com.lxj.androidktx.core.toDateString
import com.lxj.androidktx.core.visible
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.user.MovieLoginActivity
import com.manchuan.tools.activity.user.UserCenterActivity
import com.manchuan.tools.application.App
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.DatabaseInitialize
import com.manchuan.tools.database.Global
import com.manchuan.tools.database.Global.isAcceptPolicy
import com.manchuan.tools.database.Global.isNeverAsk
import com.manchuan.tools.databinding.ActivityMainBinding
import com.manchuan.tools.databinding.JoinUsBottomSheetContentBinding
import com.manchuan.tools.databinding.NavHeaderMainBinding
import com.manchuan.tools.databinding.UpdateVersionDialogBinding
import com.manchuan.tools.extensions.addPaddingBottom
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.enableTransitionTypes
import com.manchuan.tools.extensions.getSpecStyleResId
import com.manchuan.tools.extensions.isSpringOrYuanXiao
import com.manchuan.tools.extensions.joinGroup
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.neutralButton
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.progress
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.extensions.sheetDialog
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.textCopyThenPost
import com.manchuan.tools.extensions.viewQQPersonal
import com.manchuan.tools.extensions.windowBackground
import com.manchuan.tools.fragment.mains.AccountFragment
import com.manchuan.tools.fragment.mains.FunctionsFragment
import com.manchuan.tools.fragment.mains.HomeFragment
import com.manchuan.tools.fragment.mains.ResourcesFragment
import com.manchuan.tools.interfaces.TencentLoginInterfaces
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.json.app.UpdateModel
import com.manchuan.tools.user.qqBindAccount
import com.manchuan.tools.user.userInfo
import com.manchuan.tools.utils.LunarCalender
import com.nowfal.kdroidext.kex.toast
import com.qweather.sdk.bean.base.Code
import com.qweather.sdk.bean.base.Lang
import com.qweather.sdk.bean.base.Unit
import com.qweather.sdk.bean.geo.GeoBean
import com.qweather.sdk.bean.weather.WeatherNowBean
import com.qweather.sdk.view.QWeather
import com.tencent.connect.common.Constants
import com.tencent.tauth.Tencent
import io.noties.markwon.Markwon
import java.io.File
import java.util.Calendar


class MainActivity : BaseActivity() {


    private lateinit var pageChangeListener: OnPageChangeCallback
    private lateinit var locationClient: AMapLocationClient
    private lateinit var locationListener: AMapLocationListener
    private lateinit var locationOption: AMapLocationClientOption
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var headerBinding: NavHeaderMainBinding


    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            transparentBar()
            statusBarDarkFont(!isAppDarkMode)
        }
        binding.bottomBar.post {
            binding.viewPager.addPaddingBottom(binding.bottomBar.height)
        }
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, R.string.open_drawer, R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(
            actionBarDrawerToggle
        )
        actionBarDrawerToggle.syncState()
        headerBinding = NavHeaderMainBinding.bind(binding.navDrawer.getHeaderView(0))
        binding.navDrawer.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_homes -> binding.bottomBar.selectedItemId = R.id.home
                R.id.nav_functions -> binding.bottomBar.selectedItemId = R.id.functions
                R.id.nav_resources -> binding.bottomBar.selectedItemId = R.id.my
                R.id.joinGroup -> joinUsDialog()
                R.id.check_update -> updateVersion(true)
                R.id.alipay -> {
                    alertDialog {
                        title = "支付宝捐赠"
                        message =
                            "捐赠之后可通过邮件方式将支付凭证发送给开发者，开发者将会赠送天数不定的会员卡密。"
                        okButton("捐赠") {
                            startAlipay(activity, "fkx152046rguay2tz7gcp4e")
                        }
                        cancelButton()
                    }.build()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }
        binding.viewPager.apply {
            setUpViewPager()
        }
        Global.isCanUserInput.observe(lifecycleOwner) {
            binding.viewPager.isUserInputEnabled = it
        }
        pageChangeListener = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomBar.menu.getItem(position).isChecked = true
            }
        }
        doOnlyOnce(curDay.toString(), action = {
            if (isSpringOrYuanXiao()) {
                alertDialog {
                    title = "新年快乐"
                    message = "${
                        LunarCalender().animalsYear(
                            Calendar.getInstance().get(Calendar.YEAR)
                        )
                    }到了,远航开发组祝您新年快乐！"
                    okButton {}
                }.build()
            }
        })
        binding.viewPager.registerOnPageChangeCallback(pageChangeListener)
        binding.bottomBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    binding.viewPager.currentItem = 0
                    return@setOnItemSelectedListener true
                }

                R.id.functions -> {
                    binding.viewPager.currentItem = 1
                    return@setOnItemSelectedListener true
                }

                R.id.resources -> {
                    binding.viewPager.currentItem = 2
                    return@setOnItemSelectedListener true
                }

                R.id.account -> {
                    binding.viewPager.currentItem = 3
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
        updateVersion()
        pressBackTwiceToExitApp("再按一次退出")
        initJoinUsDialog()
        binding.navDrawer.setCheckedItem(R.id.nav_homes)
        initUser()
        receiveEvent<Boolean>("refreshUser") {
            initUser()
        }
        receiveEvent<String>("open_drawer") {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }/*
        contentResolver.queryMediaAudios { cursor ->
            while (cursor.moveToNext()) {
                val file = File(getPath(cursor.getMediaUri()))
                val meta = AudioFileIO.read(file)
                val tag = meta.tag
                val header = meta.audioHeader
                loge(
                    "music",
                    "歌曲名称:${cursor.getSongTitle()}" +
                            "\n歌曲路径:${getPath(cursor.getMediaUri())}" +
                            "\n歌曲时长:${header.trackLength}" +
                            "\n歌曲年份:${cursor.year}" +
                            "\n比特率:${header.bitRate}" +
                            "\n采样率:${header.sampleRate}" +
                            "\n格式:${header.format}" +
                            "\n声道:${header.channels}" +
                            "\n歌曲文件大小:${file.formatSize()}" +
                            "\n歌词:\n${tag.getFirst(FieldKey.LYRICS).ifEmpty { "暂无歌词" }}"
                )
            }
        }

         */
    }


    @SuppressLint("SetTextI18n")
    private fun initUser() {
        Global.token.observe(this) { s ->
            if (s.isEmpty()) {
                headerBinding.apply {
                    //None of the following candidates is applicable because of receiver type mismatch:
                    imageView.load(
                        R.drawable.logo_avatar_anonymous_40dp,
                        isCrossFade = true,
                        skipMemory = true,
                        isForceOriginalSize = true,
                        signature = Global.avatarSignature
                    )
                    name.text("未登录")
                    contact.animateGone()
                    card.throttleClick {
                        startActivity<MovieLoginActivity>()
                    }
                }
            } else {
                userInfo(s, success = {
                    headerBinding.apply {
                        card.throttleClick {
                            startActivity<UserCenterActivity>()
                        }
                        imageView.load(
                            it.msg.pic,
                            isCrossFade = true,
                            skipMemory = true,
                            isForceOriginalSize = true,
                            signature = Global.avatarSignature
                        )
                        name.text = it.msg.name
                        contact.animateVisible()
                        loge(it.msg.vip.toLong())
                        contact.text = "会员到期时间:${
                            if (it.msg.vip == "999999999") "永久会员" else (it.msg.vip.toLong() * 1000L).toDateString(
                                "yyyy-MM-dd HH:mm"
                            )
                        }"
                    }
                }, {
                    toast(it)
                    App.tencent.logout(context)
                    Global.token.value = ""
                })
            }
        }
        receiveEvent<Boolean>("tencent_bind") {
            tencentLogin()
        }
        runCatching {
            loge(tag = "RecentDatabase", DatabaseInitialize.allRecentMusic)
        }
    }

    private fun setUpViewPager() {
        binding.viewPager.adapter = FragmentStateAdapter(
            HomeFragment(),
            FunctionsFragment(),
            ResourcesFragment(),
            AccountFragment(),
            isLazyLoading = false
        )
    }

    private fun initJoinUsDialog() {
        if (Global.countLaunch < 2) {
            Global.countLaunch = Global.countLaunch.inc()
        } else if (Global.countLaunch >= 2) {
            doOnlyOnce("join_us_for_usage", action = {
                joinUsDialog()
            })
        }
    }

    private fun joinUsDialog() {
        sheetDialog(
            JoinUsBottomSheetContentBinding.inflate(layoutInflater).root, string(R.string.join_us)
        ) {
            val binding = JoinUsBottomSheetContentBinding.bind(it)
            binding.content.activateClick().text = spannable {
                "感谢您的使用和支持，如果您在使用过程中出现任何问题可以加入我们官方群组。".text()
                newline()
                "如果无法加入官方群组，您可以联系开发者反馈问题".text()
                newline(2)
                "开发者联系方式:".text()
                newline()
                "QQ:".text()
                "川意".span {
                    color(colorPrimary())
                    clickable { _, matchText ->
                        viewQQPersonal("3299699002")
                    }
                }
                newline()
                "Telegram:".text()
                "川意".span {
                    color(colorPrimary())
                    clickable { _, matchText ->
                        browse("https://t.me/fengespos")
                    }
                }
                newline()
                "WeChat:".text()
                "川意".span {
                    color(colorPrimary())
                    clickable { _, matchText ->
                        textCopyThenPost("zycy519617")
                    }
                }
                newline(2)
                "官方群组:".text()
            }
            binding.telegram.setOnClickListener {
                browse("https://t.me/voyage_tool")
            }
            binding.qq.setOnClickListener {
                joinGroup("754591110")
            }
            binding.qqGuild.throttleClick {
                browse("https://pd.qq.com/s/28o0btlxe")
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        when {
            !isAcceptPolicy && !isNeverAsk -> {
                requestLocationPermission()
            }

            isAcceptPolicy && isNeverAsk -> {
                requestWeatherAndCovidForNetwork()
            }
        }
    }

    private val listeners by lazy {
        TencentLoginInterfaces { qqLogin ->
            App.tencent.setAccessToken(qqLogin.access_token, qqLogin.expires_in.toString())
            App.tencent.openId = qqLogin.openid
            qqBindAccount(Global.token.value ?: "",
                qqLogin.openid,
                qqLogin.access_token,
                success = {
                    TipDialog.show(it.msg, WaitDialog.TYPE.SUCCESS)
                    sendEvent(true, "refreshUserByBind")
                },
                failed = {
                    TipDialog.show(it, WaitDialog.TYPE.WARNING)
                    App.tencent.logout(topActivity)
                })
        }
    }

    private fun tencentLogin() {
        Tencent.setIsPermissionGranted(true, Build.MODEL)
        Tencent.resetTargetAppInfoCache()
        Tencent.resetQQAppInfoCache()
        Tencent.resetTimAppInfoCache()
        if (!App.tencent.isSessionValid) {
            when (App.tencent.login(this, "all", listeners)) {
                //下面为login可能返回的值的情况
                0 -> {
                    snack("异常登录")
                }

                1 -> {
                    loge("开始登录")
                }

                -1 -> {
                    snack("登录异常")
                }

                2 -> {

                }

                else -> {
                    snack("登录出错")
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Tencent.onActivityResultData(requestCode, resultCode, data, listeners)
        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_LOGIN) {
                Tencent.handleResultData(data, listeners)
            }
        }
    }

    private fun requestWeatherAndCovidForNetwork() {
        locationOption = AMapLocationClientOption()
        locationClient = AMapLocationClient(this)
        locationOption.apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            isOnceLocation = true
        }
        locationListener = AMapLocationListener { location ->
            with(location) {
                sendEvent(location, "amap_location_data")
                QWeather.getGeoCityLookup(context, adCode, object : QWeather.OnResultGeoListener {
                    override fun onError(p0: Throwable?) {
                        runMain {
                            toast("查询城市失败，原因:${p0?.message}")
                        }
                    }

                    override fun onSuccess(geo: GeoBean?) {
                        if (geo != null) {
                            QWeather.getWeatherNow(context,
                                geo.locationBean.first().id,
                                Lang.ZH_HANS,
                                Unit.METRIC,
                                object : QWeather.OnResultWeatherNowListener {
                                    override fun onError(p0: Throwable?) {
                                        runMain {
                                            toast("天气获取出错，原因:${p0?.message}")
                                        }
                                    }

                                    override fun onSuccess(bean: WeatherNowBean?) {
                                        with(bean!!) {
                                            if (Code.OK === code) {
                                                sendEvent(now, "weather_bean")
                                            } else {
                                                loge("failed code: $code")
                                            }
                                        }
                                    }

                                })
                        }

                    }

                })
            }
        }
        locationClient.apply {
            setLocationOption(locationOption)
            setLocationListener(locationListener)
        }
        locationClient.startLocation()
    }

    private fun requestLocationPermission() {
        alertDialog {
            title = "权限申请"
            message = "获取当地新闻、天气信息等需要定位权限"
            okButton {
                XXPermissions.with(context)
                    .permission(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)
                    .request { permissions, allGranted ->
                        if (allGranted) {
                            isAcceptPolicy = true
                            isNeverAsk = true
                            AMapLocationClient.updatePrivacyShow(this@MainActivity, true, true)
                            AMapLocationClient.updatePrivacyAgree(this@MainActivity, true)
                            Global.isAccessLocation.value = true
                            requestWeatherAndCovidForNetwork()
                        } else {
                            XXPermissions.startPermissionActivity(context, permissions)
                            toast("部分权限被拒绝，需手动授予")
                        }
                    }
            }
            cancelButton("拒绝") {
                isAcceptPolicy = false
            }
            neutralButton("不再询问") {
                isNeverAsk = true
                isAcceptPolicy = false
            }
        }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            locationClient.onDestroy()
        }
    }

    private fun updateVersion(showToast: Boolean = false) {
        scopeNetLife {
            if (showToast) WaitDialog.show("检查更新中...")
            val string = Get<UpdateModel>("https://wds.ecsxs.com/230815.json") {
                setCacheMode(CacheMode.REQUEST_THEN_READ)
                converter = SerializationConverter("1", "ycTemplate", "ycTemplate")
            }.await()
            if (showToast) WaitDialog.dismiss()
            if (string.versionCode > BuildConfig.VERSION_CODE) {
                ActivityScreenShotImageView.hideContentView = true
                FullScreenDialog.build(object :
                    OnBindView<FullScreenDialog>(R.layout.update_version_dialog) {
                    override fun onBind(dialog: FullScreenDialog, v: View) {
                        val versionBinding = UpdateVersionDialogBinding.bind(v)
                        enableTransitionTypes(
                            versionBinding.buttonLayout,
                            versionBinding.contents,
                            versionBinding.root
                        )
                        val markdown = Markwon.create(context)
                        versionBinding.apply {
                            markdown.setMarkdown(content, string.message.trimIndent())
                            if (string.must == 1) cancelButton.gone()
                            cancelButton.setOnClickListener {
                                if (string.must == 1) {
                                    toast("服务端已对此版本配置为强制更新，请勿通过非法手段显示取消更新按钮")
                                } else {
                                    dialog.dismiss()
                                }
                            }
                            upgrade.setOnClickListener {
                                progress.show()
                                progressText.visible()
                                val spec = CircularProgressIndicatorSpec(
                                    context,  /*attrs=*/
                                    null, 0, getSpecStyleResId()
                                )
                                val progressIndicatorDrawable: IndeterminateDrawable<CircularProgressIndicatorSpec> =
                                    IndeterminateDrawable.createCircularDrawable(context,
                                        spec.apply {
                                            indicatorColors = intArrayOf(colorPrimary())
                                            trackCornerRadius = 4
                                        })
                                upgrade.icon = progressIndicatorDrawable
                                scopeNetLife {
                                    upgrade.disable()
                                    File.separator
                                    publicDownloadsDirPath
                                    appVersionName
                                    val file = Get<File>(string.url) {
                                        setDownloadDir(com.dylanc.longan.externalDownloadsDirPath.toString())
                                        setDownloadFileNameConflict(true)
                                        setDownloadMd5Verify()
                                        addDownloadListener(object : ProgressListener() {
                                            @SuppressLint("SetTextI18n")
                                            override fun onProgress(p: Progress) {
                                                runMain {
                                                    progress.progress(p.progress())
                                                    progressText.text =
                                                        "进度:${p.progress()}% 总大小:${p.totalSize()} 已下载:${p.currentSize()}"
                                                    upgrade.text = "下载中"
                                                }
                                            }

                                        })
                                    }.await()
                                    progress.hide()
                                    progressText.gone()
                                    upgrade.enable()
                                    upgrade.icon = drawable(R.drawable.outline_open_in_new_24)
                                    upgrade.text = "安装"
                                    upgrade.doOnClick {
                                        FileUri.getUriByPath(file.absolutePath)
                                            ?.let { it1 -> installAPK(it1) }
                                        loge(tag = "安装包文件位置", file.toUri())
                                    }
                                    upgrade.performClick()
                                }.catch {
                                    progress.hide()
                                    progressText.gone()
                                    upgrade.enable()
                                    toast("下载失败:${it.message}")
                                    println(it.printStackTrace())
                                }
                            }
                        }
                    }
                }).apply {
                    isAllowInterceptTouch = false
                    backgroundColor = windowBackground()
                    isCancelable = false
                }.show()
            } else {
                if (showToast) toast("已经是最新版")
            }
        }.catch {
            it.printStackTrace()
        }
    }

    private fun startAlipay(activity: Activity, urlCode: String): Boolean {
        return startIntentUrl(activity, URL_FORMAT.replace("{urlCode}", urlCode))
    }

    private fun startIntentUrl(activity: Activity, intentUrl: String): Boolean {
        runCatching {
            val intent = Intent.parseUri(intentUrl, Intent.URI_INTENT_SCHEME)
            activity.startActivity(intent)
        }.onFailure {
            toast("启动失败")
        }
        return false
    }

    private val URL_FORMAT =
        "intent://platformapi/startapp?saId=10000007&" + "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" + "%3Dweb-other&_t=1472443966571#Intent;" + "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"

}
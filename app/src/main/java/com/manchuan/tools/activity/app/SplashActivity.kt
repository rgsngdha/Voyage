package com.manchuan.tools.activity.app

import android.annotation.SuppressLint
import android.os.Bundle
import com.drake.net.Get
import com.drake.net.cache.CacheMode
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.pressBackTwiceToExitApp
import com.dylanc.longan.toast
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.androidktx.core.doOnceInDay
import com.lxj.androidktx.core.postDelay
import com.lxj.androidktx.core.sp
import com.manchuan.tools.activity.audio.SearchMusicActivity
import com.manchuan.tools.activity.movies.MoviesMainActivity
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.DEFAULT_LAUNCH
import com.manchuan.tools.database.DEFAULT_LAUNCH_DEFAULT_VALUE
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivitySplashBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.startActivity
import com.manchuan.tools.interceptor.PermissionInterceptor
import com.manchuan.tools.manager.StorageManager
import com.manchuan.tools.utils.atLeastT
import dev.utils.app.AppUtils


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    @SuppressLint("WrongConstant", "VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        doOnceInDay("getSentence", action = {
            scopeNetLife {
                val content = Get<String>("https://v1.hitokoto.cn/?c=i&encode=text") {
                    setCacheMode(CacheMode.REQUEST_THEN_READ)
                }.await()
                Global.localSentence = content
            }
        })
        if (!checkPMProxy()) {
            alertDialog {
                title = "警告"
                message = "请使用官方正版软件"
                okButton("确定") {
                    AppUtils.uninstallApp(packageName)
                }
                isCancelable = false
            }.show()
        } else if (checkPMProxy()) {
            if (Global.isGuideAndFirstLaunch) {
                startActivity<WelcomeActivity>()
                finish()
            } else {
                if (atLeastT()) {
                    XXPermissions.with(this).permission(
                        Permission.READ_MEDIA_VIDEO,
                        Permission.READ_MEDIA_AUDIO,
                        Permission.READ_MEDIA_IMAGES
                    ).unchecked().interceptor(PermissionInterceptor()).request { permissions, all ->
                        if (all) {
                            initStartActivity()
                        } else {
                            XXPermissions.startPermissionActivity(this)
                        }
                    }
                } else {
                    XXPermissions.with(this).permission(
                        Permission.Group.STORAGE
                    ).unchecked().interceptor(PermissionInterceptor()).request { permissions, all ->
                        if (all) {
                            initStartActivity()
                            StorageManager.createAllFolder()
                        } else {
                            XXPermissions.startPermissionActivity(this)
                            toast("请检查存储权限是否正确授予")
                        }
                    }
                }
            }
        }
        pressBackTwiceToExitApp("再按一次退出")
    }

    private fun initStartActivity() {
        postDelay(500) {
            when (sp().getString(DEFAULT_LAUNCH, DEFAULT_LAUNCH_DEFAULT_VALUE)) {
                "main" -> {
                    startActivity<MainActivity>()
                    finish()
                }

                "movie" -> {
                    startActivity<MoviesMainActivity>()
                    finish()
                }

                "media_player" -> {
                    //toast("该选项暂不支持，已重置偏好设置，请重新启动应用")
                    startActivity<SearchMusicActivity>()
                    finish()
                    //sp().putString(DEFAULT_LAUNCH, DEFAULT_LAUNCH_DEFAULT_VALUE)
                }
            }
        }
    }

    /**
     * 检测 PackageManager 代理
     */
    @SuppressLint("PrivateApi")
    private fun checkPMProxy(): Boolean {
        val truePMName = "android.content.pm.IPackageManager\$Stub\$Proxy"
        var nowPMName = ""
        try {
            val packageManager = packageManager
            val mPMField = packageManager.javaClass.getDeclaredField("mPM")
            mPMField.isAccessible = true
            val mPM = mPMField[packageManager]!!
            nowPMName = mPM.javaClass.name
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return truePMName == nowPMName
    }
}
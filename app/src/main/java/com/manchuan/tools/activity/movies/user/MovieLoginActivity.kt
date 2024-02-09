package com.manchuan.tools.activity.movies.user

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.drake.softinput.setWindowSoftInput
import com.dylanc.longan.addNavigationBarHeightToMarginBottom
import com.dylanc.longan.doOnClick
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.textString
import com.dylanc.longan.topActivity
import com.itxca.spannablex.activateClick
import com.itxca.spannablex.spannable
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.lxj.androidktx.core.runOnUIThread
import com.manchuan.tools.activity.movies.model.QQLoginModel
import com.manchuan.tools.activity.user.ForgetPasswordActivity
import com.manchuan.tools.application.App
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityMovieLoginBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.startActivity
import com.manchuan.tools.interfaces.TencentLoginInterfaces
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.user.idVerify
import com.manchuan.tools.user.login
import com.manchuan.tools.user.tencentLoginSign
import com.manchuan.tools.user.tencentLoginUrl
import com.manchuan.tools.user.timeMills
import com.nowfal.kdroidext.kex.toast
import com.tencent.connect.common.Constants
import com.tencent.tauth.Tencent

class MovieLoginActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMovieLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!isAppDarkMode)
        binding.register.activateClick().text = spannable {
            "没有账号? 立即".text()
            "注册".span {
                color(colorPrimary())
                clickable(onClick = { view: View, s: String ->
                    startActivity<MovieRegisterActivity>()
                })
            }
        }
        setWindowSoftInput {  }
        binding.forgetPassword.setOnClickListener {
            startActivity<ForgetPasswordActivity>()
        }
        binding.toolbar.setNavigationOnClickListener {
            finishAfterTransition()
        }
        binding.qq.setOnClickListener {
            tencentLogin()
        }
        binding.login.doOnClick {
            if (binding.account.textString.isNotEmpty() && binding.password.textString.isNotEmpty()) {
                login(binding.account.textString, binding.password.textString, success = {
                    Global.token.value = it.msg.token
                    Global.userModel = it
                    PopTip.show("欢迎 ${it.msg.info.name}")
                    finishAfterTransition()
                }, failed = {
                    toast(it)
                })
            } else {
                toast("请填写完整")
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Tencent.onActivityResultData(requestCode, resultCode, data, listener)
        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_LOGIN) {
                Tencent.handleResultData(data, listener)
            }
        }
    }

    private val listener by lazy {
        TencentLoginInterfaces { qqLogin ->
            App.tencent.setAccessToken(qqLogin.access_token, qqLogin.expires_in.toString())
            App.tencent.openId = qqLogin.openid
            scopeNet {
                WaitDialog.show("正在登录")
                val string = Post<QQLoginModel>(tencentLoginUrl) {
                    param("openid", qqLogin.openid)
                    param("access_token", qqLogin.access_token)
                    param("qqappid", Global.AppId)
                    param("inv", "1")
                    param("markcode", idVerify)
                    param("t", timeMills)
                    param("sign", tencentLoginSign(qqLogin.openid, qqLogin.access_token, "1"))
                    converter = SerializationConverter("200", "code", "msg")
                }.await()
                Global.token.value = string.msg.token
                runOnUIThread {
                    TipDialog.show("登录成功", WaitDialog.TYPE.SUCCESS).dialogLifecycleCallback =
                        object : DialogLifecycleCallback<WaitDialog>() {
                            override fun onShow(dialog: WaitDialog?) {
                                super.onShow(dialog)
                            }

                            override fun onDismiss(dialog: WaitDialog?) {
                                super.onDismiss(dialog)
                                topActivity.finishAfterTransition()
                            }

                        }
                }
            }.catch { throwable ->
                throwable.printStackTrace()
                TipDialog.show(throwable.message, WaitDialog.TYPE.ERROR)
                App.tencent.logout(topActivity)
            }
        }
    }

    private fun tencentLogin() {
        Tencent.setIsPermissionGranted(true, Build.MODEL)
        Tencent.resetTargetAppInfoCache()
        Tencent.resetQQAppInfoCache()
        Tencent.resetTimAppInfoCache()
        if (!App.tencent.isSessionValid) {
            when (App.tencent.login(this, "all", listener)) {
                //下面为login可能返回的值的情况
                0 -> {

                }

                1 -> {
                    //loge("开始登录")
                }

                -1 -> {
                    toast("登录异常")
                }

                2 -> {

                }

                else -> {
                    toast("登录出错")
                }
            }
        }
    }

}
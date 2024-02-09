package com.manchuan.tools.activity.user

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.drake.interval.Interval
import com.dylanc.longan.isTextEmpty
import com.dylanc.longan.lifecycleOwner
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.lxj.androidktx.core.disable
import com.lxj.androidktx.core.enable
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityForgetPasswordBinding
import com.manchuan.tools.user.forgetPassword
import com.manchuan.tools.user.verifyCode
import java.util.concurrent.TimeUnit

class ForgetPasswordActivity : BaseActivity() {

    private val binding by viewBinding(ActivityForgetPasswordBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "找回密码"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.apply {
            getCode.setOnClickListener {
                if (user.isTextEmpty()) {
                    toast("请先填写邮箱")
                } else {
                    verifyCode(user.textString, "seek", success = {
                        toast(it.msg)
                        val interval = Interval(60, 1, TimeUnit.SECONDS, 1).life(lifecycleOwner)
                        interval.subscribe {
                            getCode.disable()
                            getCode.text = "${60 - it}秒"
                        }.finish {
                            getCode.text = "获取验证码"
                            getCode.enable()
                        }.start()
                    }, failed = {
                        toast(it)
                    })
                }
            }
            confirm.setOnClickListener {
                if (user.isTextEmpty() or verifyCode.isTextEmpty() or password.isTextEmpty()) {
                    toast("请先填写完整")
                } else {
                    forgetPassword(user.textString,
                        verifyCode.textString,
                        password.textString,
                        success = {
                            toast(it.msg)

                            finish()
                        },
                        failed = {
                            toast(it)
                        })
                }
            }
        }
    }
}
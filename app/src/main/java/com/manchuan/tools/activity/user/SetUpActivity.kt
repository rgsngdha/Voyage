package com.manchuan.tools.activity.user

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import com.drake.channel.sendEvent
import com.drake.engine.utils.throttleClick
import com.dylanc.longan.isContainsChinese
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivitySetUpBinding
import com.manchuan.tools.user.setUp

class SetUpActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySetUpBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "设置用户名与密码"
            setDisplayHomeAsUpEnabled(true)
        }
        onBackPressedDispatcher.addCallback(this) {
            toast("用户名与密码设置完成后自动退出")
        }
        binding.apply {
            aiPaint.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            user.doAfterTextChanged {
                it.toString().apply {
                    userInput.isErrorEnabled = true
                    if (isContainsChinese()) {
                        userInput.error = "用户名不能包含中文"
                    } else if (isBlank()) {
                        userInput.error = "不能含有特殊字符"
                    } else {
                        userInput.isErrorEnabled = false
                    }
                }
            }
            password.doAfterTextChanged {
                it.toString().apply {
                    passwordLay.isErrorEnabled = true
                    if (isContainsChinese()) {
                        passwordLay.error = "密码不能包含中文"
                    } else if (isBlank()) {
                        passwordLay.error = "不能含有特殊字符"
                    } else {
                        passwordLay.isErrorEnabled = false
                    }
                }
            }
            confirm.throttleClick {
                if (user.textString.isContainsChinese() or password.textString.isContainsChinese()) {
                    toast("用户名或密码不能包含中文")
                } else if (user.textString.isBlank() or password.textString.isBlank()) {
                    toast("用户名或密码不能包含特殊字符")
                } else {
                    setUp(Global.token.value.toString(),
                        user.textString,
                        password.textString,
                        success = {
                            toast(it.msg)
                            sendEvent("", "set_up_user")
                            finishAfterTransition()
                        },
                        failed = {
                            toast(it)
                        })
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> toast("用户名与密码设置完成后自动退出")
        }
        return super.onOptionsItemSelected(item)
    }

}
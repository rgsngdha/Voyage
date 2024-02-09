package com.manchuan.tools.activity.user

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.drake.channel.sendEvent
import com.drake.engine.utils.throttleClick
import com.dylanc.longan.isContainsChinese
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityAlertPassBinding
import com.manchuan.tools.user.alertPass
import com.manchuan.tools.user.userInfo

class AlertPassActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAlertPassBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "修改密码"
            setDisplayHomeAsUpEnabled(true)
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
        binding.apply {
            userInfo(Global.token.value.toString(), success = {
                if (it.msg.user.isNullOrEmpty()) {
                    sendEvent("", "set_up_user")
                    toast("您当前账号的用户名未设置，请设置后再修改密码")
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    user.setText(it.msg.user)
                }
            }, failed = {
                toast(it)
            })
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
                        passwordLay.error = "旧密码不能包含中文"
                    } else if (isBlank()) {
                        passwordLay.error = "不能含有特殊字符"
                    } else {
                        passwordLay.isErrorEnabled = false
                    }
                }
            }
            newPassword.doAfterTextChanged {
                it.toString().apply {
                    newPasswordLay.isErrorEnabled = true
                    if (isContainsChinese()) {
                        newPasswordLay.error = "新密码不能包含中文"
                    } else if (isBlank()) {
                        newPasswordLay.error = "不能含有特殊字符"
                    } else {
                        newPasswordLay.isErrorEnabled = false
                    }
                }
            }
            confirm.throttleClick {
                if (password.textString.isContainsChinese() or newPassword.textString.isContainsChinese()) {
                    toast("旧密码或新密码不能包含中文")
                } else if (password.textString.isBlank() or newPassword.textString.isBlank()) {
                    toast("旧密码或新密码不能包含特殊字符")
                } else {
                    alertPass(user.textString,
                        password.textString,
                        newPassword.textString,
                        success = {
                            toast(it.msg)
                            onBackPressedDispatcher.onBackPressed()
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
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}
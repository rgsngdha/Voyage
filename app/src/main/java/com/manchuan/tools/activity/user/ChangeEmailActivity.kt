package com.manchuan.tools.activity.user

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.drake.channel.sendEvent
import com.drake.engine.keyboard.VerificationCodeEditText
import com.drake.softinput.showSoftInput
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.isEmail
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityChangeEmailBinding
import com.manchuan.tools.user.bindEmail
import com.manchuan.tools.user.verifyCode

class ChangeEmailActivity : BaseActivity() {

    private val binding by lazy {
        ActivityChangeEmailBinding.inflate(layoutInflater)
    }

    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!isAppDarkMode)
        supportActionBar?.apply {
            title = "修改/绑定邮箱"
            setDisplayHomeAsUpEnabled(true)
        }
        onBackPressedDispatcher.addCallback(this) {
            finishAfterTransition()
        }
        binding.emailAddress.setEndIconOnClickListener {
            if (binding.email.textString.isEmail()) {
                verifyCode(binding.email.textString, "bind", success = {
                    binding.emailAddress.animateGone()
                    binding.verifyCode.animateVisible()
                    toast(it.msg)
                    email = binding.email.textString
                    binding.verifyCode.showSoftInput()
                }, failed = {
                    toast(it)
                })
            } else {
                toast("该邮件地址不符合规则")
            }
        }
        binding.verifyCode.setOnInputTextListener(object :
            VerificationCodeEditText.OnInputTextListener {
            override fun onInputTextComplete(text: CharSequence) {
                bindEmail(Global.token.value.toString(), email, text.toString(), success = {
                    finishAfterTransition()
                    sendEvent("", "change_email")
                }, failed = {
                    toast(it)
                })
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}
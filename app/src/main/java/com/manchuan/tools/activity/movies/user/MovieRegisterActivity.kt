package com.manchuan.tools.activity.movies.user

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.drake.engine.utils.throttleClick
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.manchuan.tools.databinding.ActivityMovieRegisterBinding
import com.manchuan.tools.user.register

class MovieRegisterActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMovieRegisterBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        val enter = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        window.enterTransition = enter
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener {
            finishAfterTransition()
        }
        binding.login.throttleClick {
            if (binding.account.isTextNotEmpty() && binding.password.isTextNotEmpty()) {
                WaitDialog.show("注册中...")
                register(binding.name.textString.ifEmpty { "未设置" },
                    binding.account.textString,
                    binding.password.textString,
                    binding.inviteId.textString.ifEmpty { "0" },
                    success = {
                        TipDialog.show(
                            "注册成功", WaitDialog.TYPE.SUCCESS
                        ).dialogLifecycleCallback = object : DialogLifecycleCallback<WaitDialog>() {
                            override fun onShow(dialog: WaitDialog?) {
                                super.onShow(dialog)
                            }

                            override fun onDismiss(dialog: WaitDialog?) {
                                super.onDismiss(dialog)
                                finishAfterTransition()
                            }
                        }
                    },
                    failed = {
                        TipDialog.show(it, WaitDialog.TYPE.ERROR)
                    })
            } else {
                toast("请填写完整")
            }
        }
    }
}
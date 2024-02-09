package com.manchuan.tools.fragment

import com.drake.engine.base.EngineFragment
import com.drake.engine.utils.throttleClick
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.dylanc.longan.textString
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.R
import com.manchuan.tools.activity.life.database.StepAccountAndPassword
import com.manchuan.tools.activity.life.database.StepDatabase
import com.manchuan.tools.activity.life.model.StepModel
import com.manchuan.tools.databinding.FragmentXiaoMiBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.text
import com.manchuan.tools.json.SerializationConverter
import com.skydoves.whatif.whatIfNotNull
import com.skydoves.whatif.whatIfNotNullOrEmpty

class XiaoMiFragment : EngineFragment<FragmentXiaoMiBinding>(R.layout.fragment_xiao_mi) {
    override fun initData() {

    }

    override fun initView() {
        binding.help.setOnClickListener {
            requireContext().alertDialog {
                title = "帮助"
                message =
                    "1.为什么修改步数之后没有生效\n" + "答：可能因为数据接口的原因，导致修改失败\n" + "\n" + "2.修改之后关联的其他平台会生效吗？\n" + "答：会，支持同步到微信、支付宝、QQ \n" + "\n" + " tips:如果在Zepp Life下拉刷新后数据长时间没有同步，请尝试解绑Zepp Life关联的第三方平台，再重新进行绑定。"
                okButton { }
            }.build()
        }
        binding.history.throttleClick {
            val accounts = arrayListOf<String>()
            StepDatabase.historyAccount.value?.let { accountAndPasswords ->
                accountAndPasswords.forEach {
                    accounts.add(it.account)
                }
            }
            requireContext().alertDialog {
                title = "历史账号"
                items(accounts) { dialog, index ->
                    StepDatabase.historyAccount.value.whatIfNotNullOrEmpty { accountAndPasswordList ->
                        accountAndPasswordList.find { it.account == accounts[index] }?.let {
                            binding.account.text(it.account)
                            binding.password.text(it.password)
                        }
                    }
                }
                cancelButton()
            }.build()
        }
        binding.editSteps.setOnClickListener {
            when {
                binding.account.text.toString().isEmpty() && binding.password.text.toString()
                    .isEmpty() && binding.steps.text.toString().isEmpty() -> {
                    binding.accountLay.error = "请填写小米运动账号"
                    binding.passwordLay.error = "请填写小米运动密码"
                    binding.stepsLay.error = "请填写步数"
                }

                binding.account.text.toString().isEmpty() -> {
                    binding.accountLay.error = "请填写小米运动账号"
                }

                binding.password.text.toString().isEmpty() -> {
                    binding.passwordLay.error = "请填写小米运动密码"
                }

                binding.steps.text.toString().isEmpty() -> {
                    binding.stepsLay.error = "请填写步数"
                }

                else -> {
                    if (binding.steps.text.toString().toInt() > 100000) {
                        PopTip.show("步数最多只能100000步")
                    } else {
                        scopeDialog {
                            val content = Post<StepModel>("https://api.pearktrue.cn/api/xiaomi/api.php") {
                                param("username", binding.account.textString)
                                param("password", binding.password.textString)
                                param("step", binding.steps.textString)
                                converter = SerializationConverter("200", "code", "msg")
                            }.await()
                            StepDatabase.historyAccount.value.whatIfNotNull {
                                if (it.find { item -> item.account == binding.account.textString } == null) {
                                    StepDatabase.historyAccount.value =
                                        StepDatabase.historyAccount.value?.apply {
                                            add(
                                                StepAccountAndPassword(
                                                    binding.account.textString,
                                                    binding.password.textString
                                                )
                                            )
                                        }
                                } else {
                                }
                            }
                            snack("${content.data.state}请自行打开Zepp Life在首页下拉刷新同步。")
                        }
                    }
                }
            }
        }
    }
}
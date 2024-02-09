package com.manchuan.tools.activity.app.fragments

import android.os.Bundle
import androidx.navigation.Navigation
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.lxj.androidktx.core.click
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.PrivacyActivity
import com.manchuan.tools.activity.app.SplashActivity
import com.manchuan.tools.activity.app.models.PrivacyModel
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.FragmentPrivaryBinding
import com.manchuan.tools.databinding.ItemPermissionBinding
import com.manchuan.tools.extensions.load


class PrivacyFragment : EngineFragment<FragmentPrivaryBinding>(R.layout.fragment_privary) {

    override fun onCreate(savedInstanceState: Bundle?) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
    }

    override fun initData() {

    }

    private var userAgreement = false
    private var privacy = false

    override fun initView() {
        val controller = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding.guidePermissionList.linear().divider {
            orientation = DividerOrientation.HORIZONTAL
            setDivider(4, true)
            endVisible = true
        }.setup {
            addType<PrivacyModel>(R.layout.item_permission)
            onBind {
                val binding = getBinding<ItemPermissionBinding>()
                val model = getModel<PrivacyModel>()
                binding.img.load(model.icon)
                binding.title.text = model.title
                binding.info.text = model.summary
                receiveEvent<Boolean>("agree${model.type}") {
                    if (it) {
                        binding.icon.load(R.drawable.ic_baseline_check_24)
                        when (model.type) {
                            1 -> privacy = true
                            2 -> userAgreement = true
                        }
                    }
                }
            }
            onClick(R.id.card) {
                val binding = getBinding<ItemPermissionBinding>()
                val model = getModel<PrivacyModel>()
                startActivity<PrivacyActivity>("type" to model.type, "isGuide" to true)
            }
        }.models = listOf(
            PrivacyModel(
                R.drawable.newspaper_variant_outline, "隐私政策", "请阅读并同意隐私政策", 1
            ),
            PrivacyModel(R.drawable.shield_account_outline, "用户协议", "请阅读并同意用户协议", 2)
        )
        binding.guidePermissionBack.click {
            controller.navigate(R.id.guidePermissionFragment)
        }
        binding.guidePermissionNext.click {
            if (privacy && userAgreement) {
                Global.isGuideAndFirstLaunch = false
                startActivity<SplashActivity>()
                requireActivity().finish()
            } else {
                toast("请先阅读并同意${if (privacy.not() && userAgreement.not()) "《隐私政策》与《用户协议》" else if (privacy.not()) "《隐私政策》" else "《用户协议》"}")
            }
        }
    }

}
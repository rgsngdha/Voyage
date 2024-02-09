package com.manchuan.tools.activity.app.fragments

import android.os.Bundle
import androidx.navigation.Navigation
import com.blankj.utilcode.util.AppUtils
import com.drake.engine.base.EngineFragment
import com.drake.engine.utils.throttleClick
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.lxj.androidktx.core.click
import com.manchuan.tools.R
import com.manchuan.tools.databinding.FragmentWelcomeBinding


class WelcomeFragment : EngineFragment<FragmentWelcomeBinding>(R.layout.fragment_welcome) {

    override fun onCreate(savedInstanceState: Bundle?) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
    }

    override fun initData() {

    }

    override fun initView() {
        val controller = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding.guideGetStart.click {
            controller.navigate(R.id.guidePermissionFragment)
        }
        binding.guideExit.throttleClick {
            AppUtils.exitApp()
        }
    }

}
package com.manchuan.tools.activity.app.fragments

import android.os.Bundle
import androidx.navigation.Navigation
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.mutable
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.toast
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.androidktx.core.click
import com.lxj.androidktx.core.drawable
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.models.PermissionGuide
import com.manchuan.tools.databinding.FragmentPermissionBinding
import com.manchuan.tools.databinding.ItemPermissionBinding
import com.manchuan.tools.extensions.load
import com.manchuan.tools.utils.atLeastT

class PermissionFragment : EngineFragment<FragmentPermissionBinding>(R.layout.fragment_permission) {

    override fun onCreate(savedInstanceState: Bundle?) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        super.onCreate(savedInstanceState)
    }


    private val permissions = arrayListOf<String>()

    override fun initData() {

    }

    override fun initView() {
        val controller = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding.guidePermissionList.linear().divider {
            orientation = DividerOrientation.HORIZONTAL
            setDivider(4, true)
            endVisible = true
        }.setup {
            addType<PermissionGuide>(R.layout.item_permission)
            onBind {
                val binding = getBinding<ItemPermissionBinding>()
                val model = getModel<PermissionGuide>()
                binding.img.load(model.icon)
                binding.title.text = model.title
                binding.info.text = model.summary
                if (XXPermissions.isGranted(requireContext(), model.permission)) {
                    binding.icon.load(R.drawable.ic_baseline_check_24)
                } else {
                    binding.icon.load(R.drawable.baseline_close_24)
                }
                if (model == this@PermissionFragment.binding.guidePermissionList.mutable.last()) {
                    permissions.clear()
                    this@PermissionFragment.binding.guidePermissionList.mutable.forEach {
                        (it as PermissionGuide).permission.forEach { permission ->
                            permissions.add(permission)
                        }
                    }
                }
            }
            onClick(R.id.card) {
                val binding = getBinding<ItemPermissionBinding>()
                val model = getModel<PermissionGuide>()
                XXPermissions.with(this@PermissionFragment).permission(model.permission)
                    .request { permissions, allGranted ->
                        if (allGranted) {
                            binding.icon.setImageDrawable(drawable(R.drawable.ic_baseline_check_24))
                        } else {
                            binding.icon.setImageDrawable(drawable(R.drawable.baseline_close_24))
                        }
                    }
                if (model == this@PermissionFragment.binding.guidePermissionList.mutable.last()) {
                    permissions.clear()
                    this@PermissionFragment.binding.guidePermissionList.mutable.forEach {
                        (it as PermissionGuide).permission.forEach { permission ->
                            permissions.add(permission)
                        }
                    }
                }
            }
        }.models = if (atLeastT()) listOf(
            PermissionGuide(
                R.drawable.ic_outline_image_24,
                "访问照片和视频",
                "访问你设备上的照片以及保存本软件使用过程中产生的图片",
                Permission.READ_MEDIA_IMAGES,
                Permission.READ_MEDIA_VIDEO
            ), PermissionGuide(
                R.drawable.music_note_music,
                "访问音频",
                "访问设备上的音频",
                Permission.READ_MEDIA_AUDIO
            )
        ) else listOf(
            PermissionGuide(
                R.drawable.ic_primary_storage_24,
                "存储权限",
                "读取您手机存储中的相关内容",
                *Permission.Group.STORAGE
            )
        )
        binding.guidePermissionBack.click {
            controller.navigate(R.id.guideWelcomeFragment)
        }
        binding.guidePermissionNext.click {
            if (XXPermissions.isGranted(requireContext(), permissions)) {
                controller.navigate(R.id.guidePrivacyFragment)
            } else {
                toast("请先授予相关权限")
            }
        }
    }


}
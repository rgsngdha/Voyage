package com.manchuan.tools.activity.app.fragments

import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.dylanc.longan.arguments
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.models.AppPermissionModel
import com.manchuan.tools.activity.app.models.PermissionNameModel
import com.manchuan.tools.databinding.FragmentAppPermissionBinding
import com.manchuan.tools.databinding.ItemAppPermissionBinding
import com.manchuan.tools.extensions.json
import com.manchuan.tools.extensions.readRaw
import dev.utils.app.info.AppInfoUtils

class AppPermissionFragment :
    EngineFragment<FragmentAppPermissionBinding>(R.layout.fragment_app_permission) {


    private val packageName by arguments<String>("packageName")

    override fun initData() {

    }

    override fun initView() {
        val permissions =
            json.decodeFromString<PermissionNameModel>(requireContext().readRaw(R.raw.permissions))
        binding.recyclerView.linear().setup {
            addType<AppPermissionModel>(R.layout.item_app_permission)
            onBind {
                val binding = getBinding<ItemAppPermissionBinding>()
                val model = getModel<AppPermissionModel>()
                binding.permissionTitle.text =
                    permissions.permissionList.find { it.key == model.permission }?.title
                        ?: "Unknown"
                binding.summary.text =
                    permissions.permissionList.find { it.key == model.permission }?.memo
                        ?: "Unknown"
                binding.description.text = model.permission
            }
        }
        val permissionsList = arrayListOf<AppPermissionModel>()
        AppInfoUtils.getAppPermission(packageName).forEach {
            permissionsList.add(AppPermissionModel(it))
        }
        binding.recyclerView.models = permissionsList
    }

}
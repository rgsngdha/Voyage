package com.manchuan.tools.activity.site

import android.os.Bundle
import com.crazylegend.viewbinding.viewBinding
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.click
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityAddSitesBinding
import com.manchuan.tools.user.addSites
import com.manchuan.tools.user.userInfo
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
import com.wajahatkarim3.easyvalidation.core.view_ktx.validUrl

class AddSitesActivity : BaseActivity() {

    private val binding by viewBinding(ActivityAddSitesBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "添加网站"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.apply {
            add.click {
                if (inputName.nonEmpty {
                        inputNameLay.error = it
                    } || inputDescription.nonEmpty {
                        inputDescriptionLay.error = it
                    } || inputUrl.validUrl { inputUrlLay.error = it }) {
                    WaitDialog.show("添加中...")
                    userInfo(Global.token.value.toString(), success = {
                        addSites(Global.token.value.toString(),
                            inputName.textString,
                            inputDescription.textString,
                            inputUrl.textString,
                            inputImage.textString,
                            it.msg.name,
                            (if (it.msg.email != null) it.msg.email else "").toString(),
                            it.msg.id,
                            success = {
                                toast(it.msg)
                                WaitDialog.dismiss()
                            },
                            failed = {
                                WaitDialog.dismiss()
                                toast(it)
                            })
                    }, failed = {
                        toast(it)
                        WaitDialog.dismiss()
                    })
                } else {
                    toast("请填写完整")
                }
            }
        }
    }


}
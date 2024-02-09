package com.manchuan.tools.activity.hide

import android.content.Intent
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import com.drake.brv.annotaion.DividerOrientation
import com.drake.brv.utils.divider
import com.drake.brv.utils.grid
import com.drake.brv.utils.setup
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.WaitDialog
import com.manchuan.tools.R
import com.manchuan.tools.activity.hide.model.HideWebModel
import com.manchuan.tools.activity.site.WebActivity
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityHideFunctionsBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.doOnCancel
import com.manchuan.tools.extensions.doOnDismiss
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.model.MiuiShortcutModel
import com.manchuan.tools.user.verifyRole
import com.manchuan.tools.utils.UiUtils
import com.skydoves.whatif.whatIfNotNullOrEmpty

class HideFunctionsActivity : BaseActivity() {

    private val binding by lazy {
        ActivityHideFunctionsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        immerseStatusBar(!UiUtils.isDarkMode())
        binding.toolbar.addStatusBarHeightToMarginTop()
        supportActionBar?.apply {
            title = "隐藏功能"
            setDisplayHomeAsUpEnabled(true)
        }
        Global.token.value.whatIfNotNullOrEmpty {
            WaitDialog.show("验证权限")
            verifyRole(it, success = {
                WaitDialog.dismiss()
            }, failed = {
                Global.isEnabledHideFunction.value = false
                toast("无权限")
                finish()
            })
        }
        if (Global.isEnabledHideFunction.value == true) {
            binding.recyclerView.grid(2).divider {
                orientation = DividerOrientation.GRID
                includeVisible = true
                setDivider(12, true)
            }.divider {
                orientation = DividerOrientation.VERTICAL
                includeVisible = true
                setDivider(16, true)
            }.setup {
                addType<HideWebModel>(R.layout.item_hide_web)
                onClick(R.id.shortcut) {
                    val model = getModel<HideWebModel>()
                    runCatching {
                        startActivity<WebActivity>("url" to model.url)
                    }.onFailure {
                        toast("跳转失败")
                    }
                }
                onLongClick(R.id.shortcut) {
                    runCatching {
                        val intent = Intent(applicationContext, WebActivity::class.java)
                        intent.addCategory(Intent.CATEGORY_LAUNCHER)
                        intent.putExtra("url", getModel<HideWebModel>().url)
                        val shortcut = ShortcutInfoCompat.Builder(context, modelPosition.toString())
                            .setShortLabel(getModel<MiuiShortcutModel>().name)
                            .setLongLabel(getModel<MiuiShortcutModel>().name).setIntent(intent)
                            .build()
                        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
                    }.onFailure {
                        toast("快捷方式添加失败")
                    }.onSuccess {
                        toast("快捷方式添加成功,在桌面长按本应用图标即可显示")
                    }
                }
            }.models = mutableListOf(
                HideWebModel("短信测压平台", "https://run.ialtone.xyz/message/"),
                HideWebModel("极速接码平台", "https://smsjisu.com/"),
                HideWebModel("DDOS平台", "http://www.hacku.cn/i/DDoS/"),
                HideWebModel("电话轰炸", "https://smsbomber.online/call-bomber.php"),
                HideWebModel("邮箱轰炸", "https://www.skyju.cc/mailhzj.html#"),
                HideWebModel("高精度IP定位", "https://www.opengps.cn/Data/IP/LocHighAcc.aspx"),
                HideWebModel("子域名查询", "https://chaziyu.com/"),
                HideWebModel("长途电话区号查询", "https://www.ip138.com/post/"),
                HideWebModel("手机号码生成器", "https://vip.uutool.cn/maker"),
                HideWebModel("身份证查询归属地", "https://qq.ip138.com/idsearch/"),
                HideWebModel("身份证批量校验", "https://uutool.cn/idcard-batch/"),
                HideWebModel("IP地址生成", "https://uutool.cn/ip-generate/"),
                HideWebModel("IP地址转化", "https://www.dute.org/ip-to-number"),
                HideWebModel("身份证正反面生成", "https://www.socarchina.com/m/sfz/index.php"),
                HideWebModel("手机号归属地批量查询", "https://uutool.cn/phone-batch/"),
                HideWebModel("IP定位", "https://www.opengps.cn/Data/IP/IPSearch.aspx"),
                HideWebModel("IP归属地查询", "https://www.ip138.com/"),
                HideWebModel("微信三点定位", "http://anxin360.com/weixinsandian/"),
                HideWebModel("变速短信轰炸", "https://mytoolstown.com/smsbomber/"),
                HideWebModel("虚拟身份生成", "https://www.iculture.cc/demo/fakeID/"),
                HideWebModel("手机号定位", "https://anxin360.com/Phone/"),
                HideWebModel("IP旁站查询", "https://chapangzhan.com/"),
            )
        } else {
            alertDialog {
                title = "警告"
                message = "您尚未激活隐藏功能，无法使用。"
                isCancelable = false
                okButton {
                    finishAfterTransition()
                }
            }.build().doOnCancel {
                finishAfterTransition()
            }.doOnDismiss {
                finishAfterTransition()
            }
        }
    }
}
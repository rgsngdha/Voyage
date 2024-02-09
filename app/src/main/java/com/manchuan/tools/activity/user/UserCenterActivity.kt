package com.manchuan.tools.activity.user

import android.animation.LayoutTransition
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.engine.utils.throttleClick
import com.drake.net.utils.scope
import com.dylanc.longan.activityresult.launch
import com.dylanc.longan.activityresult.registerForCropPictureResult
import com.dylanc.longan.addStatusBarHeightToMarginTop
import com.dylanc.longan.context
import com.dylanc.longan.immerseStatusBar
import com.dylanc.longan.isAppDarkMode
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.dylanc.longan.topActivity
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.curDay
import com.lxj.androidktx.core.curMonth
import com.lxj.androidktx.core.curYear
import com.lxj.androidktx.core.dp
import com.lxj.androidktx.core.getDateDay
import com.lxj.androidktx.core.getDateMonth
import com.lxj.androidktx.core.getDateYear
import com.lxj.androidktx.core.toDateString
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.user.MovieLoginActivity
import com.manchuan.tools.activity.site.WebActivity
import com.manchuan.tools.application.App
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.cache.glide.key.GlideKey
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.ActivityUserCenterBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.getPath
import com.manchuan.tools.extensions.inputDialog
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.textCopyThenPost
import com.manchuan.tools.user.alertName
import com.manchuan.tools.user.cardPayByToken
import com.manchuan.tools.user.sign
import com.manchuan.tools.user.timeMills
import com.manchuan.tools.user.uploadAvatar
import com.manchuan.tools.user.userInfo
import com.manchuan.tools.utils.UiUtils
import com.maxkeppeler.sheets.core.IconButton
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText
import com.skydoves.whatif.whatIfNotNullOrEmpty
import rikka.material.app.MaterialActivity
import java.io.File
import java.time.LocalDate


class UserCenterActivity : BaseActivity() {

    private val binding by lazy {
        ActivityUserCenterBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immerseStatusBar(!isAppDarkMode)
        binding.apply {
            setSupportActionBar(toolbar)
            aiPaint.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            scroller.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            toolbar.addStatusBarHeightToMarginTop()
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "个人中心"
        }
        binding.info.linear().divider {
            setDrawable(R.drawable.divider_horizontal)
            includeVisible = false
        }.setup {
            addType<InfoFunctions>(R.layout.item_user_info)
            R.id.item.onClick {
                val model = getModel<InfoFunctions>()
                model.unit.invoke()
            }
        }
        binding.security.linear().divider {
            setDrawable(R.drawable.divider_horizontal)
            includeVisible = false
        }.setup {
            addType<InfoFunctions>(R.layout.item_user_info)
            R.id.item.onClick {
                val model = getModel<InfoFunctions>()
                model.unit.invoke()
            }
        }
        receiveEvent<String>("change_email") {
            refreshUser()
        }
        receiveEvent<String>("set_up_user") {
            refreshUser()
        }
        refreshUser()
    }

    data class InfoFunctions(
        @DrawableRes var icon: Int,
        val title: String,
        val summary: String = "",
        var unit: () -> Unit,
    )

    private fun refreshUser() {
        userInfo(Global.token.value.toString(), success = {
            scope {
                binding.apply {
                    if (it.msg.user.isNullOrEmpty()) {
                        alertDialog {
                            title = "安全提醒"
                            message = "您当前账号的用户名或密码未设置，请点击确定前往设置。"
                            okButton {
                                startActivity<SetUpActivity>()
                            }
                            isCancelable = false
                        }.build()
                    }
                    name.text("Hi，${it.msg.name}")
                    account.text(if (it.msg.user.isNullOrEmpty()) "未设置" else "@${it.msg.user}")
                    avatar.load(
                        it.msg.pic,
                        placeholder = R.drawable.logo_avatar_anonymous_40dp,
                        roundRadius = 16.dp,
                        isCrossFade = true,
                        skipMemory = true,
                        isForceOriginalSize = true,
                        signature = Global.avatarSignature
                    )
                    avatar.throttleClick {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    if (it.msg.diary == "y") {
                        sign.setIconResource(R.drawable.ic_baseline_check_24)
                        sign.text = "已签到"
                    } else {
                        sign.throttleClick {
                            sign(Global.token.value.toString(), success = {
                                toast(it.msg)
                                refreshUser()
                            }, failed = {
                                toast(it)
                            })
                        }
                    }
                    val regTime = (it.msg.regTime.toLong() * 1000L)
                    info.models = listOf(
                        InfoFunctions(R.drawable.ic_account_circle_outline, "用户UID", it.msg.id) {
                            textCopyThenPost(it.msg.id)
                        },
                        InfoFunctions(
                            R.drawable.ic_round_date_range_24,
                            "注册时间",
                            (it.msg.regTime.toLong() * 1000L).toDateString("yyyy-MM-dd HH:mm")
                        ) {
                            alertDialog {
                                title = "注册时间"
                                message =
                                    "账号注册时间:${regTime.toDateString("yyyy-MM-dd HH:mm")}\n您的账号已陪伴远航:${
                                        LocalDate.of(curYear, curMonth, curDay)
                                            .toEpochDay() - LocalDate.of(
                                            regTime.getDateYear(),
                                            regTime.getDateMonth(),
                                            regTime.getDateDay()
                                        ).toEpochDay()
                                    }天"
                                okButton {}
                            }.build()
                        },
                        InfoFunctions(R.drawable.ic_primary_card_giftcard_24, "积分", it.msg.fen) {
                            textCopyThenPost(it.msg.fen)
                        },
                        InfoFunctions(
                            R.drawable.outline_wallet_24,
                            "会员",
                            if (it.msg.vip == "999999999") "永久会员" else (it.msg.vip.toLong() * 1000L).toDateString(
                                "yyyy-MM-dd HH:mm"
                            )
                        ) {
                            inputDialog(
                                "会员卡激活",
                                "请在下方输入您捐赠获得的卡密并激活。\n如果卡密失效，请联系我们解决。\nQQ:3299699002  微信:mysteryoflovem\n邮箱:3299699002@qq.com"
                            ) { string ->
                                Global.token.value.whatIfNotNullOrEmpty { token ->
                                    cardPayByToken(token, card = string, success = {
                                        snack(it.msg)
                                        refreshUser()
                                    }, failed = {
                                        snack(it)
                                    })
                                }
                            }
                        },
                    )
                    security.models = listOf(
                        InfoFunctions(
                            R.drawable.outline_email_24,
                            "邮箱",
                            if (it.msg.email.isNullOrEmpty()) "未设置" else "已设置"
                        ) {
                            alertDialog {
                                title = "邮箱"
                                message =
                                    "您的邮箱地址为：${if (it.msg.email.isNullOrEmpty()) "无" else it.msg.email}"
                                okButton {}
                                cancelButton("修改") {
                                    startActivity<ChangeEmailActivity>()
                                }
                            }.build()
                        },
                        InfoFunctions(
                            R.drawable.ic_lock_outline, "密码", "修改密码"
                        ) {
                            startActivity<AlertPassActivity>()
                        },
                    )
                    userCard.throttleClick {
                        inputDialog("修改昵称", "昵称不得包含违法内容，否则发现直接封禁账号。") { it
                            if (it.isNotBlank()) {
                                WaitDialog.show("请求中...")
                                alertName(Global.token.value.toString(), it, success = {
                                    TipDialog.show(it.msg, WaitDialog.TYPE.SUCCESS)
                                    refreshUser()
                                    sendEvent(true, "refreshUser")
                                }, failed = {
                                    TipDialog.show(it, WaitDialog.TYPE.ERROR)
                                })
                            } else {
                                toast("昵称不能为空")
                            }
                        }
                    }
                }
            }
        }, failed = {
            binding.state.showError()
            toast(it)
            startActivity<MovieLoginActivity>()
            finish()
            Global.token.value = ""
        })
        binding.exitLogin.throttleClick {
            Global.token.value = ""
            App.tencent.logout(context)
            finish()
        }
    }

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            cropPictureLauncher.launch(
                uri,
                "crop" to "true",
                "aspectX" to 1,
                "aspectY" to 1,
                "outputX" to "512",
                "outputY" to "512"
            )
        } else {
            //Log.d("PhotoPicker", "No media selected")
        }
    }

    private val cropPictureLauncher = registerForCropPictureResult { result ->
        if (result != null) {
            WaitDialog.show("上传中...")
            uploadAvatar(Global.token.value.toString(), File(getPath(result)), success = {
                TipDialog.show(it.msg, WaitDialog.TYPE.SUCCESS)
                toast(it.msg)
                sendEvent(true, "refreshUser")
                Global.avatarSignature = GlideKey(timeMills.toString())
                refreshUser()
                File(getPath(result)).delete()
            }, failed = {
                TipDialog.show(it, WaitDialog.TYPE.ERROR)
                toast(it)
            })
        }
        // 处理 uri
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
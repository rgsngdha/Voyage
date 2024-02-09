package com.manchuan.tools.fragment.mains

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.drake.channel.receiveEvent
import com.drake.channel.sendEvent
import com.drake.engine.base.EngineFragment
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.roundCorners
import com.dylanc.longan.startActivity
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.curDay
import com.lxj.androidktx.core.curMonth
import com.lxj.androidktx.core.curYear
import com.lxj.androidktx.core.dp
import com.lxj.androidktx.core.getDateDay
import com.lxj.androidktx.core.getDateMonth
import com.lxj.androidktx.core.getDateYear
import com.lxj.androidktx.core.gone
import com.lxj.androidktx.core.toDateString
import com.lxj.androidktx.core.visible
import com.manchuan.tools.R
import com.manchuan.tools.about.AboutActivity
import com.manchuan.tools.activity.movies.fragments.model.AccountWallpaper
import com.manchuan.tools.activity.movies.user.MovieLoginActivity
import com.manchuan.tools.activity.user.AlertPassActivity
import com.manchuan.tools.activity.user.ChangeEmailActivity
import com.manchuan.tools.activity.user.SetUpActivity
import com.manchuan.tools.activity.user.UserCenterActivity
import com.manchuan.tools.application.App
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.FragmentAccountBinding
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.inputDialog
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.snack
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.textCopyThenPost
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.settings.SettingsActivity
import com.manchuan.tools.user.alertName
import com.manchuan.tools.user.cardPayByToken
import com.manchuan.tools.user.sign
import com.manchuan.tools.user.userInfo
import com.mcxiaoke.koi.log.loge
import com.nowfal.kdroidext.kex.toast
import com.skydoves.whatif.whatIfNotNullOrEmpty
import java.time.LocalDate

class AccountFragment : EngineFragment<FragmentAccountBinding>(R.layout.fragment_account) {

    override fun initData() {
    }


    override fun onResume() {
        super.onResume()
        binding.background.resume()
    }

    override fun onStop() {
        super.onStop()
        binding.background.pause()
    }

    override fun initView() {
        binding.accountLayout.roundCorners = 24F
        binding.gradient.roundCorners = 24F
        binding.info.linear().divider {
            setDrawable(R.drawable.divider_horizontal)
            includeVisible = false
        }.setup {
            addType<UserCenterActivity.InfoFunctions>(R.layout.item_user_info)
            R.id.item.onClick {
                val model = getModel<UserCenterActivity.InfoFunctions>()
                model.unit.invoke()
            }
        }
        binding.security.linear().divider {
            setDrawable(R.drawable.divider_horizontal)
            includeVisible = false
        }.setup {
            addType<UserCenterActivity.InfoFunctions>(R.layout.item_user_info)
            R.id.item.onClick {
                val model = getModel<UserCenterActivity.InfoFunctions>()
                model.unit.invoke()
            }
        }
        binding.settings.linear().divider {
            setDrawable(R.drawable.divider_horizontal)
            includeVisible = false
        }.setup {
            addType<UserCenterActivity.InfoFunctions>(R.layout.item_user_info)
            R.id.item.onClick {
                val model = getModel<UserCenterActivity.InfoFunctions>()
                model.unit.invoke()
            }
        }.models =
            listOf(UserCenterActivity.InfoFunctions(R.drawable.outline_settings_24, "设置", "") {
                startActivity<SettingsActivity>()
            }, UserCenterActivity.InfoFunctions(R.drawable.outline_info_24, "关于", "") {
                startActivity<AboutActivity>()
            })
        Global.token.observe(viewLifecycleOwner) {
            if (it.isNullOrBlank()) {
                binding.accountLayout.throttleClick {
                    startActivity<MovieLoginActivity>()
                }
                binding.userSettings.gone()
                binding.exitLogin.gone()
                binding.name.text("未登录")
                binding.account.text("点我即可前往登录")
                binding.sign.animateGone()
                binding.avatar.load(
                    R.drawable.logo_avatar_anonymous_40dp,
                    isCrossFade = true,
                    skipMemory = true,
                    diskCacheStrategy = DiskCacheStrategy.NONE
                )
                App.tencent.logout(context)
                binding.avatar.throttleClick {

                }
            } else {
                refreshUser()
            }
        }
        receiveEvent<String>("change_email") {
            refreshUser()
        }
        receiveEvent<String>("set_up_user") {
            refreshUser()
        }
        scopeNetLife {
            val model =
                Get<AccountWallpaper>("http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1") {
                    converter = SerializationConverter("", "", "")
                }.await().images.first()
            binding.background.load("http://s.cn.bing.net${model.url}", isCrossFade = true)
        }

    }

    private fun refreshUser() {
        loge("刷新用户", "触发刷新用户事件")
        userInfo(Global.token.value.toString(), success = {
            binding.apply {
                binding.userSettings.visible()
                binding.exitLogin.visible()
                binding.sign.animateVisible()
                if (it.msg.user.isNullOrEmpty()) {
                    requireContext().alertDialog {
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
                    isForceOriginalSize = true,
                    signature = Global.avatarSignature
                )
                avatar.throttleClick {
                    toast("上传头像请点击侧滑栏头像区域上传，下次上传可点击顶部标题栏打开策划栏")
                    sendEvent("", "open_drawer")
                    //pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
                    UserCenterActivity.InfoFunctions(
                        R.drawable.ic_account_circle_outline, "用户UID", it.msg.id
                    ) {
                        requireContext().textCopyThenPost(it.msg.id)
                    },
                    UserCenterActivity.InfoFunctions(
                        R.drawable.ic_round_date_range_24,
                        "注册时间",
                        (it.msg.regTime.toLong() * 1000L).toDateString("yyyy-MM-dd HH:mm")
                    ) {
                        requireContext().alertDialog {
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
                    UserCenterActivity.InfoFunctions(
                        R.drawable.ic_primary_card_giftcard_24, "积分", it.msg.fen
                    ) {
                        requireContext().textCopyThenPost(it.msg.fen)
                    },
                    UserCenterActivity.InfoFunctions(
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
                security.models = listOf(UserCenterActivity.InfoFunctions(
                    R.drawable.outline_email_24,
                    "邮箱",
                    if (it.msg.email.isNullOrEmpty()) "未设置" else "已设置"
                ) {
                    requireContext().alertDialog {
                        title = "邮箱"
                        message =
                            "您的邮箱地址为：${if (it.msg.email.isNullOrEmpty()) "无" else it.msg.email}"
                        okButton {}
                        cancelButton("修改") {
                            startActivity<ChangeEmailActivity>()
                        }
                    }.build()
                }, UserCenterActivity.InfoFunctions(
                    R.drawable.ic_lock_outline, "密码", "修改密码"
                ) {
                    startActivity<AlertPassActivity>()
                }, UserCenterActivity.InfoFunctions(
                    R.drawable.qqchat,
                    "QQ",
                    if (it.msg.openidQq.isNullOrEmpty()) "未绑定" else "已绑定"
                ) {
                    if (it.msg.openidQq.isNullOrEmpty()) {
                        sendEvent(true, "tencent_bind")
                    }
                })
                accountLayout.throttleClick {
                    inputDialog("修改昵称", "昵称不得包含违法内容，否则发现直接封禁账号。") { it1 ->
                        if (it1.isNotBlank()) {
                            WaitDialog.show("请求中...")
                            alertName(Global.token.value.toString(), it1, success = {
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
        }, failed = {
            toast(it)
            App.tencent.logout(context)
            Global.token.value = ""
        })
        binding.exitLogin.throttleClick {
            App.tencent.logout(context)
            Global.token.value = ""
        }
        receiveEvent<Boolean>("refreshUserByBind") {
            refreshUser()
        }
    }


}
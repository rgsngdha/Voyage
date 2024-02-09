package com.manchuan.tools.activity.movies.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.drake.engine.base.EngineFragment
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.dp
import com.dylanc.longan.roundCorners
import com.dylanc.longan.toast
import com.itxca.spannablex.spannable
import com.lxj.androidktx.core.animateGone
import com.lxj.androidktx.core.animateVisible
import com.lxj.androidktx.core.toDateString
import com.manchuan.tools.R
import com.manchuan.tools.activity.movies.MovieDownloadActivity
import com.manchuan.tools.activity.movies.SubscribeManageActivity
import com.manchuan.tools.activity.movies.fragments.model.AccountWallpaper
import com.manchuan.tools.activity.movies.model.FunctionModel
import com.manchuan.tools.activity.movies.settings.MoviesSettingsActivity
import com.manchuan.tools.activity.movies.user.MovieLoginActivity
import com.manchuan.tools.activity.user.UserCenterActivity
import com.manchuan.tools.database.Global
import com.manchuan.tools.databinding.FragmentMovieAccountBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.gradientOfBottomAndTop
import com.manchuan.tools.extensions.isColorLight
import com.manchuan.tools.extensions.load
import com.manchuan.tools.extensions.loge
import com.manchuan.tools.extensions.startActivity
import com.manchuan.tools.extensions.text
import com.manchuan.tools.extensions.windowBackground
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.user.userInfo


class MovieAccountFragment :
    EngineFragment<FragmentMovieAccountBinding>(R.layout.fragment_movie_account) {

    @SuppressLint("SetTextI18n")
    override fun initView() {
        val animationSet = AnimationSet(false)
        val scale = ScaleAnimation(1F, 1.2F, 1F, 1.2F)
        scale.repeatCount = -1
        scale.repeatMode = Animation.REVERSE
        scale.duration = 5000
        animationSet.addAnimation(scale)
        binding.backgroundLay.startAnimation(scale)
        binding.number.roundCorners = 4.dp
        binding.info.roundCorners = 4.dp
        binding.info.setTextColor(if (colorPrimary().isColorLight) Color.BLACK else Color.WHITE)
        binding.number.setTextColor(if (colorPrimary().isColorLight) Color.BLACK else Color.WHITE)
        binding.number.setBackgroundColor(colorPrimary())
        binding.info.setBackgroundColor(colorPrimary())
        binding.gradient.gradientOfBottomAndTop(windowBackground(), 0x00000000)
        Global.token.observe(this) { s ->
            if (s.isNullOrEmpty()) {
                binding.avatar.load(
                    "https://q1.qlogo.cn/g?b=qq&nk=3299699002&s=640", isCrossFade = true
                )
                binding.name.text("登录")
                binding.avatarContainer.setOnClickListener {
                    requireActivity().startActivity<MovieLoginActivity>()
                }
                binding.number.animateGone()
                binding.info.animateGone()
            } else {
                userInfo(s, success = {
                    binding.apply {
                        binding.number.animateVisible()
                        binding.info.animateVisible()
                        avatarContainer.throttleClick {
                            startActivity<UserCenterActivity>()
                        }
                        avatar.load(
                            it.msg.pic,
                            isCrossFade = true,
                            skipMemory = true,
                            isForceOriginalSize = true,
                            signature = Global.avatarSignature
                        )
                        name.text = it.msg.name
                        loge(it.msg.vip.toLong())
                        number.text = spannable {
                            "No.${it.msg.id}".span {
                                style(Typeface.ITALIC)
                            }
                        }
                        info.text = "会员到期时间:${
                            if (it.msg.vip == "999999999") "永久会员" else (it.msg.vip.toLong() * 1000L).toDateString(
                                "yyyy-MM-dd HH:mm"
                            )
                        }"
                    }
                }, {
                    toast(it)
                    Global.token.value = ""
                })
            }
        }
        binding.recyclerView.linear().setup {
            addType<FunctionModel>(R.layout.layout_item_action)
            setAnimation(AnimationType.ALPHA)
            R.id.item.onClick {
                val model = getModel<FunctionModel>()
                model.unit.invoke()
            }
        }.models = functionsModel()
    }

    override fun onResume() {
        super.onResume()
        binding.background.resume()
    }

    override fun onStop() {
        super.onStop()
        binding.background.pause()
    }

    private fun functionsModel(): MutableList<FunctionModel> {
        return mutableListOf(FunctionModel(R.drawable.subscriptions_material, "订阅管理") {
            requireActivity().startActivity<SubscribeManageActivity>()
        }, FunctionModel(R.drawable.material_download, "下载管理") {
            requireActivity().startActivity<MovieDownloadActivity>()
        }, FunctionModel(R.drawable.settings_material, "设置") {
            requireActivity().startActivity<MoviesSettingsActivity>()
        })
    }

    override fun initData() {
        scopeNetLife {
            val model =
                Get<AccountWallpaper>("http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1") {
                    converter = SerializationConverter("", "", "")
                }.await().images.first()
            binding.background.load("http://s.cn.bing.net${model.url}", isCrossFade = true)
        }
    }

}
package com.manchuan.tools.about

import android.annotation.SuppressLint
import android.os.SystemClock
import android.widget.ImageView
import android.widget.TextView
import com.dylanc.longan.appVersionName
import com.dylanc.longan.doOnClick
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.manchuan.tools.R
import com.manchuan.tools.activity.site.WebActivity
import com.manchuan.tools.extensions.isSpringOrYuanXiao
import com.nowfal.kdroidext.kex.string


class AboutActivity : AbsAboutActivity() {

    private val counts = 7
    private fun continuousClick() {
        //每次点击时，数组向前移动一位
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        //为数组最后一位赋值
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] >= SystemClock.uptimeMillis() - DURATION) {
            mHits = LongArray(counts) //重新初始化数组
            //startActivity<PlatLogoActivity>()
            if (isSpringOrYuanXiao()) {
                startActivity<WebActivity>("url" to "https://firework.fengmuchuan.cn")
            } else {
                toast("暂无彩蛋可触发")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        icon.setImageResource(R.mipmap.ic_voyage)
        icon.doOnClick {
            continuousClick()
        }
        slogan.text = "远航工具箱"
        version.text = "v$appVersionName".replace("PREVIEW", "先行版")
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.add(Category("介绍与帮助"))
        items.add(Card("本应用是作者利用闲暇时间制作的，如有任何问题请联系作者反馈。"))
        items.add(Category("开发组"))
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=3299699002&s=640",
                "航",
                "开发 & 设计",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=3299699002"
            )
        )
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=2899738115&s=640",
                "春秋",
                "接口提供",
                "https://ahsp.app/"
            )
        )
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=2727901961&s=640",
                "悍匪",
                "脚本提供",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=2727901961"
            )
        )
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=3373587110&s=640",
                "帕帝天秀",
                "接口提供",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=3373587110"
            )
        )
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=2246921312&s=640",
                "Tom",
                "接口提供",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=2246921312"
            )
        )
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=1652965610&s=640",
                "Name",
                "接口提供",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=1652965610"
            )
        )
        items.add(Category("测试组"))
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=3469677218&s=640",
                "祈雾色",
                "测试人员",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=3469677218"
            )
        )
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=1187877808&s=640",
                "op 一枚",
                "测试人员",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=1187877808"
            )
        )
        items.add(
            Contributor(
                "https://q1.qlogo.cn/g?b=qq&nk=2535765666&s=640",
                "T",
                "测试人员",
                "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin=2535765666"
            )
        )
        items.add(Card("每个版本的发布都离不开以上人员及开发组的测试，如有任何问题请联系作者反馈。"))
        items.add(Category("加入我们"))
        items.add(
            Contributor(
                "",
                "QQ",
                "远航",
                "mqqapi://card/show_pslcard?src_type=internal&version=1&uin=754591110&card_type=group&source=qrcode"
            )
        )
        items.add(
            Contributor(
                "", string(R.string.qq_guild), "远航 Voyage", "https://pd.qq.com/s/28o0btlxe"
            )
        )
        items.add(Category("开源许可"))
        items.add(
            License(
                "MultiType", "drakeet", License.APACHE_2, "https://github.com/drakeet/MultiType"
            )
        )
        items.add(
            License(
                "about-page", "drakeet", License.APACHE_2, "https://github.com/drakeet/about-page"
            )
        )
        items.add(
            License(
                "DialogX", "Kongzue", License.APACHE_2, "https://github.com/kongzue/DialogX"
            )
        )
        items.add(
            License(
                "BRV", "liangjingkanji", License.APACHE_2, "https://github.com/liangjingkanji/BRV"
            )
        )
        items.add(
            License(
                "Net", "liangjingkanji", License.APACHE_2, "https://github.com/liangjingkanji/Net"
            )
        )
        items.add(
            License(
                "AndroidHiddenApiBypass",
                "LSPosed",
                License.APACHE_2,
                "https://github.com/LSPosed/AndroidHiddenApiBypass"
            )
        )
    }
}
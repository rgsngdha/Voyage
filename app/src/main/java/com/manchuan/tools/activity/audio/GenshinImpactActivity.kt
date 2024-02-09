package com.manchuan.tools.activity.audio

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import com.crazylegend.viewbinding.viewBinding
import com.drake.engine.utils.throttleClick
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.dylanc.longan.context
import com.dylanc.longan.isTextNotEmpty
import com.dylanc.longan.textString
import com.dylanc.longan.toast
import com.kongzue.dialogx.dialogs.WaitDialog
import com.lxj.androidktx.core.animateVisible
import com.lzx.starrysky.StarrySky
import com.lzx.starrysky.control.RepeatMode
import com.manchuan.tools.R
import com.manchuan.tools.activity.audio.model.GenshinImpact
import com.manchuan.tools.base.BaseActivity
import com.manchuan.tools.databinding.ActivityGenshinImpactBinding
import com.manchuan.tools.extensions.publicAudiosDirPath
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.user.timeMills
import com.mcxiaoke.koi.ext.addToMediaStore
import java.io.File
import java.util.concurrent.TimeUnit

/**
# 原神语音生成
 */

class GenshinImpactActivity : BaseActivity() {

    private val binding by viewBinding(ActivityGenshinImpactBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        with(binding) {
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                title = "原神语音生成"
                setDisplayHomeAsUpEnabled(true)
            }
            val formats = ArrayAdapter(context, R.layout.cat_exposed_dropdown_popup_item, actors)
            actorsSpinner.setAdapter(formats)
            create.throttleClick {
                if (content.isTextNotEmpty() or (content.length() <= 300)) {
                    scopeNetLife {
                        WaitDialog.show("生成中")
                        val audio = Get<GenshinImpact>("https://api.lolimi.cn/API/yyhc/y.php") {
                            param("msg", content.textString)
                            param("speaker", actorsSpinner.textString)
                            param("noisew", noisew.textString)
                            param("sdp", sdp.textString)
                            param("noise", noise.textString)
                            setClient {
                                connectTimeout(5, TimeUnit.MINUTES)
                                writeTimeout(5, TimeUnit.MINUTES)
                                readTimeout(5, TimeUnit.MINUTES)
                            }
                            converter = SerializationConverter("1", "code", "msg")
                        }.await()
                        WaitDialog.dismiss()
                        StarrySky.with().setRepeatMode(RepeatMode.REPEAT_MODE_NONE, false)
                        StarrySky.with().playMusicByUrl(audio.music)
                        download.animateVisible()
                        download.throttleClick {
                            scopeNetLife {
                                val file = Get<File>(audio.music) {
                                    setDownloadDir(publicAudiosDirPath)
                                    setDownloadFileName("Voyage_$timeMills.wav")
                                }.await()
                                addToMediaStore(file)
                                toast("下载完成")
                            }
                        }
                    }.catch {
                        WaitDialog.dismiss()
                        toast(it.message)
                    }
                } else {
                    toast("请先输入语音内容")
                }
            }
        }
    }

    private val actors = arrayOf(
        "派蒙",
        "空",
        "荧",
        "纳西妲",
        "阿贝多",
        "温迪",
        "枫原万叶",
        "钟离",
        "荒泷一斗",
        "八重神子",
        "艾尔海森",
        "提纳里",
        "迪希雅",
        "卡维",
        "宵宫",
        "莱依拉",
        "赛诺",
        "诺艾尔",
        "托马",
        "凝光",
        "莫娜",
        "北斗",
        "神里绫华",
        "雷电将军",
        "芭芭拉",
        "鹿野院平藏",
        "五郎",
        "迪奥娜",
        "凯亚",
        "安柏",
        "班尼特",
        "琴",
        "柯莱",
        "夜兰",
        "妮露",
        "辛焱",
        "珐露珊",
        "魈",
        "香菱",
        "达达利亚",
        "砂糖",
        "早柚",
        "云堇",
        "刻晴",
        "丽莎",
        "迪卢克",
        "烟绯",
        "重云",
        "珊瑚宫心海",
        "胡桃",
        "可莉",
        "流浪者",
        "久岐忍",
        "神里绫人",
        "甘雨",
        "戴因斯雷布",
        "优菈",
        "菲谢尔",
        "行秋",
        "白术",
        "九条裟罗",
        "雷泽",
        "申鹤",
        "迪娜泽黛",
        "凯瑟琳",
        "多莉",
        "坎蒂丝",
        "萍姥姥",
        "罗莎莉亚",
        "留云借风真君",
        "绮良良",
        "瑶瑶",
        "七七",
        "奥兹",
        "米卡",
        "夏洛蒂",
        "埃洛伊",
        "博士",
        "女士",
        "大慈树王",
        "三月七",
        "娜塔莎",
        "希露瓦",
        "虎克",
        "克拉拉",
        "丹恒",
        "希儿",
        "布洛妮娅",
        "瓦尔特",
        "杰帕德",
        "佩拉",
        "姬子",
        "艾丝妲",
        "白露",
        "星",
        "穹",
        "桑博",
        "伦纳德",
        "停云",
        "罗刹",
        "卡芙卡",
        "彦卿",
        "史瓦罗",
        "螺丝咕姆",
        "阿兰",
        "银狼",
        "素裳",
        "丹枢",
        "黑塔",
        "景元",
        "帕姆",
        "可可利亚",
        "半夏",
        "符玄",
        "公输师傅",
        "奥列格",
        "青雀",
        "大毫",
        "青镞",
        "费斯曼",
        "绿芙蓉",
        "镜流",
        "信使",
        "丽塔",
        "失落迷迭",
        "缭乱星棘",
        "伊甸",
        "伏特加女孩",
        "狂热蓝调",
        "莉莉娅",
        "萝莎莉娅",
        "八重樱",
        "八重霞",
        "卡莲",
        "第六夜想曲",
        "卡萝尔",
        "姬子",
        "极地战刃",
        "布洛妮娅",
        "次生银翼",
        "理之律者",
        "真理之律者",
        "迷城骇兔",
        "希儿",
        "魇夜星渊",
        "黑希儿",
        "帕朵菲莉丝",
        "天元骑英",
        "幽兰黛尔",
        "德丽莎",
        "月下初拥",
        "朔夜观星",
        "暮光骑士",
        "明日香",
        "李素裳",
        "格蕾修",
        "梅比乌斯",
        "渡鸦",
        "人之律者",
        "爱莉希雅",
        "爱衣",
        "天穹游侠",
        "琪亚娜",
        "空之律者",
        "终焉之律者",
        "薪炎之律者",
        "云墨丹心",
        "符华",
        "识之律者",
        "维尔薇",
        "始源之律者",
        "芽衣",
        "雷之律者",
        "苏莎娜",
        "阿波尼亚",
        "陆景和",
        "莫弈",
        "夏彦",
        "左然"
    )

}
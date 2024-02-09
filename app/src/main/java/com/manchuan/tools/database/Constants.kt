package com.manchuan.tools.database

import android.content.ComponentName
import android.content.Intent
import com.drake.net.Get
import com.drake.net.utils.scopeNet
import com.dylanc.longan.startActivity
import com.dylanc.longan.toast
import com.dylanc.longan.topActivity
import com.dylanc.longan.topActivityOrApplication
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.AppManagerActivity
import com.manchuan.tools.activity.audio.AudioFormatActivity
import com.manchuan.tools.activity.audio.CompressAudioActivity
import com.manchuan.tools.activity.audio.DenoiseActivity
import com.manchuan.tools.activity.audio.FormatAudioActivity
import com.manchuan.tools.activity.audio.FormatPcmActivity
import com.manchuan.tools.activity.audio.GenshinImpactActivity
import com.manchuan.tools.activity.audio.PerfectTuneActivity
import com.manchuan.tools.activity.audio.SearchMusicActivity
import com.manchuan.tools.activity.crypt.AesCrypt
import com.manchuan.tools.activity.crypt.BaseConvertActivity
import com.manchuan.tools.activity.crypt.DesCrypt
import com.manchuan.tools.activity.crypt.EncryptMD5Activity
import com.manchuan.tools.activity.crypt.HmacMD5Activity
import com.manchuan.tools.activity.crypt.HmacSHACrypt
import com.manchuan.tools.activity.crypt.Md2CryptActivity
import com.manchuan.tools.activity.crypt.RC4EDActivity
import com.manchuan.tools.activity.crypt.SHACrypt
import com.manchuan.tools.activity.game.HonorKingsPowerActivity
import com.manchuan.tools.activity.game.McServerInfoActivity
import com.manchuan.tools.activity.images.AiAvatarActivity
import com.manchuan.tools.activity.images.AiPaintActivity
import com.manchuan.tools.activity.images.AvatarsCategoryActivity
import com.manchuan.tools.activity.images.BlackGreyPhotoActivity
import com.manchuan.tools.activity.images.CompressActivity
import com.manchuan.tools.activity.images.CreateColorImageActivity
import com.manchuan.tools.activity.images.ExifEditActivity
import com.manchuan.tools.activity.images.HideImageActivity
import com.manchuan.tools.activity.images.ImageCryptActivity
import com.manchuan.tools.activity.images.ImageParagraphActivity
import com.manchuan.tools.activity.images.ImageToUrlActivity
import com.manchuan.tools.activity.images.LowPolyActivity
import com.manchuan.tools.activity.images.PhoneHistoryActivity
import com.manchuan.tools.activity.images.PhotoMiaoActivity
import com.manchuan.tools.activity.images.PhotoTextActivity
import com.manchuan.tools.activity.images.PhotoWaterMarkActivity
import com.manchuan.tools.activity.images.PicturePixelActivity
import com.manchuan.tools.activity.images.WallpaperCategoryActivity
import com.manchuan.tools.activity.life.AiStoryActivity
import com.manchuan.tools.activity.life.BaiduLibraryActivity
import com.manchuan.tools.activity.life.CreatePasswordActivity
import com.manchuan.tools.activity.life.DogParagraphActivity
import com.manchuan.tools.activity.life.HistoryTodayActivity
import com.manchuan.tools.activity.life.LocationInquireActivity
import com.manchuan.tools.activity.life.MarQueeActivity
import com.manchuan.tools.activity.life.MetalDetectionActivity
import com.manchuan.tools.activity.life.MovieLinesSearchActivity
import com.manchuan.tools.activity.life.NoiseMeasurementActivity
import com.manchuan.tools.activity.life.PostalCodeActivity
import com.manchuan.tools.activity.life.RanDomArticleActivity
import com.manchuan.tools.activity.life.RulerActivity
import com.manchuan.tools.activity.life.StepsActivity
import com.manchuan.tools.activity.life.TelephoneActivity
import com.manchuan.tools.activity.life.TimeActivity
import com.manchuan.tools.activity.life.TranslateActivity
import com.manchuan.tools.activity.normal.ChinaMobileActivity
import com.manchuan.tools.activity.normal.CowFileActivity
import com.manchuan.tools.activity.normal.DecimalConvertActivity
import com.manchuan.tools.activity.normal.GithubDownloadActivity
import com.manchuan.tools.activity.normal.LanzouActivity
import com.manchuan.tools.activity.normal.QRCodeActivity
import com.manchuan.tools.activity.normal.RanDomColorActivity
import com.manchuan.tools.activity.normal.SpecialTextActivity
import com.manchuan.tools.activity.site.BeianActivity
import com.manchuan.tools.activity.site.PingActivity
import com.manchuan.tools.activity.site.PortsScanActivity
import com.manchuan.tools.activity.site.QueryIpActivity
import com.manchuan.tools.activity.site.ServerInfoActivity
import com.manchuan.tools.activity.site.WakeOnLanActivity
import com.manchuan.tools.activity.system.BackupWordLibrary
import com.manchuan.tools.activity.system.CustomModelActivity
import com.manchuan.tools.activity.system.FontScale
import com.manchuan.tools.activity.system.FtpServiceActivity
import com.manchuan.tools.activity.system.HDRCheckActivity
import com.manchuan.tools.activity.system.MIUIShortCutActivity
import com.manchuan.tools.activity.system.MediaScannerActivity
import com.manchuan.tools.activity.touch.MainActivity
import com.manchuan.tools.activity.video.AspectActivity
import com.manchuan.tools.activity.video.BrightActivity
import com.manchuan.tools.activity.video.CompressVideoActivity
import com.manchuan.tools.activity.video.ContrastActivity
import com.manchuan.tools.activity.video.FormatVideoActivity
import com.manchuan.tools.activity.video.H264Activity
import com.manchuan.tools.activity.video.HlsActivity
import com.manchuan.tools.activity.video.M3U8Activity
import com.manchuan.tools.activity.video.MakeMuteActivity
import com.manchuan.tools.activity.video.MuteVideoActivity
import com.manchuan.tools.activity.video.ReverseActivity
import com.manchuan.tools.activity.video.ShortVideoActivity
import com.manchuan.tools.activity.video.TOGifActivity
import com.manchuan.tools.activity.video.TOImageActivity
import com.manchuan.tools.activity.video.VideoWallpaperActivity
import com.manchuan.tools.activity.video.YUVideoActivity
import com.manchuan.tools.activity.video.anime.TraceMoeActivity
import com.manchuan.tools.activity.video.download.M3u8Activity
import com.manchuan.tools.extensions.alertDialog
import com.manchuan.tools.extensions.cancelButton
import com.manchuan.tools.extensions.floating.life.TimeFloating
import com.manchuan.tools.extensions.okButton
import com.manchuan.tools.extensions.textCopyThenPost
import com.manchuan.tools.fragment.json.models.ShiCiModels
import com.manchuan.tools.fragment.model.FunctionModel
import com.manchuan.tools.interfaces.HardwarePresenter
import com.manchuan.tools.json.SerializationConverter
import com.manchuan.tools.utils.CommonFunctions
import com.manchuan.tools.utils.RootCmd
import com.manchuan.tools.utils.SettingsLoader
import org.koin.java.KoinJavaComponent

const val amapKey = "721350d737fcd3a80f40738e87c4e36e"
const val DEFAULT_LAUNCH = "default_launch_mode"
const val DEFAULT_LAUNCH_DEFAULT_VALUE = "main"

//日常
val dailyTool = mutableListOf(FunctionModel("刻度尺") {
    startActivity(Intent(topActivity, RulerActivity::class.java))
}, FunctionModel("AI故事") {
    startActivity(Intent(topActivity, AiStoryActivity::class.java))
}, FunctionModel("原神语音生成") {
    startActivity(Intent(topActivity, GenshinImpactActivity::class.java))
}, FunctionModel("百度题库") {
    startActivity(Intent(topActivity, BaiduLibraryActivity::class.java))
}, FunctionModel("影视台词搜寻") {
    startActivity(Intent(topActivity, MovieLinesSearchActivity::class.java))
}, FunctionModel("随机诗词佳句文案") {
    scopeNet {
        val model = Get<ShiCiModels>("https://zj.v.api.aa1.cn/api/wenan-shici/?type=json") {
            converter = SerializationConverter("", "", "")
        }.await()
        topActivityOrApplication.alertDialog {
            title = "诗词佳句"
            message = model.msg
            okButton(R.string.copy) {
                topActivityOrApplication.textCopyThenPost(model.msg)
            }
            cancelButton()
        }.build()
    }.catch {
        topActivityOrApplication.toast(it.toString())
    }
}, FunctionModel("随机皮皮的话") {
    scopeNet {
        val model = Get<ShiCiModels>("https://zj.v.api.aa1.cn/api/wenan-pp/?type=json") {
            converter = SerializationConverter("", "", "")
        }.await()
        topActivityOrApplication.alertDialog {
            title = "皮皮的话"
            message = model.msg
            okButton(R.string.copy) {
                topActivityOrApplication.textCopyThenPost(model.msg)
            }
            cancelButton()
        }.build()
    }.catch {
        topActivityOrApplication.toast(it.toString())
    }
}, FunctionModel("随机励志哲理文案") {
    scopeNet {
        val model = Get<ShiCiModels>("https://zj.v.api.aa1.cn/api/wenan-zl/?type=json") {
            converter = SerializationConverter("", "", "")
        }.await()
        topActivityOrApplication.alertDialog {
            title = "励志哲理"
            message = model.msg
            okButton(R.string.copy) {
                topActivityOrApplication.textCopyThenPost(model.msg)
            }
            cancelButton()
        }.build()
    }.catch {
        topActivityOrApplication.toast(it.toString())
    }
}, FunctionModel("随机唯美文案") {
    scopeNet {
        val model = Get<ShiCiModels>("https://zj.v.api.aa1.cn/api/wenan-wm/?type=json") {
            converter = SerializationConverter("", "", "")
        }.await()
        topActivityOrApplication.alertDialog {
            title = "唯美文案"
            message = model.msg
            okButton(R.string.copy) {
                topActivityOrApplication.textCopyThenPost(model.msg)
            }
            cancelButton()
        }.build()
    }.catch {
        topActivityOrApplication.toast(it.toString())
    }
}, FunctionModel("噪音测量") {
    startActivity(Intent(topActivity, NoiseMeasurementActivity::class.java))
}, FunctionModel("狗屁不通文章生成器") {
    startActivity(Intent(topActivity, DogParagraphActivity::class.java))
}, FunctionModel("进制转换") {
    startActivity<DecimalConvertActivity>()
}, FunctionModel("手机平板默认壁纸") {
    startActivity(Intent(topActivity, PhoneHistoryActivity::class.java))
}, FunctionModel("视频壁纸") {
    startActivity<VideoWallpaperActivity>()
}, FunctionModel("Google翻译") {
    startActivity(Intent(topActivity, TranslateActivity::class.java))
}, FunctionModel("时间悬浮窗") {
    TimeFloating.show(topActivityOrApplication)
}, FunctionModel("强密码生成") {
    startActivity(Intent(topActivity, CreatePasswordActivity::class.java))
}, FunctionModel("时间屏幕") {
    startActivity(Intent(topActivity, TimeActivity::class.java))
}, FunctionModel("金属探测器") {
    startActivity(Intent(topActivity, MetalDetectionActivity::class.java))
}, FunctionModel("历史上的今天") {
    startActivity(Intent(topActivity, HistoryTodayActivity::class.java))
}, FunctionModel("经纬度查询") {
    startActivity(Intent(topActivity, LocationInquireActivity::class.java))
}, FunctionModel("滚动字幕") {
    startActivity(Intent(topActivity, MarQueeActivity::class.java))
}, FunctionModel("步数修改") {
    startActivity(Intent(topActivity, StepsActivity::class.java))
}, FunctionModel("QQ快捷跳转") {
    CommonFunctions().openQQFunctions(topActivityOrApplication)
}, FunctionModel("随机颜色") {
    startActivity(Intent(topActivity, RanDomColorActivity::class.java))
}, FunctionModel("随机一文") {
    startActivity(Intent(topActivity, RanDomArticleActivity::class.java))
}, FunctionModel("支付宝到账音效") {
    CommonFunctions().alipayAudio(topActivityOrApplication)
}, FunctionModel("聚合短视频解析") {
    startActivity(Intent(topActivity, ShortVideoActivity::class.java))
}, FunctionModel("聚合图集解析") {
    startActivity(Intent(topActivity, ImageParagraphActivity::class.java))
}, FunctionModel("Github加速下载") {
    startActivity(Intent(topActivity, GithubDownloadActivity::class.java))
}, FunctionModel("蓝奏云解析") {
    startActivity(Intent(topActivity, LanzouActivity::class.java))
}, FunctionModel("移动网盘解析") {
    startActivity(Intent(topActivity, ChinaMobileActivity::class.java))
}, FunctionModel("奶牛快传解析") {
    startActivity(Intent(topActivity, CowFileActivity::class.java))
}, FunctionModel("AI绘图") {
    startActivity(Intent(topActivity, AiPaintActivity::class.java))
})

//查询
val queryTool = mutableListOf(FunctionModel("以图搜番") {
    startActivity<TraceMoeActivity>()
}, FunctionModel("手机号归属地查询") {
    startActivity(Intent(topActivity, TelephoneActivity::class.java))
}, FunctionModel("MC服务器信息查询") {
    startActivity(Intent(topActivity, McServerInfoActivity::class.java))
}, FunctionModel("最低战力查询") {
    startActivity(
        Intent(
            topActivity, HonorKingsPowerActivity::class.java
        )
    )
}, FunctionModel("音乐搜索器") {
    startActivity(Intent(topActivity, SearchMusicActivity::class.java))
}, FunctionModel("全球IP查询") {
    startActivity(Intent(topActivity, QueryIpActivity::class.java))
}, FunctionModel("邮政编码查询") {
    startActivity(Intent(topActivity, PostalCodeActivity::class.java))
})

//站长
val siteTool = mutableListOf(FunctionModel("Ping") {
    startActivity(Intent(topActivity, PingActivity::class.java))
}, FunctionModel("网站ICP备案查询") {
    startActivity(Intent(topActivity, BeianActivity::class.java))
}, FunctionModel("服务器信息查询") {
    startActivity(Intent(topActivity, ServerInfoActivity::class.java))
}, FunctionModel("端口扫描") {
    startActivity(Intent(topActivity, PortsScanActivity::class.java))
}, FunctionModel("LAN唤醒") {
    startActivity(Intent(topActivity, WakeOnLanActivity::class.java))
})

//解码
val transformerTool = mutableListOf(FunctionModel("AES加解密") {
    startActivity(Intent(topActivity, AesCrypt::class.java))
}, FunctionModel("RC4加解密") {
    startActivity(Intent(topActivity, RC4EDActivity::class.java))
}, FunctionModel("DES加解密") {
    startActivity(Intent(topActivity, DesCrypt::class.java))
}, FunctionModel("Base64加解密") {
    startActivity(Intent(topActivity, BaseConvertActivity::class.java))
}, FunctionModel("MD5加密") {
    startActivity(Intent(topActivity, EncryptMD5Activity::class.java))
}, FunctionModel("MD2加密") {
    startActivity(Intent(topActivity, Md2CryptActivity::class.java))
}, FunctionModel("SHA加密") {
    startActivity(Intent(topActivity, SHACrypt::class.java))
}, FunctionModel("HmacMD5加密") {
    startActivity(Intent(topActivity, HmacMD5Activity::class.java))
}, FunctionModel("HmacSHA加密") {
    startActivity(Intent(topActivity, HmacSHACrypt::class.java))
}, FunctionModel("特殊文本生成") {
    startActivity(Intent(topActivity, SpecialTextActivity::class.java))
})

//系统
private val hardwareFloat: HardwarePresenter by KoinJavaComponent.inject(HardwarePresenter::class.java)

val systemTool = mutableListOf(FunctionModel("FTP局域网文件共享") {
    startActivity<FtpServiceActivity>()
}, FunctionModel("系统界面调节工具") {
    startActivity(
        Intent().setComponent(
            ComponentName(
                "com.android.systemui", "com.android.systemui.DemoMode"
            )
        )
    )
}, FunctionModel("应用管理") {
    startActivity(Intent(topActivity, AppManagerActivity::class.java))
}, FunctionModel("硬件信息悬浮窗") {
    hardwareFloat.loadContext(topActivityOrApplication)
}, FunctionModel("网速悬浮窗") {
    hardwareFloat.loadNetworkFloat(topActivityOrApplication)
}, FunctionModel("屏幕HDR检测") {
    startActivity(Intent(topActivity, HDRCheckActivity::class.java))
}, FunctionModel("媒体库刷新") {
    startActivity(Intent(topActivity, MediaScannerActivity::class.java))
}, FunctionModel("高级重启") {
    val items = arrayOf<CharSequence>(
        "重启",
        "软重启",
        "重启系统用户界面",
        "安全模式",
        "快速重启",
        "Recovery",
        "高通9008",
        "FastBoot"
    )
    BottomMenu.show(items).setTitle("高级重启").onMenuItemClickListener =
        OnMenuItemClickListener { _: BottomMenu?, _: CharSequence?, index: Int ->
            when (items[index].toString()) {
                "重启" -> if (RootCmd.haveRoot()) {
                    RootCmd.runRootCommand("reboot")
                } else {
                    PopTip.show(R.string.no_root)
                }

                "安全模式" -> if (RootCmd.haveRoot()) {
                    RootCmd.runRootCommand("su -c setprop persist.sys.safemode 1\nreboot")
                } else {
                    PopTip.show(R.string.no_root)
                }

                "快速重启" -> if (RootCmd.haveRoot()) {
                    RootCmd.runRootCommand("reboot -p")
                } else {
                    PopTip.show(R.string.no_root)
                }

                "Recovery" -> if (RootCmd.haveRoot()) {
                    RootCmd.runRootCommand("reboot recovery")
                } else {
                    PopTip.show(R.string.no_root)
                }

                "高通9008" -> if (RootCmd.haveRoot()) {
                    RootCmd.runRootCommand("reboot edl")
                } else {
                    PopTip.show(R.string.no_root)
                }

                "FastBoot" -> if (RootCmd.haveRoot()) {
                    RootCmd.runRootCommand("reboot fastboot")
                } else {
                    PopTip.show(R.string.no_root)
                }

                "软重启" -> if (RootCmd.haveRoot()) {
                    RootCmd.runRootCommand("setprop ctl.restart zygote")
                } else {
                    PopTip.show(R.string.no_root)
                }

                "重启系统用户界面" -> if (RootCmd.haveRoot()) {
                    RootCmd.runRootCommand("pkill -f com.android.systemui")
                } else {
                    PopTip.show(R.string.no_root)
                }
            }
            false
        }
}, FunctionModel("MIUI快捷方式") {
    startActivity(Intent(topActivity, MIUIShortCutActivity::class.java))
}, FunctionModel("电量伪装") {
    CommonFunctions().setDumpsysBattery(topActivity)
}, FunctionModel("查看设备信息") {
    SettingsLoader.deviceInfoDialog(topActivityOrApplication)
}, FunctionModel("多点触控测试") {
    startActivity(Intent(topActivity, MainActivity::class.java))
}, FunctionModel("保存手机壁纸") {
    CommonFunctions().saveWallpaper(topActivity)
}, FunctionModel("系统字体大小调节") {
    startActivity(Intent(topActivity, FontScale::class.java))
}, FunctionModel("振动器") {
    CommonFunctions().vibrateFunction(topActivity)
}, FunctionModel("备份字库") {
    startActivity(Intent(topActivity, BackupWordLibrary::class.java))
}, FunctionModel("Magisk机型修改模块生成") {
    startActivity(Intent(topActivity, CustomModelActivity::class.java))
})

//媒体
val mediaTool = mutableListOf(FunctionModel("压缩视频") {
    startActivity<CompressVideoActivity>()
}, FunctionModel("自定义频率音频") {
    startActivity<PerfectTuneActivity>()
}, FunctionModel("压缩音频") {
    startActivity<CompressAudioActivity>()
}, FunctionModel("视频提取音频") {
    startActivity(Intent(topActivity, AudioFormatActivity::class.java))
}, FunctionModel("M3U8视频下载") {
    startActivity(Intent(topActivity, M3u8Activity::class.java))
}, FunctionModel("音频格式转换") {
    startActivity<FormatAudioActivity>()
}, FunctionModel("视频格式转换") {
    startActivity<FormatVideoActivity>()
}, FunctionModel("视频转GIF") {
    startActivity<TOGifActivity>()
}, FunctionModel("视频转图片") {
    startActivity<TOImageActivity>()
}, FunctionModel("视频静音") {
    startActivity<MuteVideoActivity>()
}, FunctionModel("设置视频屏幕高宽比") {
    startActivity<AspectActivity>()
}, FunctionModel("音频提取PCM") {
    startActivity<FormatPcmActivity>()
}, FunctionModel("视频解码YUV") {
    startActivity<YUVideoActivity>()
}, FunctionModel("视频编码H264") {
    startActivity<H264Activity>()
}, FunctionModel("生成静音音频") {
    startActivity<MakeMuteActivity>()
}, FunctionModel("修改视频对比度") {
    startActivity<ContrastActivity>()
}, FunctionModel("修改视频亮度") {
    startActivity<BrightActivity>()
}, FunctionModel("视频降噪") {
    startActivity<DenoiseActivity>()
}, FunctionModel("视频切片") {
    startActivity<HlsActivity>()
}, FunctionModel("M3U8切片转视频") {
    startActivity<M3U8Activity>()
}, FunctionModel("视频倒放") {
    startActivity<ReverseActivity>()
})

//图片
val imageTool = mutableListOf(FunctionModel("二维码生成") {
    startActivity(Intent(topActivity, QRCodeActivity::class.java))
}, FunctionModel("AI头像") {
    startActivity(Intent(topActivity, AiAvatarActivity::class.java))
}, FunctionModel("图片Base64加解密") {
    startActivity(Intent(topActivity, ImageCryptActivity::class.java))
}, FunctionModel("隐藏图制作") {
    startActivity(Intent(topActivity, HideImageActivity::class.java))
}, FunctionModel("纯色图生成") {
    startActivity(Intent(topActivity, CreateColorImageActivity::class.java))
}, FunctionModel("LowPoly图片生成") {
    startActivity(Intent(topActivity, LowPolyActivity::class.java))
}, FunctionModel("图片压缩") {
    startActivity(Intent(topActivity, CompressActivity::class.java))
}, FunctionModel("图片灰白化") {
    startActivity(Intent(topActivity, BlackGreyPhotoActivity::class.java))
}, FunctionModel("图片文字化") {
    startActivity(Intent(topActivity, PhotoTextActivity::class.java))
}, FunctionModel("图片像素化") {
    startActivity(Intent(topActivity, PicturePixelActivity::class.java))
}, FunctionModel("照片信息编辑") {
    startActivity(Intent(topActivity, ExifEditActivity::class.java))
}, FunctionModel("图片转链接") {
    startActivity(Intent(topActivity, ImageToUrlActivity::class.java))
}, FunctionModel("图片水印") {
    startActivity(Intent(topActivity, PhotoWaterMarkActivity::class.java))
}, FunctionModel("图片素描") {
    startActivity(Intent(topActivity, PhotoMiaoActivity::class.java))
}, FunctionModel("壁纸大全") {
    startActivity(
        Intent(
            topActivity, WallpaperCategoryActivity::class.java
        )
    )
}, FunctionModel("头像大全") {
    startActivity(Intent(topActivity, AvatarsCategoryActivity::class.java))
})
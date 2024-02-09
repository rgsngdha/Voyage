package com.manchuan.tools.activity.info

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coder.ffmpeg.annotation.CodecAttribute
import com.coder.ffmpeg.jni.FFmpegCommand
import com.gyf.immersionbar.ktx.immersionBar
import com.itxca.spannablex.spannable
import com.manchuan.tools.BuildConfig
import com.manchuan.tools.databinding.ActivityAppLocalBinding
import com.manchuan.tools.extensions.androidLogo
import com.manchuan.tools.extensions.androidString
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.utils.BuildUtils
import com.manchuan.tools.utils.UiUtils

class AppLocalActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAppLocalBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            titleBar(binding.toolbar)
            transparentBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
        }
        binding.info.text = spannable {
            "安卓版本: ".text()
            image(androidLogo())
            androidString().text()
            "\n软件版本号: ${BuildConfig.VERSION_CODE}".text()
            "\n软件版本名: ${BuildConfig.VERSION_NAME}".text()
            "\n发布构建类型: ${BuildConfig.BUILD_TYPE}".text()
            "\n设备名称: ${BuildUtils.name}".text()
            "\n设备厂商: ${BuildUtils.manufacturer}".text()
            "\n设备主板: ${BuildUtils.brand}".text()
            "\nROM指纹: ${BuildUtils.fingerprint}".text()
            "\n设备硬件: ${BuildUtils.hardware}".text()
            newline(3)
            "FFmpeg信息:".span {
                typeface(Typeface.DEFAULT_BOLD)
                absoluteSize(18)
                color(colorPrimary())
            }
            newline()
            "支持解码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.DECODE).text()
            newline(2)
            "支持编码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.ENCODE).text()
            newline(2)
            "支持的音频编码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.ENCODE_AUDIO).text()
            newline(2)
            "支持的音频解码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.DECODE_AUDIO).text()
            newline(2)
            "支持的视频编码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.ENCODE_VIDEO).text()
            newline(2)
            "支持的视频解码格式:".text()
            FFmpegCommand.getSupportCodec(CodecAttribute.DECODE_VIDEO).text()
        }
    }
}
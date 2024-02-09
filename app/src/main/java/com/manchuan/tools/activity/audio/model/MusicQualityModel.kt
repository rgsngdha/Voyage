package com.manchuan.tools.activity.audio.model

/**
 * # 音乐品质模型
 * @param level 等级
 * @param bitrate 比特率
 * @param format 音频格式
 * @param size 大小
 * */

data class MusicQualityModel(
    var level: String,
    var bitrate: String,
    var format: String,
    var size: String,
)

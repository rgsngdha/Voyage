package com.manchuan.tools.activity.audio.model

import com.lzx.starrysky.SongInfo
import com.manchuan.tools.activity.game.models.KuwoMusicModel
import kotlinx.serialization.Serializable

@Serializable
data class MediaInfo(var originData: NewKuwoMusicModel.Abslist, var songInfo: SongInfo) : java.io.Serializable

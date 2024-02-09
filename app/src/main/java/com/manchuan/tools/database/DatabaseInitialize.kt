package com.manchuan.tools.database

import com.dylanc.longan.topActivityOrApplication
import com.lzx.starrysky.SongInfo
import com.manchuan.tools.database.music.RecentMusicDatabase

object DatabaseInitialize {

    private val recentMusicDatabase by lazy {
        RecentMusicDatabase.getInstance(topActivityOrApplication)
    }

    val allRecentMusic: MutableList<SongInfo> by lazy {
        val songInfoEntityList = recentMusicDatabase.musicFlowDao().queryAllMusic()
        val songInfo = mutableListOf<SongInfo>()
        songInfoEntityList.forEach { songInfoEntity ->
            songInfo.add(
                SongInfo(
                    songInfoEntity.songId,
                    songInfoEntity.songUrl,
                    songInfoEntity.songName,
                    songInfoEntity.artist,
                    songInfoEntity.songCover,
                    lrclist = songInfoEntity.lrclist
                )
            )
        }
        songInfo
    }

}
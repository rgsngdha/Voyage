package com.manchuan.tools.database.music

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.manchuan.tools.activity.audio.model.KuwoSongInfo
import com.manchuan.tools.activity.audio.model.NewKuwoMusicModel
import com.manchuan.tools.database.music.converter.AbsListConverter
import com.manchuan.tools.database.music.converter.LyricListConverter

@Entity(tableName = "song_info")
@TypeConverters(LyricListConverter::class, AbsListConverter::class)
data class SongInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo var songId: String = "", //音乐id
    @ColumnInfo var songUrl: String = "",  //音乐播放地址
    @ColumnInfo var songName: String = "",  //音乐标题
    @ColumnInfo var artist: String = "",    //作者
    @ColumnInfo var songCover: String = "",  //音乐封面
    @ColumnInfo(defaultValue = "0") var duration: Long = 0L,  //音乐封面
    @ColumnInfo var decode: Boolean = false, //是否需要解码，如果要解码，最好用本地音频
    @ColumnInfo var lrclist: MutableList<KuwoSongInfo.Data.Lrclist>? = emptyList<KuwoSongInfo.Data.Lrclist>().toMutableList(),
    @ColumnInfo var abslist: NewKuwoMusicModel.Abslist? = NewKuwoMusicModel.Abslist(),
    @ColumnInfo var lyric: String? = "",
    @ColumnInfo var translateLyric: String? = "",
)
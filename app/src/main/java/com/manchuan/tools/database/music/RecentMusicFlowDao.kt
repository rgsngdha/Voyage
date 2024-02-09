package com.manchuan.tools.database.music

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import com.manchuan.tools.database.music.converter.AbsListConverter
import com.manchuan.tools.database.music.converter.LyricListConverter

@Dao
@TypeConverters(LyricListConverter::class, AbsListConverter::class)
interface RecentMusicFlowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMusic(vararg musics: SongInfoEntity)

    @Delete
    fun deleteMusic(song: SongInfoEntity)

    @Query("DELETE  FROM song_info where songId=:songId")
    fun deleteMusic(songId: String)

    @Query("SELECT * FROM song_info")
    fun queryAllMusic(): List<SongInfoEntity> // 修改返回值为Flow

    @Query("SELECT * FROM song_info")
    fun queryAllMusicLiveData(): LiveData<List<SongInfoEntity>> // 修改返回值为Flow

}
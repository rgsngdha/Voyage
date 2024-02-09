package com.manchuan.tools.database.music

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.manchuan.tools.database.music.converter.AbsListConverter
import com.manchuan.tools.database.music.converter.LyricListConverter

@Database(
    entities = [SongInfoEntity::class],
    version = 4,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3), AutoMigration(from = 3, to = 4)]
)
@TypeConverters(LyricListConverter::class, AbsListConverter::class)
abstract class RecentMusicDatabase : RoomDatabase() {

    abstract fun musicFlowDao(): RecentMusicFlowDao

    @SuppressLint("StaticFieldLeak")
    companion object {

        @Volatile
        private var INSTANCE: RecentMusicDatabase? = null
        fun getInstance(context: Context): RecentMusicDatabase = INSTANCE ?: synchronized(
            this
        ) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, RecentMusicDatabase::class.java, "recent_music.db"
        ).allowMainThreadQueries().build()

    }

}

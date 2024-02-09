package com.manchuan.tools.activity.movies.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(
    entities = [SourceEntity::class],
    version = 17,
    autoMigrations = [AutoMigration(16, 17)]
)
@TypeConverters(
    SubscribeConverter::class, SubVideoParserConverter::class
)
abstract class SourcesDatabase : RoomDatabase() {
    abstract fun getSourcesDao(): SourcesDao

    companion object {
        @Volatile
        private var INSTANCE: SourcesDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): SourcesDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, SourcesDatabase::class.java, "sources.db"
        ).allowMainThreadQueries().build()
    }
}
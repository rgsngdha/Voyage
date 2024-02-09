package com.manchuan.tools.activity.movies.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update

@Dao
@TypeConverters(
    SubscribeConverter::class, VideoParseConverter::class, SubVideoParserConverter::class
)
interface SourcesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSource(source: SourceEntity)

    @Delete
    fun deleteSource(title: SourceEntity)

    @Update
    fun updateSource(source: SourceEntity)

    @Query("SELECT * FROM sources_database")
    fun queryAllSources(): MutableList<SourceEntity>

    @Query("SELECT * FROM sources_database WHERE name LIKE :name")
    fun getSourceByName(name: String): SourceEntity

    @Query("SELECT * FROM sources_database WHERE id LIKE :id")
    fun getSourceById(id: Int): SourceEntity

}
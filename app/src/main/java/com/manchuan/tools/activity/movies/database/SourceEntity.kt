package com.manchuan.tools.activity.movies.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable


/**
 * @author 川懿
 * @param id 订阅ID
 * @param avatar 订阅作者头像
 * @param description 订阅描述
 * @param version 订阅版本号
 * @param name 订阅名称
 * @param updateUrl 订阅更新链接
 * @param sources 订阅数据
 * @param sourceSwitch 订阅开关
 */
@Entity(tableName = "sources_database")
@Serializable
@TypeConverters(
    SubscribeConverter::class, SubVideoParserConverter::class
)
data class SourceEntity(
    @PrimaryKey var id: Int,
    @ColumnInfo(name = "avatar") var avatar: String = "",
    @ColumnInfo(name = "description") var description: String = "",
    @ColumnInfo(name = "version") var version: Int? = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "update_url") var updateUrl: String? = "",
    @ColumnInfo(name = "data") var sources: MutableList<SubscribeList>? = mutableListOf(),
    @ColumnInfo(name = "video_parse") var videoParse: MutableList<SubVideoParser>? = mutableListOf(),
    @ColumnInfo(name = "source_switch") var sourceSwitch: Boolean = true,
)
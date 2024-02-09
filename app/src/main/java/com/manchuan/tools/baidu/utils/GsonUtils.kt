/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.manchuan.tools.baidu.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/**
 * Json工具类.
 */
object GsonUtils {
    private val gson: Gson = GsonBuilder().create()

    fun toJson(value: Any?): String {
        return gson.toJson(value)
    }

    @Throws(JsonParseException::class)
    fun <T> fromJson(json: String?, classOfT: Class<T>?): T {
        return gson.fromJson(json, classOfT)
    }

    @Throws(JsonParseException::class)
    fun <T> fromJson(json: String?, typeOfT: Type?): T {
        return gson.fromJson<Any>(json, typeOfT) as T
    }
}

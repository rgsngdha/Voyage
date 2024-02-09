package com.lxj.androidktx.core

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


/**
 * 解析url的查询参数
 */
fun String.parseQueryParams(): Map<String, String>{
    val index = lastIndexOf("?") + 1
    val queryParam = hashMapOf<String, String>()
    if(index>0){
        val query = substring(index, length)
        query.split("&").forEach {
            if(it.contains("=")){
                val arr = it.split("=")
                if(arr.size>1){
                    queryParam[arr[0]] = arr[1]
                }
            }
        }
    }
    return queryParam
}


fun String?.isJsonObject(): Boolean{
    if(this==null) return false
    return try {
        JSONObject(this)
        true
    }catch (e: JSONException){
        false
    }
}

fun String?.isJsonArray(): Boolean{
    if(this==null) return false
    return try {
        JSONArray(this)
        true
    }catch (e: JSONException){
        false
    }
}
package com.manchuan.tools.activity.audio.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * @Author Doge
 * @Description Json 解析工具类
 * @Date 2020/12/15
 */
public class JsonUtil {
    /**
     * 判断 Json 对象是否为 null 或 {}
     *
     * @param obj
     * @return
     */
    public static boolean isEmpty(JSONObject obj) {
        return obj == null || obj.isEmpty();
    }

    /**
     * 判断 Json 对象是否不为 null 和 {}
     *
     * @param obj
     * @return
     */
    public static boolean notEmpty(JSONObject obj) {
        return obj != null && !obj.isEmpty();
    }

    /**
     * 判断 Json 数组是否为 null 或 {}
     *
     * @param array
     * @return
     */
    public static boolean isEmpty(JSONArray array) {
        return array == null || array.isEmpty();
    }

    /**
     * 判断 Json 数组是否不为 null 或 {}
     *
     * @param array
     * @return
     */
    public static boolean notEmpty(JSONArray array) {
        return array != null && !array.isEmpty();
    }

    /**
     * 判断字符串是否为合法的 Json
     *
     * @param s
     * @return
     */
    public static boolean isValidObject(String s) {
        return JSON.isValidObject(s);
    }
}

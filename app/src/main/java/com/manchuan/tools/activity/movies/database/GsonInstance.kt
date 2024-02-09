package com.manchuan.tools.activity.movies.database

import com.google.gson.Gson


class GsonInstance {
    val gson: Gson?
        get() {
            if (Companion.gson == null) {
                synchronized(GsonInstance::class.java) {
                    if (Companion.gson == null) {
                        Companion.gson = Gson()
                    }
                }
            }
            return Companion.gson
        }

    companion object {
        private var INSTANCE: GsonInstance? = null
        private var gson: Gson? = null
        val instance: GsonInstance?
            get() {
                if (INSTANCE == null) {
                    synchronized(GsonInstance::class.java) {
                        if (INSTANCE == null) {
                            INSTANCE = GsonInstance()
                        }
                    }
                }
                return INSTANCE
            }
    }
}
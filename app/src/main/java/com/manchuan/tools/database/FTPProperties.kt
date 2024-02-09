package com.manchuan.tools.database

import android.os.Environment
import com.drake.serialize.serialize.serialLazy
import com.drake.serialize.serialize.serialLiveData

object FTPProperties {

    val isRunning by serialLiveData(false)
    var isAnonymous by serialLazy(true)
    var user by serialLazy("anonymous")
    var password by serialLazy("")
    var rootPath by serialLazy(Environment.getExternalStorageDirectory().absolutePath)
    var port by serialLazy(2121)

}
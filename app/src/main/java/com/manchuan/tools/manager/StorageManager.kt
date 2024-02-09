package com.manchuan.tools.manager

import android.content.Context
import android.os.Environment
import com.manchuan.tools.extensions.publicAudiosDirPath
import com.manchuan.tools.extensions.publicDocumentsDirPath
import com.manchuan.tools.extensions.publicDownloadsDirPath
import com.manchuan.tools.extensions.publicMoviesDirPath
import com.manchuan.tools.extensions.publicPicturesDirPath
import java.io.File

object StorageManager {
    private fun createAppFolder() {
        val appFolder =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan"
        if (!File(appFolder).exists()) {
            File(appFolder).mkdirs()
        }
    }

    private fun createCacheFolder() {
        val appFolder =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "HaiYan" + File.separator + "cache"
        if (!File(appFolder).exists()) {
            File(appFolder).mkdirs()
        }
    }

    private fun createLogcatFolder(context: Context) {
        val logcatFolder = context.externalCacheDir!!.absolutePath + File.separator + "logs"
        if (!File(logcatFolder).exists()) {
            File(logcatFolder).mkdirs()
        }
    }

    fun createAllFolder() {
        if (!File(publicPicturesDirPath).exists()) {
            File(publicPicturesDirPath).mkdirs()
        }
        if (!File(publicDownloadsDirPath).exists()) {
            File(publicDownloadsDirPath).mkdirs()
        }
        if (!File(publicAudiosDirPath).exists()) {
            File(publicAudiosDirPath).mkdirs()
        }
        if (!File(publicMoviesDirPath).exists()) {
            File(publicMoviesDirPath).mkdirs()
        }
        if (!File(publicDocumentsDirPath).exists()) {
            File(publicDocumentsDirPath).mkdirs()
        }
    }
}
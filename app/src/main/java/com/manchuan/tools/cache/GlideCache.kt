package com.manchuan.tools.cache

import android.content.Context
import android.os.Looper
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import java.io.File
import java.math.BigDecimal


/**
 * Glide缓存工具类
 */
/**
 * 清除图片磁盘缓存
 */
fun clearImageDiskCache(context: Context?) {
    try {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Thread {
                if (context != null) {
                    Glide.get(context).clearDiskCache()
                }
            }.start()
        } else {
            if (context != null) {
                Glide.get(context).clearDiskCache()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 清除图片内存缓存
 */
fun clearImageMemoryCache(context: Context?) {
    try {
        if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
            if (context != null) {
                Glide.get(context).clearMemory()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 清除图片所有缓存
 */
fun Context.clearImageAllCache() {
    clearImageDiskCache(this)
    clearImageMemoryCache(this)
    val ImageExternalCatchDir: String = externalCacheDir!!.absolutePath + ExternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR
    deleteFolderFile(ImageExternalCatchDir, true)
}

/**
 * 获取Glide造成的缓存大小
 *
 * @return CacheSize
 */
fun Context.getGlideCacheSize(): String {
    try {
        return getFormatSize(getFolderSize(File(cacheDir!!.absolutePath + "/" + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR)).toDouble())
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

/**
 * 获取指定文件夹内所有文件大小的和
 *
 * @param file file
 * @return size
 * @throws Exception
 */
@Throws(Exception::class)
private fun getFolderSize(file: File): Long {
    var size: Long = 0
    try {
        val fileList: Array<File> = file.listFiles()
        for (aFileList in fileList) {
            if (aFileList.isDirectory) {
                size += getFolderSize(aFileList)
            } else {
                size += aFileList.length()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return size
}

/**
 * 删除指定目录下的文件，这里用于缓存的删除
 *
 * @param filePath filePath
 * @param deleteThisPath deleteThisPath
 */
private fun deleteFolderFile(filePath: String, deleteThisPath: Boolean) {
    if (!TextUtils.isEmpty(filePath)) {
        try {
            val file = File(filePath)
            if (file.isDirectory) {
                val files: Array<out File>? = file.listFiles()
                if (files != null) {
                    for (file1 in files) {
                        deleteFolderFile(file1.absolutePath, true)
                    }
                }
            }
            if (deleteThisPath) {
                if (!file.isDirectory) {
                    file.delete()
                } else {
                    if (file.listFiles()?.isEmpty() == true) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * 格式化单位
 *
 * @param size size
 * @return size
 */
private fun getFormatSize(size: Double): String {
    val kiloByte = size / 1024
    if (kiloByte < 1) {
        return size.toString() + "Byte"
    }
    val megaByte = kiloByte / 1024
    if (megaByte < 1) {
        val result1 = BigDecimal(kiloByte.toString())
        return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
    }
    val gigaByte = megaByte / 1024
    if (gigaByte < 1) {
        val result2 = BigDecimal(megaByte.toString())
        return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
    }
    val teraBytes = gigaByte / 1024
    if (teraBytes < 1) {
        val result3 = BigDecimal(gigaByte.toString())
        return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
    }
    val result4 = BigDecimal(teraBytes)
    return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
}
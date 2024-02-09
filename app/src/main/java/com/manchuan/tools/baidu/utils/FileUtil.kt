package com.manchuan.tools.baidu.utils

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

/**
 * 文件读取工具类
 */
object FileUtil {
    /**
     * 读取文件内容，作为字符串返回
     */
    @Throws(IOException::class)
    fun readFileAsString(filePath: String?): String {
        val file = File(filePath)
        if (!file.exists()) {
            throw FileNotFoundException(filePath)
        }

        if (file.length() > 1024 * 1024 * 1024) {
            throw IOException("File is too large")
        }

        val sb = StringBuilder(file.length().toInt())
        // 创建字节输入流
        val fis = FileInputStream(filePath)
        // 创建一个长度为10240的Buffer
        val bbuf = ByteArray(10240)
        // 用于保存实际读取的字节数
        var hasRead = 0
        while ((fis.read(bbuf).also { hasRead = it }) > 0) {
            sb.append(String(bbuf, 0, hasRead))
        }
        fis.close()
        return sb.toString()
    }

    /**
     * 根据文件路径读取byte[] 数组
     */
    @Throws(IOException::class)
    fun readFileByBytes(filePath: String?): ByteArray {
        val file = File(filePath)
        if (!file.exists()) {
            throw FileNotFoundException(filePath)
        } else {
            val bos = ByteArrayOutputStream(file.length().toInt())
            var `in`: BufferedInputStream? = null

            try {
                `in` = BufferedInputStream(FileInputStream(file))
                val bufSize: Short = 1024
                val buffer = ByteArray(bufSize.toInt())
                var len1: Int
                while (-1 != (`in`.read(buffer, 0, bufSize.toInt()).also { len1 = it })) {
                    bos.write(buffer, 0, len1)
                }

                val var7 = bos.toByteArray()
                return var7
            } finally {
                try {
                    `in`?.close()
                } catch (var14: IOException) {
                    var14.printStackTrace()
                }

                bos.close()
            }
        }
    }
}

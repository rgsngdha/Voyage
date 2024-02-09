package com.manchuan.tools.extensions

import ando.file.core.FileSizeUtils
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.util.Base64

fun formatSize(path: String): String {
    return FileSizeUtils.formatFileSize(FileSizeUtils.getFileSize(File(path)))
}

fun File.formatSize(): String {
    return FileSizeUtils.formatFileSize(FileSizeUtils.getFileSize(this))
}

//Date格式
const val DATE_FORMAT_LINK = "yyyyMMddHHmmssSSS"

//Date格式 常用
const val DATE_FORMAT_DETACH = "yyyy-MM-dd HH:mm:ss"
const val DATE_FORMAT_DETACH_CN = "yyyy年MM月dd日 HH:mm:ss"
const val DATE_FORMAT_DETACH_CN_SSS = "yyyy年MM月dd日 HH:mm:ss SSS"

//Date格式 带毫秒
const val DATE_FORMAT_DETACH_SSS = "yyyy-MM-dd HH:mm:ss SSS"

//时间格式 分钟：秒钟 一般用于视频时间显示
const val DATE_FORMAT_MM_SS = "mm:ss"

fun formatSize(size: Long): String {
    return FileSizeUtils.formatFileSize(size)
}

fun String.substringBetween(start: String, end: String): String {
    return StringUtils.substringBetween(this, start, end)
}

fun CharSequence.substringBetween(start: String, end: String): String {
    return StringUtils.substringBetween(this.toString(), start, end)
}

fun ByteArray.asByteBuffer(): ByteBuffer = ByteBuffer.wrap(this)

fun ByteArray.base64Encode(): String = Base64.getEncoder().encodeToString(this)

fun ByteArray.base64EncodeArray(): ByteArray = Base64.getEncoder().encode(this)

fun ByteBuffer.length() = limit() - position()

fun ByteBuffer.asString(): String = String(array(), position(), length())

fun ByteBuffer.base64Encode(): String = array().base64Encode()

fun ByteBuffer.base64Decoded(): ByteArray = Base64.getDecoder().decode(array())

fun ByteBuffer.base64DecodedByteBuffer(): ByteBuffer = base64Decoded().asByteBuffer()

fun String.asByteBuffer(): ByteBuffer = toByteArray().asByteBuffer()

fun String.quoted() = "\"${replace("\"", "\\\"")}\""

fun String.unquoted(): String =
    replaceFirst("^\"".toRegex(), "").replaceFirst("\"$".toRegex(), "").replace("\\\"", "\"")

fun StringBuilder.appendIfNotBlank(valueToCheck: String, vararg toAppend: String): StringBuilder =
    appendIf({ valueToCheck.isNotBlank() }, *toAppend)

fun StringBuilder.appendIfNotEmpty(
    valueToCheck: List<Any>, vararg toAppend: String
): StringBuilder = appendIf({ valueToCheck.isNotEmpty() }, *toAppend)

fun StringBuilder.appendIfPresent(valueToCheck: Any?, vararg toAppend: String): StringBuilder =
    appendIf({ valueToCheck != null }, *toAppend)

fun StringBuilder.appendIf(condition: () -> Boolean, vararg toAppend: String): StringBuilder =
    apply {
        if (condition()) toAppend.forEach { append(it) }
    }

fun String.base64Decoded() = base64DecodedArray().decodeToString()

fun String.base64DecodedArray(): ByteArray = Base64.getDecoder().decode(this)

fun String.base64DecodedByteBuffer() = base64DecodedArray().asByteBuffer()

fun String.base64Encode() = toByteArray().base64Encode()

fun String.urlEncoded(): String = URLEncoder.encode(this, "utf-8")

fun String.urlDecoded(): String = URLDecoder.decode(this, "utf-8")
package com.lxj.androidktx.core

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Formatter
import java.util.Locale
import java.util.TimeZone

/**
 * Description: 时间日期相关
 * Create by lxj, at 2018/12/7
 */

@SuppressLint("SimpleDateFormat")
fun transToString(time: Long, format: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(format).format(time)
}

/**
 *  字符串日期格式（比如：2018-4-6)转为毫秒
 *  @param format 时间的格式，默认是按照yyyy-MM-dd HH:mm:ss来转换，如果您的格式不一样，则需要传入对应的格式
 */
fun String.toDateMills(format: String = "yyyy-MM-dd HH:mm:ss") =
    SimpleDateFormat(format, Locale.getDefault()).parse(this)?.time

fun dateNow(format: String = "yyyy-MM-dd HH:mm:ss"): String =
    SimpleDateFormat(format, Locale.getDefault()).format(Date())

/**
 * Long类型时间戳转为字符串的日期格式
 * @param format 时间的格式，默认是按照yyyy-MM-dd HH:mm:ss来转换，如果您的格式不一样，则需要传入对应的格式
 */
fun Long.toDateString(format: String = "yyyy-MM-dd HH:mm:ss") =
    SimpleDateFormat(format, Locale.getDefault()).format(Date(this))

fun String.toDateString(format: String = "yyyy-MM-dd HH:mm:ss") =
    SimpleDateFormat(format, Locale.getDefault()).format(Date(this))

fun Int.toDateString(format: String = "yyyy-MM-dd HH:mm:ss") =
    SimpleDateFormat(format, Locale.getDefault()).format(Date(this.toLong()))

/**
 * 将毫秒值转为 hh:mm:ss 格式
 */
fun Long.toMediaTime(): String {
    val formatBuilder = StringBuilder()
    val formatter = Formatter(formatBuilder, Locale.getDefault())
    if (this < 0) {
        return "00:00"
    }
    val seconds = (this % DateUtils.MINUTE_IN_MILLIS) / DateUtils.SECOND_IN_MILLIS
    val minutes = (this % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS
    val hours = (this % DateUtils.DAY_IN_MILLIS) / DateUtils.HOUR_IN_MILLIS
    formatBuilder.setLength(0)
    if (hours > 0) {
        return formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString()
    }
    return formatter.format("%02d:%02d", minutes, seconds).toString()
}

val curTime: Long
    get() = System.currentTimeMillis()

/**
 * 获取当前年份
 */
val curYear: Int
    get() = curTime.getDateYear()

/**
 * 获取当前月份
 */
val curMonth: Int
    get() = curTime.getDateMonth()

/**
 * 获取当前日
 */
val curDay: Int
    get() = curTime.getDateDay()

/**
 * 获取当前小时
 */
val curHour: Int
    get() = curTime.getDateHour()

/**
 * 获取当前分钟
 */
val curMinute: Int
    get() = curTime.getDateMinute()

/**
 * 获取当前秒钟
 */
val curSecond: Int
    get() = curTime.getDateSecond()

/**
 * 获取时间戳中的年份
 * @return [Int] 年份
 */
fun Long.getDateYear(): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.get(Calendar.YEAR)
}

/**
 * 获取时间戳中的月份
 * @return [Int] 月份
 */
fun Long.getDateMonth(): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.get(Calendar.MONTH) + 1
}

/**
 * 获取时间戳中的日
 * @return [Int] 日
 */
fun Long.getDateDay(): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.get(Calendar.DAY_OF_MONTH)
}

/**
 * 获取时间戳中的小时 24小时
 * @return [Int] 小时
 */
fun Long.getDateHour(): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.get(Calendar.HOUR_OF_DAY)
}

/**
 * 获取时间戳中的分钟
 * @return [Int] 分钟
 */
fun Long.getDateMinute(): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.get(Calendar.MINUTE)
}

/**
 * 获取时间戳中的秒钟
 * @return [Int] 秒钟
 */
fun Long.getDateSecond(): Int {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar.get(Calendar.SECOND)
}

/**
 * 时间戳转换成字符窜
 * @param pattern 时间样式 yyyy-MM-dd HH:mm:ss
 * @return [String] 时间字符串
 */
@SuppressLint("SimpleDateFormat")
fun Long.toDateStr(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val date = Date(this)
    val format = SimpleDateFormat(pattern)
    return format.format(date)
}

/**
 * 将字符串转为时间戳
 * @param pattern 时间样式 yyyy-MM-dd HH:mm:ss
 * @return [String] 时间字符串
 */
fun String.toDateLong(pattern: String = "yyyy-MM-dd HH:mm:ss"): Long {
    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat(pattern)
    var date: Date? = Date()
    try {
        date = dateFormat.parse(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return date?.time ?: 0
}

/**
 * 根据年月日获取时间戳
 * @param year 年
 * @param month 月
 * @param day 日
 * @return [Long] 时间戳
 */
fun getDateFromYMD(year: Int = curYear, month: Int = curMonth, day: Int = curDay): Long {
    return getDateFromYMDHMS(year, month, day, 0, 0, 0)
}

/**
 * 根据年月日时分秒获取时间戳
 * @param year Int 年
 * @param month Int 月
 * @param day Int 日
 * @param hour Int 时
 * @param minute Int 分
 * @param second Int 秒
 * @return [Long] 时间戳
 */
fun getDateFromYMDHMS(
    year: Int = curYear,
    month: Int = curMonth,
    day: Int = curDay,
    hour: Int = curHour,
    minute: Int = curMinute,
    second: Int = curSecond,
): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month -1, day, hour, minute, second)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/**
 * 获取第n天的时间戳
 * @param offset n
 * @return [Long] 时间戳
 */
fun getNextDate(offset: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.time = Date(getDateFromYMD(curYear, curMonth , curDay))
    calendar.add(Calendar.DAY_OF_MONTH, offset)
    return calendar.timeInMillis
}

/**
 * 获取某个日子为标点的附近的日子时间戳
 * @receiver Long
 * @param offset Int
 * @return Long
 */
fun Long.getNextDay(offset: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.time = Date(this)
    calendar.add(Calendar.DAY_OF_MONTH, offset)
    return calendar.timeInMillis
}

/**
 * 获取指定月份的天数
 * @param year 年
 * @param month 月
 * @return [Int] 天数
 */
@SuppressLint("SimpleDateFormat")
fun getDaysOfMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month -1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

/**
 * 获取今天星期几
 * @return [Int] [Calendar.SUNDAY]
 */
fun getCurWeek(): Int {
    return curTime.getDateWeek()
}

/**
 * 获取时间戳是星期几
 * @return [Int] [Calendar.SUNDAY]
 */
fun Long.getDateWeek(): Int {
    val calendar = Calendar.getInstance()
    calendar.time = Date(this)
    return calendar.get(Calendar.DAY_OF_WEEK)
}

/**
 * 时间戳是否为今天的
 * @receiver Long
 * @return Boolean
 */
fun Long.isToday(): Boolean {
    return getDateYear() == curYear && getDateMonth() == curMonth && getDateDay() == curDay
}

/**
 * 时间戳是否为昨天的
 * @receiver Long
 * @return Boolean
 */
fun Long.isYesterday(): Boolean {
    val yesterday = curTime.getNextDay(-1)
    return getDateYear() == yesterday.getDateYear() && getDateMonth() == yesterday.getDateMonth() && getDateDay() == yesterday.getDateDay()
}

/**
 * 本地时间转化为UTC时间
 * @receiver Long
 * @return Long
 */
fun Long.toUTCDate(): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@toUTCDate
    }
    val zoneOffset = calendar.get(Calendar.ZONE_OFFSET)
    val dstOffset = calendar.get(Calendar.DST_OFFSET)
    calendar.add(Calendar.MILLISECOND, - (zoneOffset + dstOffset))
    return calendar.timeInMillis
}

/**
 * UTC时间转化为本地时间
 * @receiver Long
 * @return Long
 */
@SuppressLint("SimpleDateFormat")
fun Long.toLocalDate(): Long {
    val pattern = "yyyyMMddHHmmssSSS"
    val utcSdf = SimpleDateFormat(pattern).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val utcD = utcSdf.parse(this.toDateStr(pattern))?: return 0L
    val localSdf = SimpleDateFormat(pattern).apply {
        timeZone = TimeZone.getDefault()
    }
    val localStr = localSdf.format(utcD.time)
    return localStr.toDateLong(pattern)
}

/**
 * UTC时间转化为指定timeZone时间
 * @receiver Long
 * @param timeZoneInt Int
 * @return Long
 */
@SuppressLint("SimpleDateFormat")
fun Long.toCustomDate(timeZoneInt: Int): Long {
    val pattern = "yyyyMMddHHmmssSSS"
    val utcSdf = SimpleDateFormat(pattern).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val utcD = utcSdf.parse(this.toDateStr(pattern))?: return 0L
    val localSdf = SimpleDateFormat(pattern).apply {
        timeZone = TimeZone.getTimeZone("GMT" + (if (timeZoneInt >= 0) "+" else "") + timeZoneInt)
    }
    val localStr = localSdf.format(utcD.time)
    return localStr.toDateLong(pattern)
}

/**
 * 根据时间转换为时间戳
 * @param date
 * @param timestampType 转换类型 0毫秒 1秒
 * @return
 */
fun getTimeStamp(date: Date, timestampType: Int): Long {
    var times = date.time
    if (timestampType == 1) {
        times = times / 1000L
    }
    return times
}

/**
 * 时间戳转时间
 * @param timestamp
 * @param timestampType 时间戳格式 0毫秒 1秒
 * @return
 */
fun getDateTime(timestamp: Long, timestampType: Int): Date? {
    var timestamp = timestamp
    if (timestampType == 1) {
        //如果时间戳格式是秒，需要江时间戳变为毫秒
        timestamp = timestamp * 1000L
    }
    return Date(timestamp)
}

/**
 * 格式化传入的时间，将时间转化为指定格式字符串
 * @param date
 * @param format 时间格式，如：yyyy-MM-dd HH:mm:ss SSS 或 yyyy年MM月dd日 HH:mm:ss
 * @return
 */
fun getDateTimeString(date: Date?, format: String?): String? {
    if (format == null || format.isEmpty()) {
        return null
    }
    // 格式化日期
    val sdf = SimpleDateFormat(format)
    return sdf.format(date)
}

/**
 * 格式化传入的时间戳，将时间戳转化为指定格式字符串
 * @param timestamp
 * @param format 时间格式，如：yyyy-MM-dd HH:mm:ss SSS 或 yyyy年MM月dd日 HH:mm:ss     *
 * @param timestampType 时间戳格式 0毫秒 1秒
 * @return
 */
fun getTimeStampString(
    timestamp: Long,
    format: String?,
    timestampType: Int,
): String? {
    var timestamp = timestamp
    if (format.isNullOrEmpty()) {
        return null
    }
    if (timestampType == 1) {
        //如果时间戳格式是秒，需要江时间戳变为毫秒
        timestamp *= 1000L
    }
    val dateTime = Date(timestamp)
    // 格式化日期
    val sdf = SimpleDateFormat(format)
    return sdf.format(dateTime)
}

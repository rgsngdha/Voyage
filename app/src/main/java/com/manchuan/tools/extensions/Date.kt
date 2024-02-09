package com.manchuan.tools.extensions

import android.text.format.Time
import com.manchuan.tools.utils.LunarCalender
import java.util.Calendar


/**
 * 是否在春节与元宵节日之间
 */
fun isSpringOrYuanXiao(): Boolean {
    val lunarCalender = LunarCalender()
    val calendar: Calendar = Calendar.getInstance()
    val year: Int = calendar.get(Calendar.YEAR)
    val month: Int = calendar.get(Calendar.MONTH) + 1
    val day: Int = calendar.get(Calendar.DATE)
    var isSpringOrYuanXiao = false

    if (lunarCalender.getFestival(year, month, day).contains("春节")) {
        isSpringOrYuanXiao = true
    } else {
        (1 until 14).forEach {
            if (!isSpringOrYuanXiao) isSpringOrYuanXiao =
                lunarCalender.getFestival(year, month, day + it).contains("元宵")
        }
    }
    return isSpringOrYuanXiao
}

/**
 * 判断当前系统时间是否在指定时间的范围内
 *
 *
 * beginHour 开始小时,例如22
 * beginMin  开始小时的分钟数,例如30
 * endHour   结束小时,例如 8
 * endMin    结束小时的分钟数,例如0
 * true表示在范围内, 否则false
 */
fun isCurrentInTimeScope(beginHour: Int, beginMin: Int, endHour: Int, endMin: Int): Boolean {
    var result = false
    val aDayInMillis = (1000 * 60 * 60 * 24).toLong()
    val currentTimeMillis = System.currentTimeMillis()
    val now = Time()
    now.set(currentTimeMillis)
    val startTime = Time()
    startTime.set(currentTimeMillis)
    startTime.hour = beginHour
    startTime.minute = beginMin
    val endTime = Time()
    endTime.set(currentTimeMillis)
    endTime.hour = endHour
    endTime.minute = endMin
    // 跨天的特殊情况(比如22:00-8:00)
    if (!startTime.before(endTime)) {
        startTime.set(startTime.toMillis(true) - aDayInMillis)
        result = !now.before(startTime) && !now.after(endTime)
        val startTimeInThisDay = Time()
        startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis)
        if (!now.before(startTimeInThisDay)) {
            result = true
        }
    } else {
        result = !now.before(startTime) && !now.after(endTime)
    }
    return result
}

val NowTimeString = if (isCurrentInTimeScope(0, 0, 4, 59)) "凌晨" else if (isCurrentInTimeScope(
        4, 59, 6, 59
    )
) "清晨" else if (isCurrentInTimeScope(6, 59, 9, 0)) "早上" else if (isCurrentInTimeScope(
        9, 0, 11, 0
    )
) "上午" else if (isCurrentInTimeScope(11, 0, 12, 30)) "中午" else if (isCurrentInTimeScope(
        12, 30, 17, 0
    )
) "下午" else if (isCurrentInTimeScope(17, 0, 18, 59)) "傍晚" else if (isCurrentInTimeScope(
        18, 59, 22, 59
    )
) "晚上" else "深夜"


package com.manchuan.tools.extensions

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import java.net.NetworkInterface
import kotlin.reflect.KFunction

fun getIpAddress(): String {
    var ip = ""
    try {
        val enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (enumNetworkInterfaces.hasMoreElements()) {
            val networkInterface = enumNetworkInterfaces.nextElement()
            val enumInetAddress = networkInterface.inetAddresses
            while (enumInetAddress.hasMoreElements()) {
                val inetAddress = enumInetAddress.nextElement()
                if (inetAddress.isSiteLocalAddress) {
                    ip += inetAddress.hostAddress
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ip += "Something Wrong! $e\n"
    }
    return ip
}

val defaultHeaders = mapOf(
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"
)
lateinit var cache: Cache

//fun <K, V, R> Map<out K, V>.asyncMap(f: suspend (Map.Entry<K, V>) -> R): List<R> = runBlocking {
//    map { withContext(Dispatchers.IO) { async { f(it) } } }.map { it.await() }
//}

fun <A, B> Collection<A>.asyncMap(f: suspend (A) -> B): List<B> = runBlocking {
    map { async { f(it) } }.map { it.await() }
}

//fun <A, B> Collection<A>.asyncMapNotNull(f: suspend (A) -> B?): List<B> = runBlocking {
//    map { async { f(it) } }.mapNotNull { it.await() }
//}

fun logError(e: Exception) {
    toastString(e.localizedMessage)
    e.printStackTrace()
}

fun <T> tryWith(call: () -> T, failed: (() -> T)? = null): T? {
    return try {
        call.invoke()
    } catch (e: Exception) {
        logError(e)
        failed?.invoke()
        null
    }
}

suspend fun <T> tryWithSuspend(call: suspend () -> T, failed: suspend () -> T): T? {
    return try {
        call.invoke()
    } catch (e: Exception) {
        logError(e)
        failed.invoke()
        null
    } catch (e: CancellationException) {
        null
    }
}

//Credits to leg
data class Lazier<T>(
    val lClass: KFunction<T>,
    val name: String,
) {
    val get = lazy { lClass.call() }
}

fun <T> lazyList(vararg objects: Pair<String, KFunction<T>>): List<Lazier<T>> {
    return objects.map {
        Lazier(it.second, it.first)
    }
}
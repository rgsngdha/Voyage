package com.manchuan.tools.cache.glide.key

import com.bumptech.glide.load.Key
import com.bumptech.glide.util.Preconditions
import java.io.Serializable
import java.security.MessageDigest

/**
 * Wraps an [java.lang.Object], delegating [.equals] and [.hashCode] to
 * the wrapped Object and providing the bytes of the result of the Object's [.toString]
 * method to the [java.security.MessageDigest] in [ ][.updateDiskCacheKey].
 *
 *
 * The Object's [.toString] method must be unique and suitable for use as a disk cache
 * key.
 */

class GlideKey(`object`: Any) : Key, Serializable {
    private val `object`: Any?

    init {
        this.`object` = Preconditions.checkNotNull(`object`)
    }

    override fun toString(): String {
        return "ObjectKey{object=$`object`}"
    }

    override fun equals(o: Any?): Boolean {
        return if (o is GlideKey) {
            `object` == o.`object`
        } else false
    }

    override fun hashCode(): Int {
        return `object`.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(`object`.toString().toByteArray(Key.CHARSET))
    }
}

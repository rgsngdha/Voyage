package com.manchuan.tools.cache

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.EncodeStrategy
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.serialization.Serializable

@Serializable
object DiskCache : DiskCacheStrategy(), java.io.Serializable {
    @Serializable
    val ALL: DiskCacheStrategy = object : DiskCacheStrategy() {
        override fun isDataCacheable(dataSource: DataSource): Boolean {
            return dataSource == DataSource.REMOTE
        }

        override fun isResourceCacheable(
            isFromAlternateCacheKey: Boolean,
            dataSource: DataSource,
            encodeStrategy: EncodeStrategy?
        ): Boolean {
            return (dataSource != DataSource.RESOURCE_DISK_CACHE && dataSource != DataSource.MEMORY_CACHE)
        }

        override fun decodeCachedResource(): Boolean {
            return true
        }

        override fun decodeCachedData(): Boolean {
            return true
        }
    }

    /** Saves no data to cache.  */
    @Serializable
    val NONE: DiskCacheStrategy = object : DiskCacheStrategy() {
        override fun isDataCacheable(dataSource: DataSource?): Boolean {
            return false
        }

        override fun isResourceCacheable(
            isFromAlternateCacheKey: Boolean,
            dataSource: DataSource?,
            encodeStrategy: EncodeStrategy?
        ): Boolean {
            return false
        }

        override fun decodeCachedResource(): Boolean {
            return false
        }

        override fun decodeCachedData(): Boolean {
            return false
        }
    }

    /** Writes retrieved data directly to the disk cache before it's decoded.  */
    @Serializable
    val DATA: DiskCacheStrategy = object : DiskCacheStrategy() {
        override fun isDataCacheable(dataSource: DataSource): Boolean {
            return dataSource != DataSource.DATA_DISK_CACHE && dataSource != DataSource.MEMORY_CACHE
        }

        override fun isResourceCacheable(
            isFromAlternateCacheKey: Boolean,
            dataSource: DataSource?,
            encodeStrategy: EncodeStrategy?
        ): Boolean {
            return false
        }

        override fun decodeCachedResource(): Boolean {
            return false
        }

        override fun decodeCachedData(): Boolean {
            return true
        }
    }

    /** Writes resources to disk after they've been decoded.  */
    @Serializable
    val RESOURCE: DiskCacheStrategy = object : DiskCacheStrategy() {
        override fun isDataCacheable(dataSource: DataSource?): Boolean {
            return false
        }

        override fun isResourceCacheable(
            isFromAlternateCacheKey: Boolean,
            dataSource: DataSource,
            encodeStrategy: EncodeStrategy?
        ): Boolean {
            return (dataSource != DataSource.RESOURCE_DISK_CACHE && dataSource != DataSource.MEMORY_CACHE)
        }

        override fun decodeCachedResource(): Boolean {
            return true
        }

        override fun decodeCachedData(): Boolean {
            return false
        }
    }

    /**
     * Tries to intelligently choose a strategy based on the data source of the [ ] and the [ ] of the [com.bumptech.glide.load.ResourceEncoder]
     * (if an [com.bumptech.glide.load.ResourceEncoder] is available).
     */
    @Serializable
    val AUTOMATIC: DiskCacheStrategy = object : DiskCacheStrategy() {
        override fun isDataCacheable(dataSource: DataSource): Boolean {
            return dataSource == DataSource.REMOTE
        }

        override fun isResourceCacheable(
            isFromAlternateCacheKey: Boolean, dataSource: DataSource, encodeStrategy: EncodeStrategy
        ): Boolean {
            return ((isFromAlternateCacheKey && dataSource == DataSource.DATA_DISK_CACHE || dataSource == DataSource.LOCAL) && encodeStrategy == EncodeStrategy.TRANSFORMED)
        }

        override fun decodeCachedResource(): Boolean {
            return true
        }

        override fun decodeCachedData(): Boolean {
            return true
        }
    }

    override fun isDataCacheable(dataSource: DataSource?): Boolean {
        return true
    }

    override fun isResourceCacheable(
        isFromAlternateCacheKey: Boolean, dataSource: DataSource?, encodeStrategy: EncodeStrategy?
    ): Boolean {
        return false
    }

    override fun decodeCachedResource(): Boolean {
        return false
    }

    override fun decodeCachedData(): Boolean {
        return false
    }

}
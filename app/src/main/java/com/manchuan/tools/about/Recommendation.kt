package com.manchuan.tools.about

import androidx.annotation.Keep

/**
 * @author drakeet
 */
@Keep
class Recommendation {
    var id = 0
    @JvmField
    var appName: String? = null
    @JvmField
    var iconUrl: String? = null
    @JvmField
    var packageName: String? = null
    @JvmField
    var description: String? = null
    @JvmField
    var downloadUrl: String? = null
    var createdTime: String? = null
    var updatedTime: String? = null
    @JvmField
    var downloadSize = 0.0
    @JvmField
    var openWithGooglePlay = false

    constructor()
    constructor(
        id: Int,
        appName: String,
        iconUrl: String,
        packageName: String,
        description: String,
        downloadUrl: String,
        createdTime: String,
        updatedTime: String,
        downloadSize: Double,
        openWithGooglePlay: Boolean
    ) {
        this.id = id
        this.appName = appName
        this.iconUrl = iconUrl
        this.packageName = packageName
        this.description = description
        this.downloadUrl = downloadUrl
        this.createdTime = createdTime
        this.updatedTime = updatedTime
        this.downloadSize = downloadSize
        this.openWithGooglePlay = openWithGooglePlay
    }
}
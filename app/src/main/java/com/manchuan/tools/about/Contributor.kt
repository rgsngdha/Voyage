package com.manchuan.tools.about

import androidx.annotation.DrawableRes

/**
 * @author drakeet
 */
class Contributor {
    @JvmField
    @DrawableRes
    var avatarResId = 0
    @JvmField
    val name: String
    @JvmField
    val desc: String
    @JvmField
    var url: String?
    @JvmField
    var avatarUrl: String? = null

    constructor(avatarUrl: String, name: String, desc: String) : this(avatarUrl, name, desc, null)

    @JvmOverloads
    constructor(@DrawableRes avatarResId: Int, name: String, desc: String, url: String? = null) {
        this.avatarResId = avatarResId
        this.name = name
        this.desc = desc
        this.url = url
    }

    constructor(avatarUrl: String, name: String, desc: String, url: String?) {
        this.avatarUrl = avatarUrl
        this.name = name
        this.desc = desc
        this.url = url
    }
}
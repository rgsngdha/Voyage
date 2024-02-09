package com.manchuan.tools.about

/**
 * @author drakeet
 */
class License {
    @JvmField
    var name: String? = null
    @JvmField
    var author: String? = null
    @JvmField
    var type: String? = null
    @JvmField
    var url: String? = null

    constructor()
    constructor(name: String, author: String, type: String, url: String) {
        this.name = name
        this.author = author
        this.type = type
        this.url = url
    }

    companion object {
        const val MIT = "MIT License"
        const val APACHE_2 = "Apache Software License 2.0"
        const val GPL_V3 = "GNU general public license Version 3"
    }
}
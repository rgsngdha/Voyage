package com.manchuan.tools.about

import android.annotation.SuppressLint

/**
 * @author drakeet
 */
class Card(val content: CharSequence, val lineSpacingExtra: Int) {
    @SuppressLint("NewApi")
    constructor(content: CharSequence) : this(content, 0)
}
package com.manchuan.tools.activity.app.models

import androidx.annotation.DrawableRes

data class PrivacyModel(
    @DrawableRes var icon: Int,
    var title: String,
    var summary: String,
    var type: Int,
)

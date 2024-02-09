package com.manchuan.tools.activity.app.models

import androidx.annotation.DrawableRes

class PermissionGuide(
    @DrawableRes var icon: Int,
    var title: String,
    var summary: String,
    vararg var permission: String,
)

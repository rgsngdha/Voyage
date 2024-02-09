package com.manchuan.tools.activity.movies.model

import androidx.annotation.DrawableRes

data class FunctionModel(@DrawableRes var icon: Int, var name: String, var unit: () -> Unit)

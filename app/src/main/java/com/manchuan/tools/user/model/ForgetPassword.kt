package com.manchuan.tools.user.model

data class ForgetPassword(
    val code: Int,
    val msg: String,
    val time: Int
)
package com.manchuan.tools.activity.app.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PermissionNameModel(
    @SerialName("PermissionList")
    var permissionList: List<Permission> = listOf()
) {
    @Serializable
    data class Permission(
        @SerialName("Key")
        var key: String = "",
        @SerialName("Title")
        var title: String = "",
        @SerialName("Memo")
        var memo: String = "",
        @SerialName("Level")
        var level: Int = 0
    )
}
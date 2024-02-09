package com.manchuan.tools.fragment.json.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShiCiModels(
    @SerialName("msg")
    var msg: String
)
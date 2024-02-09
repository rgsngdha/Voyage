package com.manchuan.tools.activity.game.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class HonorKings : ArrayList<HonorKings.HonorKingsItem>(){
    @Serializable
    data class HonorKingsItem(
        @SerialName("ename")
        var ename: Int = 0,
        @SerialName("cname")
        var cname: String = "",
        @SerialName("title")
        var title: String = "",
        @SerialName("new_type")
        var newType: Int = 0,
        @SerialName("hero_type")
        var heroType: Int = 0,
        @SerialName("skin_name")
        var skinName: String = "",
        @SerialName("moss_id")
        var mossId: Int = 0,
        @SerialName("pay_type")
        var payType: Int = 0,
        @SerialName("hero_type2")
        var heroType2: Int = 0
    )
}
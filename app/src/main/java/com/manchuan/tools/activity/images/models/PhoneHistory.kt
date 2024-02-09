package com.manchuan.tools.activity.images.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PhoneHistory : ArrayList<PhoneHistory.PhoneHistoryItem>(){
    @Serializable
    data class PhoneHistoryItem(
        @SerialName("name")
        var name: String = "",
        @SerialName("author")
        var author: String = "",
        @SerialName("dimensions")
        var dimensions: String = "",
        @SerialName("url")
        var url: String = "",
        @SerialName("thumbnail")
        var thumbnail: String = "",
        @SerialName("collections")
        var collections: String = "",
        @SerialName("downloadable")
        var downloadable: String = ""
    )
}
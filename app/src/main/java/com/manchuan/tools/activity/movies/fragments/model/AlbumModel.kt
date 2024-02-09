package com.manchuan.tools.activity.movies.fragments.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlbumModel(
    @SerialName("data")
    var `data`: Data
) {
    @Serializable
    data class Data(
        @SerialName("items")
        var items: List<Item>
    ) {
        @Serializable
        data class Item(
            @SerialName("albumDes")
            var albumDes: String,
            @SerialName("albumImageUrl")
            var albumImageUrl: String
        )
    }
}
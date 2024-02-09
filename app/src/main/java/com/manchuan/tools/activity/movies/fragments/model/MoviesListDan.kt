package com.manchuan.tools.activity.movies.fragments.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesListDan(
    @SerialName("code")
    var code: Int,
    @SerialName("data")
    var `data`: Data,
    @SerialName("result")
    var result: String
) {
    @Serializable
    data class Data(
        @SerialName("item_list")
        var itemList: List<Item>,
        @SerialName("total")
        var total: Int
    ) {
        @Serializable
        data class Item(
            @SerialName("cover_url1")
            var coverUrl1: String,
            @SerialName("cover_url2")
            var coverUrl2: String,
            @SerialName("cover_url3")
            var coverUrl3: String,
            @SerialName("created_on")
            var createdOn: Int,
            @SerialName("movie_count")
            var movieCount: Int,
            @SerialName("movie_list_id")
            var movieListId: String,
            @SerialName("movie_list_name")
            var movieListName: String,
            @SerialName("num_fav")
            var numFav: Int,
            @SerialName("num_upvote")
            var numUpvote: Int
        )
    }
}
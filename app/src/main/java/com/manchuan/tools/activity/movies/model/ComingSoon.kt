package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Serializable
@Parcelize
data class ComingSoon(
    @SerialName("status")
    var status: Int = 0,
    @SerialName("data")
    var `data`: Data = Data(),
    @SerialName("msg")
    var msg: String = ""
) : Parcelable {
    @Serializable
    @Parcelize
    data class Data(
        @SerialName("films")
        var films: List<Film> = listOf(),
        @SerialName("total")
        var total: Int = 0
    ) : Parcelable {
        @Serializable
        @Parcelize
        data class Film(
            @SerialName("filmId")
            var filmId: Int = 0,
            @SerialName("name")
            var name: String = "",
            @SerialName("poster")
            var poster: String = "",
            @SerialName("actors")
            var actors: List<Actor> = listOf(),
            @SerialName("director")
            var director: String = "",
            @SerialName("category")
            var category: String = "",
            @SerialName("synopsis")
            var synopsis: String = "",
            @SerialName("filmType")
            var filmType: FilmType = FilmType(),
            @SerialName("nation")
            var nation: String = "",
            @SerialName("language")
            var language: String = "",
            @SerialName("videoId")
            var videoId: String = "",
            @SerialName("premiereAt")
            var premiereAt: Int = 0,
            @SerialName("timeType")
            var timeType: Int = 0,
            @SerialName("runtime")
            var runtime: Int = 0,
            @SerialName("item")
            var item: Item = Item(),
            @SerialName("isPresale")
            var isPresale: Boolean = false,
            @SerialName("isSale")
            var isSale: Boolean = false
        ) : Parcelable {
            @Serializable
            @Parcelize
            data class Actor(
                @SerialName("name")
                var name: String = "",
                @SerialName("role")
                var role: String = "",
                @SerialName("avatarAddress")
                var avatarAddress: String = ""
            ) : Parcelable

            @Serializable
            @Parcelize
            data class FilmType(
                @SerialName("name")
                var name: String = "",
                @SerialName("value")
                var value: Int = 0
            ) : Parcelable

            @Serializable
            @Parcelize
            data class Item(
                @SerialName("name")
                var name: String = "",
                @SerialName("type")
                var type: Int = 0
            ) : Parcelable
        }
    }
}
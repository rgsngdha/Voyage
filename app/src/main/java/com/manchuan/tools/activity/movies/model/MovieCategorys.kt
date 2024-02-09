package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieCategorys(
    @SerialName("data") var `data`: Data,
    @SerialName("info") var info: String,
    @SerialName("status") var status: Int,
) {
    @Serializable
    data class Data(
        @SerialName("list") var list: DataList,
        @SerialName("matches") var matches: String,
        @SerialName("results") var results: List<Result>,
        @SerialName("totalitems") var totalitems: String,
        @SerialName("type") var type: String,
    ) {
        @Serializable
        data class DataList(
            @SerialName("entity") var entity: String,
            @SerialName("filter_list") var filterList: List<Filter>,
            @SerialName("order") var order: String,
        ) {
            @Serializable
            data class Filter(
                @SerialName("name") var name: String,
                @SerialName("option_list") var optionList: List<String>,
                @SerialName("option_name") var optionName: String,
                @SerialName("selected") var selected: String,
            ) : java.io.Serializable
        }

        @Serializable
        data class Result(
            @SerialName("director") var director: String,
            @SerialName("dockey") var dockey: String,
            @SerialName("doctype") var doctype: String,
            @SerialName("duration") var duration: String,
            @SerialName("emcee") var emcee: String,
            @SerialName("hit_count") var hitCount: String,
            @SerialName("ipad_play_for_list") var ipadPlayForList: IpadPlayForList? = null,
            @SerialName("name") var name: String,
            @SerialName("picurl") var picurl: String,
            @SerialName("score") var score: String,
            @SerialName("date") var date: String? = "",
            @SerialName("shengyou") var shengyou: String,
            @SerialName("starring") var starring: String,
            @SerialName("style") var style: String,
            @SerialName("tiny_url") var tinyUrl: String,
            @SerialName("url") var url: String,
            @SerialName("v_height") var vHeight: String,
            @SerialName("v_picurl") var vPicurl: String,
            @SerialName("v_width") var vWidth: String,
            @SerialName("year") var year: String,
            @SerialName("zone") var zone: String,
        ) {
            @Serializable
            data class IpadPlayForList(
                @SerialName("fee") var fee: String? = null,
                @SerialName("episode") var episode: String? = "",
                @SerialName("finish_episode") var finishEpisode: String? = "",
            )
        }
    }
}
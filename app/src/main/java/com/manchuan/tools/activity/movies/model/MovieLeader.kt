package com.manchuan.tools.activity.movies.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieLeader(
    @SerialName("data") var `data`: List<Data>,
    @SerialName("status") var status: Int,
) {
    @Serializable
    data class Data(
        @SerialName("key") var key: String,
        @SerialName("resourceid") var resourceid: String,
        @SerialName("result") var result: Result,
        @SerialName("StdStg") var stdStg: String,
        @SerialName("StdStl") var stdStl: String,
        @SerialName("title") var title: String,
        @SerialName("tplt") var tplt: String,
        @SerialName("url") var url: String,
    ) {
        @Serializable
        data class Result(
            @SerialName("ae_sid") var aeSid: String,
            @SerialName("disp_data_url_ex") var dispDataUrlEx: DispDataUrlEx,
            @SerialName("need_select_tags") var needSelectTags: Boolean,
            @SerialName("replace_query") var replaceQuery: String,
            @SerialName("resourceType") var resourceType: String,
            @SerialName("resourceid") var resourceid: String,
            @SerialName("result") var result: List<Result>,
            @SerialName("selected") var selected: Int,
            @SerialName("showRightText") var showRightText: String,
            @SerialName("showRightUrl") var showRightUrl: String,
            @SerialName("tab") var tab: List<Tab>,
            @SerialName("tags") var tags: List<String>,
            @SerialName("title") var title: String,
            @SerialName("url") var url: String,
            @SerialName("video_type_list") var videoTypeList: List<String>,
        ) {
            @Serializable
            data class DispDataUrlEx(
                @SerialName("title") var title: String,
            )

            @Serializable
            data class Result(
                @SerialName("additional") var additional: String,
                @SerialName("ename") var ename: String,
                @SerialName("fxq_genre") var fxqGenre: List<String>? = null,
                @SerialName("img") var img: String,
                @SerialName("img_di") var imgDi: String,
                @SerialName("img_nsrc") var imgNsrc: String,
                @SerialName("jumpquery") var jumpquery: String,
                @SerialName("kg_sid") var kgSid: String,
                @SerialName("po_country") var poCountry: String,
                @SerialName("po_director") var poDirector: String? = null,
                @SerialName("po_starring_new") var poStarringNew: List<String>? = null,
                @SerialName("po_starring_x") var poStarringX: String? = null,
                @SerialName("regionalReleaseDate") var regionalReleaseDate: String? = null,
                @SerialName("score") var score: String,
                @SerialName("show_label") var showLabel: ShowLabel,
                @SerialName("video") var video: List<Video>? = null,
                @SerialName("videoType") var videoType: String,
                @SerialName("year") var year: String,
            ) {
                @Serializable
                data class ShowLabel(
                    @SerialName("text") var text: String,
                    @SerialName("type") var type: Int,
                )

                @Serializable
                data class Video(
                    @SerialName("is_pay") var isPay: String,
                    @SerialName("label") var label: List<Label>,
                    @SerialName("pay_label") var payLabel: PayLabel,
                    @SerialName("playTxt") var playTxt: String,
                    @SerialName("siteicon") var siteicon: String,
                    @SerialName("siteicon_di") var siteiconDi: String,
                    @SerialName("siteicon_nsrc") var siteiconNsrc: String,
                    @SerialName("sitename") var sitename: String,
                    @SerialName("sub_title") var subTitle: String,
                    @SerialName("url") var url: String,
                    @SerialName("url_di") var urlDi: String,
                    @SerialName("url_nsrc") var urlNsrc: String,
                    @SerialName("url_trans_feature") var urlTransFeature: UrlTransFeature? = null,
                    @SerialName("url_xcx_params") var urlXcxParams: UrlXcxParams? = null,
                ) {
                    @Serializable
                    data class Label(
                        @SerialName("text") var text: String,
                        @SerialName("type") var type: Int,
                    )

                    @Serializable
                    data class PayLabel(
                        @SerialName("text") var text: String,
                        @SerialName("type") var type: Int,
                    )

                    @Serializable
                    data class UrlTransFeature(
                        @SerialName("xcx_appid") var xcxAppid: String,
                        @SerialName("xcx_appkey") var xcxAppkey: String,
                        @SerialName("xcx_path") var xcxPath: String,
                        @SerialName("xcx_type") var xcxType: Int,
                        @SerialName("xcx_url") var xcxUrl: String,
                    )

                    @Serializable
                    data class UrlXcxParams(
                        @SerialName("xcx_appkey") var xcxAppkey: String,
                        @SerialName("xcx_from") var xcxFrom: String,
                        @SerialName("xcx_key") var xcxKey: String,
                        @SerialName("xcx_path") var xcxPath: String,
                        @SerialName("xcx_query") var xcxQuery: String,
                        @SerialName("xcx_url") var xcxUrl: String,
                    )
                }
            }

            @Serializable
            data class Tab(
                @SerialName("key") var key: String,
                @SerialName("type") var type: String,
                @SerialName("value") var value: List<Value>,
            ) {
                @Serializable
                data class Value(
                    @SerialName("code") var code: String,
                    @SerialName("text") var text: String,
                )
            }
        }
    }
}
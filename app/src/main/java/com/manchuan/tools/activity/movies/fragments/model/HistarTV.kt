package com.manchuan.tools.activity.movies.fragments.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistarTV(
    @SerialName("pageProps")
    var pageProps: PageProps = PageProps(),
    @SerialName("__N_SSG")
    var nSSG: Boolean = false
) {
    @Serializable
    data class PageProps(
        @SerialName("layoutProps")
        var layoutProps: LayoutProps = LayoutProps(),
        @SerialName("cards")
        var cards: List<Card> = listOf(),
        @SerialName("headers")
        var headers: List<Header> = listOf()
    ) {
        @Serializable
        data class LayoutProps(
            @SerialName("noSomeMetaInfo")
            var noSomeMetaInfo: Boolean = false,
            @SerialName("url")
            var url: String = "",
            @SerialName("title")
            var title: String = "",
            @SerialName("desc")
            var desc: String = ""
        )

        @Serializable
        data class Card(
            @SerialName("name")
            var name: String = "",
            @SerialName("cards")
            var cards: List<Card> = listOf(),
            @SerialName("ban")
            var ban: Boolean = false
        ) {
            @Serializable
            data class Card(
                @SerialName("name")
                var name: String = "",
                @SerialName("img")
                var img: String = "",
                @SerialName("id")
                var id: Int = 0,
                @SerialName("countStr")
                var countStr: String = "",
                @SerialName("url")
                var url: String = ""
            )
        }

        @Serializable
        data class Header(
            @SerialName("id")
            var id: Int = 0,
            @SerialName("label")
            var label: String = "",
            @SerialName("countStr")
            var countStr: String = "",
            @SerialName("focusName")
            var focusName: String = "",
            @SerialName("name")
            var name: String = "",
            @SerialName("posterImg")
            var posterImg: String = "",
            @SerialName("picImg")
            var picImg: String = "",
            @SerialName("startTime")
            var startTime: Int = 0,
            @SerialName("endTime")
            var endTime: Int = 0
        )
    }
}
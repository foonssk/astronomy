package com.frolova.astronomyguide

data class NewsItem(
    val id: Int,
    val title: String,
    val text: String,
    var likes: Int = 0,
    var isLiked: Boolean = false
)

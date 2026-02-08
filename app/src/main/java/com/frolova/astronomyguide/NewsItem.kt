package com.frolova.astronomyguide

data class NewsItem(
    val id: Int,
    val title: String,
    val content: String,
    var isLiked: Boolean = false,
    var likes: Int = 0
)
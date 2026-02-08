package com.frolova.astronomyguide

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlin.random.Random
import androidx.lifecycle.viewModelScope

class NewsViewModel : ViewModel() {

    private val allNews = listOf(
        NewsItem(1, "Черные дыры", "Учёные обнаружили новую сверхмассивную чёрную дыру..."),
        NewsItem(2, "Марс", "NASA планирует отправить людей на Марс к 2030 году..."),
        NewsItem(3, "Экзопланеты", "Обнаружена планета в зоне обитания звезды TRAPPIST-1..."),
        NewsItem(4, "Телескоп Джеймс Уэбб", "Новые снимки далёких галактик поразили учёных..."),
        NewsItem(5, "Солнечные вспышки", "Сильнейшая солнечная буря за последние 10 лет..."),
        NewsItem(6, "Луна", "Китай построит базу на обратной стороне Луны..."),
        NewsItem(7, "Гравитационные волны", "LIGO зарегистрировала слияние нейтронных звёзд..."),
        NewsItem(8, "Космический мусор", "Растёт угроза столкновений на орбите Земли..."),
        NewsItem(9, "Темная материя", "Новые данные указывают на её распределение в скоплениях..."),
        NewsItem(10, "Астероиды", "Опасный астероид пролетит рядом с Землёй в 2027..."),
    )

    private val _currentNews = mutableStateOf<List<NewsItem>>(emptyList())
    val currentNews: State<List<NewsItem>> = _currentNews

    init {
        refreshNews()
        startAutoRefresh()
    }

    private fun refreshNews() {
        _currentNews.value = List(4) { allNews.random() }
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                val indexToReplace = Random.nextInt(4)
                val newNews = allNews.random()
                val updated = _currentNews.value.toMutableList()
                updated[indexToReplace] = newNews.copy(likes = 0)
                _currentNews.value = updated
            }
        }

    }

    fun toggleLike(news: NewsItem) {
        val updated = _currentNews.value.toMutableList()
        val index = updated.indexOfFirst { it.id == news.id }
        if (index != -1) {
            val current = updated[index]
            updated[index] = current.copy(
                isLiked = !current.isLiked,
                likes = if (current.isLiked) current.likes - 1 else current.likes + 1
            )
            _currentNews.value = updated
        }
    }
}


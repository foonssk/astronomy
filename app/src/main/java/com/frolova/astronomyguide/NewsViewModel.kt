package com.frolova.astronomyguide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NewsViewModel: ViewModel() {
    private val allNews = listOf(
        NewsItem(1,"Парад планет", "28 февраля 2026 года после заката на небе выстроятся в дугу 6 планет. Юпитер, Сатурн, Венеру и Меркурий можно будет увидеть невооруженным глазом", 100),
        NewsItem(2,"Кольцеобразное солнечное затмение", "17 февраля 2026 года Луна закроет Солнце на 96%, образуя 'огненное кольцо'", 2010),
        NewsItem(3,"Полное лунное затмение", "2 марта 2026 года будет наблюдаться в Восточной Азиии, на северо-западе Северной Америки и в Тихоокеанском регионе", 890),
        NewsItem(4,"Открытие 'Джеймса Уэбба'", "Телескоп обнаружил, что верхняя атмосфера Урана продолжает охлаждаться", 222),
        NewsItem(5,"Астероид 2024 YR4", "29 января 2026 года зафиксировано сближение астероида с Луной.", 12345),
        NewsItem(6,"Суперлуние", "24 декабря 2026 года Луна подойдет максимально близко к Земле.", 52),
        NewsItem(7,"60-летие первой мягкой посадки", "3 февраля научное сообщество отметило юбилей миссии 'Луна-9'.", 67),
        NewsItem(8,"ИИ-водитель на Марсе", "Ровер Perseverance успешно завершил свою первую поездку, полностью спланированную алгоритмами искусственного интеллекта", 5551),
        NewsItem(9,"Прогноз бури", "Основной удар солнечной плазмы ожидается 5 февраля 2026 года.", 0),
        NewsItem(10,"Звезда-фотобомба", "На новом снимке 'Хаббла' была замечена тейтронная звезда, которая случайно пролетела на фоне формирующейся звездной колыбели в Большом Магеллановом Облаке", 2)
    )
    private val viewedNews = mutableMapOf<Int, NewsItem>()
    private val _currentNews = MutableStateFlow<List<NewsItem>>(emptyList())
    val currentNews = _currentNews.asStateFlow()

    init {
        val initial = allNews.shuffled().take(4)
        initial.forEach { viewedNews[it.id] = it.copy() }
        _currentNews.value = initial
        startNewsRotation()
    }

    private fun startNewsRotation(){
        viewModelScope.launch {
            while(isActive){
                delay(5000)
                rotateOneNews()
            }
        }
    }

    private fun rotateOneNews() {
        val current = _currentNews.value.toMutableList()
        if (current.size != 4) return

        val indexToChange = (0..3).random()

        val usedIds = current.map { it.id }.toSet()

        val candidates = allNews.filter { it.id !in usedIds }

        if (candidates.isEmpty()) return

        val selectedOriginal = candidates.random()

        val selected = viewedNews.getOrPut(selectedOriginal.id) {
            selectedOriginal.copy()
        }

        current[indexToChange] = selected
        _currentNews.value = current
    }

    fun toggleLike(news: NewsItem) {
        val currentList = _currentNews.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == news.id }
        if (index == -1) return

        val updatedNews = news.copy(
            isLiked = !news.isLiked,
            likes = if (!news.isLiked) news.likes + 1 else originalLikes(news)
        )

        currentList[index] = updatedNews

        viewedNews[news.id] = updatedNews

        _currentNews.value = currentList
    }

    private fun originalLikes(news: NewsItem): Int {
        return allNews.first { it.id == news.id }.likes
    }

    fun getDisplayLikes(news: NewsItem): Int = news.likes
}
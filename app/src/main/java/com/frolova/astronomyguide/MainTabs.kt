package com.frolova.astronomyguide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

// Главный экран с переключением вкладок
@Composable
fun MainTabs(viewModel: NewsViewModel, modifier: Modifier = Modifier){
    // Сохраняем индекс активной вкладки при пересоздании
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    // Список названий вкладок
    val tabs = listOf("Новости", "3D галактика")

    Column(modifier = modifier.fillMaxSize()){
        // === ВЕРХНЯЯ ПАНЕЛЬ: ВКЛАДКИ ===
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Black,  // Фон панели
            contentColor = Color.White     // Цвет индикатора
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }, // Смена вкладки по клику
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index) Color.White else Color.LightGray
                        )
                    },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.LightGray
                )
            }
        }

        // === КОНТЕНТ ВКЛАДОК ===
        when (selectedTab){
            0 -> FourQuartersScreen( // Вкладка новостей
                viewModel = viewModel,
                getDisplayLikes = { news -> viewModel.getDisplayLikes(news) },
                onToggleLike = { news -> viewModel.toggleLike(news) },
                modifier = Modifier.weight(1f)
            )
            1 -> OpenGLScreen( // Вкладка 3D-галактики
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Экран с 3D-сценой и кнопками управления
@Composable
fun OpenGLScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var glView: OpenGLView? = null // Ссылка на OpenGLView для управления

    Column(modifier = modifier.fillMaxSize()) {
        // === 3D-СЦЕНА (90% экрана) ===
        AndroidView(
            factory = { ctx ->
                OpenGLView(ctx).also { glView = it } // Создаем View и сохраняем ссылку
            },
            modifier = Modifier.weight(9f).fillMaxSize()
        )

        // === ПАНЕЛЬ УПРАВЛЕНИЯ (10% экрана) ===
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка: предыдущая планета
            Button(onClick = {
                glView?.getRenderer()?.selectPrev()
            }) {
                Text("Влево")
            }

            // Кнопка: информация (заглушка)
            Button(onClick = {}) {
                Text("Инфо")
            }

            // Кнопка: следующая планета
            Button(onClick = {
                glView?.getRenderer()?.selectNext()
            }) {
                Text("Вправо")
            }
        }
    }
}
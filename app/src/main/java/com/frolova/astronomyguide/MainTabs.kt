package com.frolova.astronomyguide

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MainTabs(viewModel: NewsViewModel, modifier: Modifier = Modifier){
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf("Новости", "3D галактика")

    Column(modifier = modifier.fillMaxSize()){
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index},
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab){
            0 -> FourQuartersScreen(
                viewModel = viewModel,
                getDisplayLikes = { news -> viewModel.getDisplayLikes(news) },
                onToggleLike = { news -> viewModel.toggleLike(news) },
                modifier = Modifier.weight(1f)
            )
            1 -> OpenGLScreen(
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun OpenGLScreen(modifier: Modifier = Modifier){ // Экран с OpenGL
    val context = LocalContext.current // текущий контекст Android

    AndroidView( // компонент-обертка Composable, для встраивания традиционных View-элементов из android в интерфейс с Jetpack Compose
        factory = {
                ctx -> OpenGLView(ctx) // создание экземпляра OpenGl View с контекстом
        },
        modifier = modifier.fillMaxSize() // модификатор с заполнением всего пространства
    )
}
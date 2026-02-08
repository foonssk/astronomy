package com.frolova.astronomyguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(viewModel = viewModel())
        }
    }
}

@Composable
fun MainScreen(viewModel: NewsViewModel, modifier: Modifier = Modifier) {
    FourQuartersScreen(
        viewModel = viewModel,
        getDisplayLikes = { news -> news.likes },
        onToggleLike = { news -> viewModel.toggleLike(news) },
        modifier = modifier
    )
}

@Composable
fun FourQuartersScreen(
    viewModel: NewsViewModel,
    getDisplayLikes: (NewsItem) -> Int,
    onToggleLike: (NewsItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val newsList by viewModel.currentNews

    if (newsList.size != 4) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Загрузка...")
        }
        return
    }

    val quarterColors = listOf(
        Color(0xFFE40045), // Cyan
        Color(0xFFFF4900), // Purple
        Color(0xFFA600A6), // Orange
        Color(0xFF67E300)  // Pink
    )

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Quarter(
                news = newsList[0],
                displayLikes = getDisplayLikes(newsList[0]),
                onLikeClick = { onToggleLike(newsList[0]) },
                backgroundColor = quarterColors[0],
                modifier = Modifier.weight(1f)
            )
            Quarter(
                news = newsList[1],
                displayLikes = getDisplayLikes(newsList[1]),
                onLikeClick = { onToggleLike(newsList[1]) },
                backgroundColor = quarterColors[1],
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Quarter(
                news = newsList[2],
                displayLikes = getDisplayLikes(newsList[2]),
                onLikeClick = { onToggleLike(newsList[2]) },
                backgroundColor = quarterColors[2],
                modifier = Modifier.weight(1f)
            )
            Quarter(
                news = newsList[3],
                displayLikes = getDisplayLikes(newsList[3]),
                onLikeClick = { onToggleLike(newsList[3]) },
                backgroundColor = quarterColors[3],
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Quarter(
    news: NewsItem,
    displayLikes: Int,
    onLikeClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Верхняя часть — 90%
        Column(
            modifier = Modifier
                .weight(9f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = news.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = news.content,
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }

        // Нижняя часть — 10% (лайки)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .clickable(onClick = onLikeClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Like",
                tint = if (news.isLiked) Color.Red else Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$displayLikes лайков",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
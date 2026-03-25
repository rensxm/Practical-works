@file:OptIn(ExperimentalFoundationApi::class)

package com.example.myapplication

import androidx.compose.foundation.ExperimentalFoundationApi
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Data class
data class MovieItem(
    val title: String,
    val imageRes: Int,
    val director: String,
    val year: Int,
    val detailedInfo: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 2. Коллекция данных
            val movies = listOf(
                MovieItem("Inception", R.drawable.inception, "Christopher Nolan", 2010, "Фильм о снах и реальности"),
                MovieItem("Interstellar", R.drawable.interstellar, "Christopher Nolan", 2014, "Космическая эпопея"),
                MovieItem("The Matrix", R.drawable.matrix, "Wachowski Sisters", 1999, "Культовый фильм о виртуальной реальности"),
                MovieItem("Avatar", R.drawable.avatar, "James Cameron", 2009, "Фантастика о планете Пандора"),
                MovieItem("Titanic", R.drawable.titanic, "James Cameron", 1997, "История любви на фоне катастрофы"),
                MovieItem("Gladiator", R.drawable.gladiator, "Ridley Scott", 2000, "История римского генерала"),
                MovieItem("The Dark Knight", R.drawable.dark_knight, "Christopher Nolan", 2008, "Бэтмен против Джокера"),
                MovieItem("Fight Club", R.drawable.fight_club, "David Fincher", 1999, "Фильм о двойной личности"),
                MovieItem("Forrest Gump", R.drawable.forest_gump, "Robert Zemeckis", 1994, "История жизни Форреста"),
                MovieItem("The Godfather", R.drawable.godfather, "Francis Ford Coppola", 1972, "Мафия и семейные ценности")
            )


            MovieApp(movies)
        }
    }
}

@Composable
fun MovieApp(movies: List<MovieItem>) {
    var currentIndex by remember { mutableStateOf(0) }
    val currentMovie = movies[currentIndex]

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Изображение
        Image(
            painter = painterResource(id = currentMovie.imageRes),
            contentDescription = currentMovie.title,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Основная информация
        Text(text = currentMovie.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "${currentMovie.director} (${currentMovie.year})", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Навигация
        Row {
            Button(
                onClick = { if (currentIndex > 0) currentIndex-- },
                enabled = currentIndex > 0
            ) {
                Text("Предыдущий")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { if (currentIndex < movies.size - 1) currentIndex++ },
                enabled = currentIndex < movies.size - 1
            ) {
                Text("Следующий")
            }
        }
    }
}

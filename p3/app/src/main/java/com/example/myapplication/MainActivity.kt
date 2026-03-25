package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Data class
data class FactItem(
    val day: Int,
    val title: String,
    val description: String,
    val imageRes: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF6200EE),
                    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC5)
                )
            ) {
                // 2. Коллекция данных (пример)
                val facts = listOf(
                    FactItem(1, "Spend 15 minutes outdoors", "Прогуляйся на свежем воздухе", R.drawable.outdoors),
                    FactItem(2, "Listen to a new podcast", "Попробуй новый подкаст или аудиокнигу", R.drawable.podcast),
                    FactItem(3, "Drink more water", "Следи за водным балансом", R.drawable.water),
                    FactItem(4, "Read a book", "Выдели время на чтение", R.drawable.book),
                    FactItem(5, "Meditate", "Попробуй 10 минут медитации", R.drawable.meditation)
                    // ... добавь до 30 фактов
                )

                FactList(facts)
            }
        }
    }
}

@Composable
fun FactList(facts: List<FactItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(facts) { fact ->
            FactCard(fact)
        }
    }
}

@Composable
fun FactCard(fact: FactItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Day ${fact.day}", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = fact.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = fact.imageRes),
                contentDescription = fact.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = fact.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


// 1. Data class
data class Place(
    val id: Int,
    val name: String,
    val category: String,
    val description: String,
    val imageRes: Int,
    val rating: Float = 0f
)

// 2. Репозиторий
object PlacesRepository {
    private val places = listOf(
        Place(1, "набережная", "Достопримечательности", "пляж", R.drawable.beach, 4.8f),
        Place(2, "авганский парк", "Парки", "Популярное место для отдыха", R.drawable.park_afgancev, 4.5f),
        Place(3, "салтанат", "Рестораны", "Традиционная кухня", R.drawable.saltanat, 4.2f)
    )

    fun getCategories(): List<String> = listOf("Достопримечательности", "Парки", "Рестораны")
    fun getPlacesByCategory(category: String): List<Place> = places.filter { it.category == category }
    fun getPlaceById(id: Int): Place? = places.find { it.id == id }
}

// 3. ViewModel
class PlacesViewModel : ViewModel() {
    private val _places = mutableStateOf<List<Place>>(emptyList())
    val places: State<List<Place>> = _places

    fun loadPlaces(category: String) {
        _places.value = PlacesRepository.getPlacesByCategory(category)
    }
}

// 4. Навигация
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "categories") {
        composable("categories") { CategoriesScreen(navController) }
        composable("places/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            PlacesScreen(category, navController)
        }
        composable("details/{placeId}") { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId")?.toIntOrNull()
            placeId?.let { DetailsScreen(it) }
        }
    }
}

// 5. Экраны
@Composable
fun CategoriesScreen(navController: NavController) {
    val categories = PlacesRepository.getCategories()
    LazyColumn {
        items(categories) { category ->
            Card(
                onClick = { navController.navigate("places/$category") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(category, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun PlacesScreen(category: String, navController: NavController) {
    val viewModel: PlacesViewModel = viewModel()
    val places by viewModel.places

    LaunchedEffect(category) {
        viewModel.loadPlaces(category)
    }

    LazyColumn {
        items(places) { place ->
            PlaceItem(place, onItemClick = { navController.navigate("details/${place.id}") })
        }
    }
}

@Composable
fun PlaceItem(place: Place, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        onClick = onItemClick
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = place.imageRes),
                contentDescription = place.name,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(place.name, fontWeight = FontWeight.Bold)
                Text("Рейтинг: ${place.rating}")
            }
        }
    }
}

@Composable
fun DetailsScreen(placeId: Int) {
    val place = PlacesRepository.getPlaceById(placeId)
    place?.let {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(it.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(id = it.imageRes),
                contentDescription = it.name,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(it.description)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Рейтинг: ${it.rating}")
        }
    }
}

// 6. MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


// ---------- МОДЕЛИ ----------
data class Character(val id: Int, val name: String, val status: String, val species: String, val image: String)
data class CharacterResponse(val results: List<Character>)

data class Location(val id: Int, val name: String, val type: String, val dimension: String)
data class LocationResponse(val results: List<Location>)

data class Episode(val id: Int, val name: String, val episode: String, val air_date: String)
data class EpisodeResponse(val results: List<Episode>)

// ---------- API ----------
interface RickMortyApiService {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): CharacterResponse

    @GET("location")
    suspend fun getLocations(@Query("page") page: Int): LocationResponse

    @GET("episode")
    suspend fun getEpisodes(@Query("page") page: Int): EpisodeResponse
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://rickandmortyapi.com/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(RickMortyApiService::class.java)

// ---------- VIEWMODEL ----------
class RickMortyViewModel : ViewModel() {
    var characters by mutableStateOf<List<Character>>(emptyList())
    var locations by mutableStateOf<List<Location>>(emptyList())
    var episodes by mutableStateOf<List<Episode>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun loadCharacters() {
        viewModelScope.launch {
            isLoading = true
            try {
                characters = api.getCharacters(1).results
            } catch (e: Exception) {
                error = e.message
            }
            isLoading = false
        }
    }

    fun loadLocations() {
        viewModelScope.launch {
            isLoading = true
            try {
                locations = api.getLocations(1).results
            } catch (e: Exception) {
                error = e.message
            }
            isLoading = false
        }
    }

    fun loadEpisodes() {
        viewModelScope.launch {
            isLoading = true
            try {
                episodes = api.getEpisodes(1).results
            } catch (e: Exception) {
                error = e.message
            }
            isLoading = false
        }
    }
}

// ---------- UI ----------
@Composable
fun RickMortyApp(viewModel: RickMortyViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Персонажи", "Локации", "Эпизоды")

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                    Text(title, modifier = Modifier.padding(16.dp))
                }
            }
        }

        when (selectedTab) {
            0 -> {
                LaunchedEffect(Unit) { viewModel.loadCharacters() }
                CharacterList(viewModel)
            }
            1 -> {
                LaunchedEffect(Unit) { viewModel.loadLocations() }
                LocationList(viewModel)
            }
            2 -> {
                LaunchedEffect(Unit) { viewModel.loadEpisodes() }
                EpisodeList(viewModel)
            }
        }
    }
}

@Composable
fun CharacterList(viewModel: RickMortyViewModel) {
    if (viewModel.isLoading) CircularProgressIndicator()
    viewModel.error?.let { Text("Error: $it") }
    LazyColumn {
        items(viewModel.characters) { character ->
            Card(modifier = Modifier.padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AsyncImage(model = character.image, contentDescription = character.name)
                    Text(character.name, fontWeight = FontWeight.Bold)
                    Text("Status: ${character.status}")
                    Text("Species: ${character.species}")
                }
            }
        }
    }
}

@Composable
fun LocationList(viewModel: RickMortyViewModel) {
    if (viewModel.isLoading) CircularProgressIndicator()
    viewModel.error?.let { Text("Error: $it") }
    LazyColumn {
        items(viewModel.locations) { location ->
            Card(modifier = Modifier.padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(location.name, fontWeight = FontWeight.Bold)
                    Text("Type: ${location.type}")
                    Text("Dimension: ${location.dimension}")
                }
            }
        }
    }
}

@Composable
fun EpisodeList(viewModel: RickMortyViewModel) {
    if (viewModel.isLoading) CircularProgressIndicator()
    viewModel.error?.let { Text("Error: $it") }
    LazyColumn {
        items(viewModel.episodes) { episode ->
            Card(modifier = Modifier.padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(episode.name, fontWeight = FontWeight.Bold)
                    Text("Code: ${episode.episode}")
                    Text("Air date: ${episode.air_date}")
                }
            }
        }
    }
}

// ---------- MAIN ----------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RickMortyApp()
            }
        }
    }
}


package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// ══════════════════════════════════════════════════════════════
//  МОДЕЛИ
// ══════════════════════════════════════════════════════════════

data class Genre(val id: Int, val name: String)

data class Movie(
    val id: Int = 0,
    val title: String,
    val description: String,
    val director: String,
    val year: Int,
    val rating: Float,
    val duration: Int,
    val genreId: Int?,
    val genreName: String? = null
)

// ══════════════════════════════════════════════════════════════
//  БАЗА ДАННЫХ
// ══════════════════════════════════════════════════════════════

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "movies.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE genres (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE movies (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                director TEXT,
                year INTEGER,
                rating REAL,
                duration INTEGER,
                genre_id INTEGER,
                FOREIGN KEY(genre_id) REFERENCES genres(id)
            )
        """)
        // Начальные жанры
        listOf("Боевик", "Комедия", "Драма", "Ужасы", "Фантастика", "Триллер").forEach {
            val cv = ContentValues()
            cv.put("name", it)
            db.insert("genres", null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS movies")
        db.execSQL("DROP TABLE IF EXISTS genres")
        onCreate(db)
    }

    // ── Жанры ──────────────────────────────────────────

    fun getAllGenres(): List<Genre> {
        val list = mutableListOf<Genre>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM genres ORDER BY name", null)
        while (cursor.moveToNext()) {
            list.add(Genre(cursor.getInt(0), cursor.getString(1)))
        }
        cursor.close()
        return list
    }

    fun insertGenre(name: String) {
        val cv = ContentValues()
        cv.put("name", name)
        writableDatabase.insert("genres", null, cv)
    }

    fun deleteGenre(id: Int) {
        writableDatabase.delete("genres", "id = ?", arrayOf(id.toString()))
    }

    // ── Фильмы ─────────────────────────────────────────

    fun getAllMovies(search: String = "", genreId: Int? = null): List<Movie> {
        val list = mutableListOf<Movie>()
        val query = """
            SELECT m.id, m.title, m.description, m.director, m.year, m.rating, m.duration,
                   m.genre_id, g.name
            FROM movies m
            LEFT JOIN genres g ON m.genre_id = g.id
            WHERE m.title LIKE ?
            ${if (genreId != null) "AND m.genre_id = $genreId" else ""}
            ORDER BY m.title
        """
        val cursor = readableDatabase.rawQuery(query, arrayOf("%$search%"))
        while (cursor.moveToNext()) {
            list.add(
                Movie(
                    id = cursor.getInt(0),
                    title = cursor.getString(1),
                    description = cursor.getString(2) ?: "",
                    director = cursor.getString(3) ?: "",
                    year = cursor.getInt(4),
                    rating = cursor.getFloat(5),
                    duration = cursor.getInt(6),
                    genreId = if (cursor.isNull(7)) null else cursor.getInt(7),
                    genreName = cursor.getString(8)
                )
            )
        }
        cursor.close()
        return list
    }

    fun insertMovie(movie: Movie) {
        val cv = ContentValues().apply {
            put("title", movie.title)
            put("description", movie.description)
            put("director", movie.director)
            put("year", movie.year)
            put("rating", movie.rating)
            put("duration", movie.duration)
            if (movie.genreId != null) put("genre_id", movie.genreId) else putNull("genre_id")
        }
        writableDatabase.insert("movies", null, cv)
    }

    fun updateMovie(movie: Movie) {
        val cv = ContentValues().apply {
            put("title", movie.title)
            put("description", movie.description)
            put("director", movie.director)
            put("year", movie.year)
            put("rating", movie.rating)
            put("duration", movie.duration)
            if (movie.genreId != null) put("genre_id", movie.genreId) else putNull("genre_id")
        }
        writableDatabase.update("movies", cv, "id = ?", arrayOf(movie.id.toString()))
    }

    fun deleteMovie(id: Int) {
        writableDatabase.delete("movies", "id = ?", arrayOf(id.toString()))
    }
}

// ══════════════════════════════════════════════════════════════
//  АДАПТЕР
// ══════════════════════════════════════════════════════════════

class MovieAdapter(
    private val onEdit: (Movie) -> Unit,
    private val onDelete: (Movie) -> Unit,
    private val onDetail: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.VH>(object : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(a: Movie, b: Movie) = a.id == b.id
    override fun areContentsTheSame(a: Movie, b: Movie) = a == b
}) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvInfo: TextView = view.findViewById(R.id.tvInfo)
        val tvGenre: TextView = view.findViewById(R.id.tvGenre)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = getItem(position)
        holder.tvTitle.text = m.title
        holder.tvInfo.text = "${m.year} • ⭐${m.rating} • ${m.duration} мин • ${m.director}"
        holder.tvGenre.text = m.genreName ?: "без жанра"
        holder.itemView.setOnClickListener { onDetail(m) }
        holder.btnEdit.setOnClickListener { onEdit(m) }
        holder.btnDelete.setOnClickListener { onDelete(m) }
    }
}

// ══════════════════════════════════════════════════════════════
//  MAIN ACTIVITY
// ══════════════════════════════════════════════════════════════

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: MovieAdapter
    private var currentSearch = ""
    private var currentGenreId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)
        setupRecyclerView()
        setupSearch()
        setupGenreSpinner()
        setupFab()
        loadMovies()
    }

    // ── Список фильмов ────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = MovieAdapter(
            onEdit = { showMovieDialog(it) },
            onDelete = { movie ->
                AlertDialog.Builder(this)
                    .setTitle("Удалить фильм?")
                    .setMessage("«${movie.title}» будет удалён.")
                    .setPositiveButton("Удалить") { _, _ ->
                        db.deleteMovie(movie.id)
                        loadMovies()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            },
            onDetail = { showDetailDialog(it) }
        )
        val rv = findViewById<RecyclerView>(R.id.recyclerView)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun loadMovies() {
        val movies = db.getAllMovies(currentSearch, currentGenreId)
        adapter.submitList(movies)
        findViewById<TextView>(R.id.tvEmpty).visibility =
            if (movies.isEmpty()) View.VISIBLE else View.GONE
    }

    // ── Поиск ─────────────────────────────────────────────────

    private fun setupSearch() {
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearch = newText.orEmpty()
                loadMovies()
                return true
            }
        })
    }

    // ── Фильтр по жанру (Spinner) ─────────────────────────────

    private fun setupGenreSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinnerFilter)
        val genres = db.getAllGenres()
        val allGenres = listOf(Genre(0, "Все жанры")) + genres
        val names = allGenres.map { it.name }

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                currentGenreId = if (pos == 0) null else allGenres[pos].id
                loadMovies()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ── FAB ───────────────────────────────────────────────────

    private fun setupFab() {
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab)
            .setOnClickListener { showMovieDialog(null) }
    }

    // ── Диалог добавления/редактирования ──────────────────────

    private fun showMovieDialog(existing: Movie?) {
        val view = layoutInflater.inflate(R.layout.dialog_movie, null)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etDirector = view.findViewById<EditText>(R.id.etDirector)
        val etYear = view.findViewById<EditText>(R.id.etYear)
        val etRating = view.findViewById<EditText>(R.id.etRating)
        val etDuration = view.findViewById<EditText>(R.id.etDuration)
        val spinnerGenre = view.findViewById<Spinner>(R.id.spinnerGenre)

        // Заполнить спиннер жанров
        val genres = db.getAllGenres()
        val allGenres = listOf(Genre(0, "— без жанра —")) + genres
        spinnerGenre.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, allGenres.map { it.name }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Если редактирование — подставить данные
        existing?.let {
            etTitle.setText(it.title)
            etDescription.setText(it.description)
            etDirector.setText(it.director)
            etYear.setText(it.year.toString())
            etRating.setText(it.rating.toString())
            etDuration.setText(it.duration.toString())
            val idx = allGenres.indexOfFirst { g -> g.id == it.genreId }
            if (idx >= 0) spinnerGenre.setSelection(idx)
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Добавить фильм" else "Редактировать")
            .setView(view)
            .setPositiveButton("Сохранить") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isBlank()) {
                    Toast.makeText(this, "Введите название!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val selectedPos = spinnerGenre.selectedItemPosition
                val genreId = if (selectedPos == 0) null else allGenres[selectedPos].id

                val movie = Movie(
                    id = existing?.id ?: 0,
                    title = title,
                    description = etDescription.text.toString().trim(),
                    director = etDirector.text.toString().trim(),
                    year = etYear.text.toString().toIntOrNull() ?: 2024,
                    rating = etRating.text.toString().toFloatOrNull()?.coerceIn(0f, 10f) ?: 0f,
                    duration = etDuration.text.toString().toIntOrNull() ?: 0,
                    genreId = genreId
                )

                if (existing == null) db.insertMovie(movie) else db.updateMovie(movie)
                loadMovies()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // ── Диалог детальной информации ───────────────────────────

    private fun showDetailDialog(movie: Movie) {
        val msg = buildString {
            appendLine("🎬 Режиссёр: ${movie.director.ifBlank { "—" }}")
            appendLine("📅 Год: ${movie.year}")
            appendLine("⭐ Рейтинг: ${movie.rating}/10")
            appendLine("⏱ Длительность: ${movie.duration} мин")
            appendLine("🎭 Жанр: ${movie.genreName ?: "не указан"}")
            appendLine()
            appendLine("📝 Описание:")
            append(movie.description.ifBlank { "—" })
        }
        AlertDialog.Builder(this)
            .setTitle(movie.title)
            .setMessage(msg)
            .setPositiveButton("ОК", null)
            .setNeutralButton("✏️ Редактировать") { _, _ -> showMovieDialog(movie) }
            .show()
    }
}
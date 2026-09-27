package com.example.parcial1buscadordelibros

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parcial1buscadordelibros.adapter.BookAdapter
import com.example.parcial1buscadordelibros.databinding.ActivityMainBinding
import com.example.parcial1buscadordelibros.network.NetworkChecker
import com.example.parcial1buscadordelibros.network.RetrofitClient
import com.example.parcial1buscadordelibros.repository.BookRepository
import com.example.parcial1buscadordelibros.viewmodel.BookViewModel
import com.example.parcial1buscadordelibros.viewmodel.BookViewModelFactory

/**
 * Pantalla 1: buscador + lista de resultados (la "V" de MVVM).
 *
 * Solo muestra y escucha toques. La busqueda contra la API la hace el ViewModel;
 * esta clase se limita a observar los LiveData y actualizar las vistas.
 */
class MainActivity : AppCompatActivity() {

    // View Binding: acceso a las vistas de activity_main.xml sin findViewById.
    private lateinit var binding: ActivityMainBinding

    // Lo guardamos como propiedad para poder refrescarlo desde el observer.
    private lateinit var adapter: BookAdapter

    // El delegate crea el ViewModel, o recupera el que ya existia si la pantalla
    // se volvio a crear (por ejemplo al rotar).
    //
    // Aca es donde se arman las dependencias y se inyectan: la Activity construye
    // el repositorio con lo que necesita y se lo pasa al ViewModel a traves de la
    // fabrica. El ViewModel no se crea nada por su cuenta, solo usa lo que recibe.
    private val viewModel: BookViewModel by viewModels {
        BookViewModelFactory(
            repository = BookRepository(
                apiService = RetrofitClient.openLibraryApiService,
                networkChecker = NetworkChecker(applicationContext)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    /** Deja la lista lista para usar: orientacion vertical y adapter enganchado. */
    private fun setupRecyclerView() {
        adapter = BookAdapter(emptyList()) { book ->
            // Tocaron un libro: abrimos el detalle mandandolo dentro del Intent.
            // Viaja porque Book es Serializable.
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_BOOK, book)
            startActivity(intent)
        }

        binding.rvBooks.layoutManager = LinearLayoutManager(this)
        binding.rvBooks.adapter = adapter
    }

    /** Valida lo que escribio el usuario y se lo pasa al ViewModel. */
    private fun setupClickListeners() {
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()

            if (query.isNotEmpty()) {
                viewModel.searchBooks(query)
            } else {
                Toast.makeText(this, R.string.error_empty_query, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Nos suscribimos a los LiveData del ViewModel. Cada vez que cambian, estos
     * bloques corren solos y refrescan la pantalla.
     */
    private fun observeViewModel() {
        viewModel.books.observe(this) { books ->
            adapter.updateList(books)
            binding.rvBooks.visibility = if (books.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // Mientras carga bloqueamos el boton para no disparar dos busquedas.
            binding.btnSearch.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.tvMessage.text = message
                binding.tvMessage.visibility = View.VISIBLE
            } else {
                binding.tvMessage.visibility = View.GONE
            }
        }
    }

    /**
     * Como la app va de punta a punta de la pantalla (edge to edge), agregamos
     * padding para que el contenido no quede debajo de las barras del sistema.
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

package com.example.parcial1buscadordelibros.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parcial1buscadordelibros.model.Book
import com.example.parcial1buscadordelibros.repository.BookRepository
import com.example.parcial1buscadordelibros.repository.SearchResult

/**
 * ViewModel de la pantalla principal (la "VM" de MVVM).
 *
 * Le pide la busqueda al repositorio y traduce el resultado a los tres estados
 * que la pantalla necesita: la lista, si esta cargando, y el mensaje de error.
 * No sabe nada de Retrofit ni de como se consiguen los datos.
 *
 * Al sobrevivir a la rotacion, la busqueda no se pierde si se gira el celular.
 *
 * Recibe el repositorio por constructor (inyeccion de dependencias): quien lo
 * crea decide que repositorio usar, y para testearlo se le podria pasar uno falso.
 */
class BookViewModel(private val repository: BookRepository) : ViewModel() {

    // Patron de siempre: el MutableLiveData es privado (solo lo toca el ViewModel)
    // y afuera se expone la version de solo lectura.
    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> get() = _books

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    /** Dispara la busqueda y publica el resultado en los LiveData. */
    fun searchBooks(query: String) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.searchBooks(query) { result ->
            _isLoading.value = false

            // El when sobre la sealed class obliga a cubrir los cuatro casos.
            when (result) {
                is SearchResult.Success -> {
                    _books.value = result.books

                    // Anduvo bien pero no hubo coincidencias.
                    if (result.books.isEmpty()) {
                        _errorMessage.value = "No encontramos libros con esa búsqueda."
                    }
                }

                is SearchResult.NoConnection ->
                    showError("Sin conexión a internet. Fijate la red y probá de nuevo.")

                is SearchResult.HttpError ->
                    showError("Error del servidor (${result.code})")

                is SearchResult.Unexpected ->
                    showError("Ocurrió un error inesperado.")
            }
        }
    }

    /** Vacia la lista y muestra el mensaje, que es lo que hacen los tres errores. */
    private fun showError(message: String) {
        _books.value = emptyList()
        _errorMessage.value = message
    }
}

package com.example.parcial1buscadordelibros.repository

import android.util.Log
import com.example.parcial1buscadordelibros.model.BookSearchResponse
import com.example.parcial1buscadordelibros.network.NetworkChecker
import com.example.parcial1buscadordelibros.network.OpenLibraryApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * Unica puerta de entrada a los datos de libros (patron Repository).
 *
 * Concentra dos cosas: chequear que haya red y hablar con la API. El ViewModel
 * lo llama y recibe un SearchResult ya interpretado, asi que no sabe que existe
 * Retrofit. Si mañana hubiera que agregar una cache o cambiar de API, se toca
 * solo este archivo.
 *
 * Recibe sus dependencias por constructor en vez de crearlas adentro, asi se lo
 * puede armar con un servicio falso para probarlo.
 */
class BookRepository(
    private val apiService: OpenLibraryApiService,
    private val networkChecker: NetworkChecker
) {

    /**
     * Busca libros y avisa el resultado por la funcion onResult.
     *
     * @param query titulo o autor que escribio el usuario.
     * @param onResult se ejecuta cuando termina, en el hilo principal.
     */
    fun searchBooks(query: String, onResult: (SearchResult) -> Unit) {
        // Si no hay red ni salimos a preguntar: cortamos aca.
        if (!networkChecker.isConnected()) {
            Log.w(TAG, "Sin red disponible, no se hace la llamada")
            onResult(SearchResult.NoConnection)
            return
        }

        // enqueue manda la llamada en segundo plano y avisa por los callbacks.
        // Con execute() se colgaria la pantalla.
        apiService.searchBooks(query).enqueue(object : Callback<BookSearchResponse> {

            override fun onResponse(
                call: Call<BookSearchResponse>,
                response: Response<BookSearchResponse>
            ) {
                // isSuccessful es true para cualquier codigo 2xx.
                if (response.isSuccessful) {
                    onResult(SearchResult.Success(response.body()?.docs ?: emptyList()))
                } else {
                    Log.e(TAG, "El servidor respondio ${response.code()}")
                    onResult(SearchResult.HttpError(response.code()))
                }
            }

            override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                Log.e(TAG, "Fallo la llamada a la API", t)

                // Una IOException casi siempre es que se corto la conexion o
                // vencio el timeout; cualquier otra cosa es un problema del JSON.
                onResult(
                    if (t is IOException) SearchResult.NoConnection else SearchResult.Unexpected
                )
            }
        })
    }

    companion object {
        private const val TAG = "BookRepository"
    }
}

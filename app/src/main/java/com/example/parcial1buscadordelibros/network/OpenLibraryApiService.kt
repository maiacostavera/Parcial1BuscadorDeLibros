package com.example.parcial1buscadordelibros.network

import com.example.parcial1buscadordelibros.model.BookSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoints de Open Library que usa la app.
 *
 * Retrofit toma esta interfaz y genera solo la implementacion que arma
 * la URL y hace la llamada HTTP.
 */
interface OpenLibraryApiService {

    /**
     * Busca libros por titulo o autor.
     *
     * @param query lo que escribio el usuario (ej "tolkien" o "el hobbit").
     * @param limit cuantos resultados pedir.
     * @param fields que campos queremos que devuelva. Sin esto la API no manda
     *               ni las paginas ni las editoriales ni los subjects.
     */
    @GET("search.json")
    fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("fields") fields: String = BOOK_FIELDS
    ): Call<BookSearchResponse>

    companion object {
        const val DEFAULT_LIMIT = 30

        // Los nombres van tal cual los espera la API, separados por coma y sin espacios.
        const val BOOK_FIELDS =
            "key,title,author_name,first_publish_year,cover_i," +
                "number_of_pages_median,publisher,subject"
    }
}

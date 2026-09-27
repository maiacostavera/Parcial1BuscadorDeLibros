package com.example.parcial1buscadordelibros.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Unico lugar donde se configura la conexion a la API.
 *
 * Es un object (singleton) para no crear un Retrofit nuevo en cada busqueda.
 * El "by lazy" hace que se construya recien la primera vez que se usa.
 */
object RetrofitClient {

    private const val BASE_URL = "https://openlibrary.org/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // GsonConverterFactory es el que convierte el JSON en nuestras data classes.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val openLibraryApiService: OpenLibraryApiService by lazy {
        retrofit.create(OpenLibraryApiService::class.java)
    }
}

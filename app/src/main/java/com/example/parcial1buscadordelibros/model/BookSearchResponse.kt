package com.example.parcial1buscadordelibros.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta completa de GET /search.json.
 *
 * El JSON trae mas cosas (start, numFoundExact, etc) pero Gson ignora sin
 * problema los campos que no declaramos aca.
 */
data class BookSearchResponse(

    /** Cuantos resultados encontro en total, no cuantos nos manda. */
    @SerializedName("numFound")
    val numFound: Int,

    /** La lista de libros de esta pagina de resultados. */
    @SerializedName("docs")
    val docs: List<Book>
)

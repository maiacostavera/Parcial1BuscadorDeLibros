package com.example.parcial1buscadordelibros.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Un libro devuelto por la Search API de Open Library.
 *
 * Casi todos los campos son nulables a proposito: la API no completa siempre
 * lo mismo para cada libro (hay ediciones viejas sin portada, sin paginas, etc).
 *
 * Implementa Serializable para poder viajar dentro del Intent hacia
 * DetailActivity y despues dentro del Bundle hacia BookDetailFragment.
 */
data class Book(
    // Viene con formato "/works/OL82563W", por eso se le puede pegar el dominio adelante.
    @SerializedName("key")
    val key: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("author_name")
    val authorNames: List<String>?,

    @SerializedName("first_publish_year")
    val firstPublishYear: Int?,

    // Id de la portada dentro del servidor de imagenes de Open Library.
    @SerializedName("cover_i")
    val coverId: Int?,

    // Mediana de paginas entre todas las ediciones del libro.
    @SerializedName("number_of_pages_median")
    val pageCount: Int?,

    @SerializedName("publisher")
    val publishers: List<String>?,

    @SerializedName("subject")
    val subjects: List<String>?
) : Serializable {

    /**
     * Arma la URL de la portada. Devuelve null si el libro no tiene imagen
     * cargada, asi el que llama sabe que tiene que mostrar el placeholder.
     *
     * @param size "S", "M" o "L" (chica, mediana o grande).
     */
    fun getCoverUrl(size: String): String? {
        if (coverId == null) return null
        return "$COVERS_BASE_URL/b/id/$coverId-$size.jpg"
    }

    /** Ficha del libro en la web de Open Library, para abrirla en el navegador. */
    fun getWebUrl(): String = "$SITE_BASE_URL$key"

    companion object {
        const val COVER_SIZE_MEDIUM = "M"
        const val COVER_SIZE_LARGE = "L"

        private const val COVERS_BASE_URL = "https://covers.openlibrary.org"
        private const val SITE_BASE_URL = "https://openlibrary.org"
    }
}

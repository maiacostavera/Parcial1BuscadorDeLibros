package com.example.parcial1buscadordelibros.repository

import com.example.parcial1buscadordelibros.model.Book

/**
 * Los cuatro finales posibles de una busqueda.
 *
 * Es el contrato que el repositorio le devuelve al ViewModel. Al ser una sealed
 * class, el compilador obliga a contemplar todos los casos en el when: si mañana
 * apareciera uno nuevo, no se puede olvidar de manejarlo.
 */
sealed class SearchResult {

    /** La API contesto bien. La lista puede venir vacia si no hubo coincidencias. */
    data class Success(val books: List<Book>) : SearchResult()

    /** No hay red, o se corto en el medio de la llamada. */
    object NoConnection : SearchResult()

    /** El servidor contesto con un codigo de error (404, 500, etc). */
    data class HttpError(val code: Int) : SearchResult()

    /** Cualquier otra cosa, normalmente un problema al leer el JSON. */
    object Unexpected : SearchResult()
}

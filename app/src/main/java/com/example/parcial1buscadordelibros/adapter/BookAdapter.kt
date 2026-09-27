package com.example.parcial1buscadordelibros.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parcial1buscadordelibros.R
import com.example.parcial1buscadordelibros.databinding.ItemBookBinding
import com.example.parcial1buscadordelibros.model.Book
import com.squareup.picasso.Picasso

/**
 * Conecta la lista de libros con el RecyclerView: agarra cada Book y lo pega
 * en una fila de item_book.xml.
 *
 * @param bookList lista a mostrar. Arranca vacia y se llena cuando el ViewModel responde.
 * @param onItemClick que hacer cuando tocan una fila. Lo decide la Activity, el
 *                    adapter solo avisa.
 */
class BookAdapter(
    private var bookList: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    /**
     * Sostiene las vistas de UNA fila. Guardamos el binding de item_book.xml en
     * vez de cada vista por separado, asi el RecyclerView recicla la fila sin
     * volver a buscarlas. binding.root es la MaterialCardView de afuera.
     */
    class BookViewHolder(val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root)

    /** Infla item_book.xml y lo mete en un ViewHolder. Se llama pocas veces. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun getItemCount() = bookList.size

    /** Llena la fila de esa posicion con los datos del libro. Se llama muchas veces. */
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        val context = holder.binding.root.context

        holder.binding.apply {
            tvBookTitle.text = book.title

            // joinToString junta todos los autores separados por coma.
            tvBookAuthors.text = book.authorNames
                ?.joinToString(", ")
                ?: context.getString(R.string.unknown_author)

            tvBookYear.text = book.firstPublishYear
                ?.let { context.getString(R.string.year_format, it) }
                ?: context.getString(R.string.unknown_year)

            // Muchos libros no tienen portada cargada, por eso el chequeo.
            val coverUrl = book.getCoverUrl(Book.COVER_SIZE_MEDIUM)
            if (coverUrl != null) {
                Picasso.get()
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(ivBookCover)
            } else {
                // Si no hay URL cortamos cualquier descarga anterior de esta fila
                // reciclada y dejamos el placeholder.
                Picasso.get().cancelRequest(ivBookCover)
                ivBookCover.setImageResource(R.drawable.ic_book_placeholder)
            }

            root.setOnClickListener { onItemClick(book) }
        }
    }

    /** Reemplaza la lista y redibuja, cuando el ViewModel publica resultados nuevos. */
    fun updateList(newList: List<Book>) {
        bookList = newList
        notifyDataSetChanged()
    }
}

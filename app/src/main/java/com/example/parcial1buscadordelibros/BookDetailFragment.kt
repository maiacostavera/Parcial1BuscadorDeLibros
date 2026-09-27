package com.example.parcial1buscadordelibros

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.parcial1buscadordelibros.databinding.FragmentBookDetailBinding
import com.example.parcial1buscadordelibros.model.Book
import com.squareup.picasso.Picasso

/**
 * Fragment obligatorio del trabajo: muestra la ficha completa de un libro
 * (portada grande, paginas, editoriales, temas y link a Open Library).
 *
 * No sabe de donde sale el libro ni hace llamadas a la API: lo recibe armado
 * en sus argumentos. Asi se podria reusar en cualquier otra pantalla.
 */
class BookDetailFragment : Fragment() {

    // El binding se crea en onCreateView y se tira en onDestroyView, porque la
    // vista del fragment puede morir antes que el fragment. Por eso es nullable.
    private var _binding: FragmentBookDetailBinding? = null

    // Atajo para no escribir _binding!! en todos lados. Solo valido mientras la
    // vista existe (entre onCreateView y onDestroyView).
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sacamos el libro de los argumentos que le paso DetailActivity.
        val book = arguments?.getSerializable(ARG_BOOK) as? Book ?: return

        showBook(book)
    }

    /** Vuelca los datos del libro en las vistas. */
    private fun showBook(book: Book) {
        binding.apply {
            tvDetailTitle.text = book.title

            tvDetailAuthors.text = book.authorNames
                ?.joinToString(", ")
                ?: getString(R.string.unknown_author)

            tvDetailYear.text = book.firstPublishYear
                ?.let { getString(R.string.year_format, it) }
                ?: getString(R.string.unknown_year)

            // Numero de paginas (mediana entre todas las ediciones).
            tvDetailPages.text = book.pageCount
                ?.toString()
                ?: getString(R.string.not_available)

            // Editoriales y temas pueden venir con cientos de items, por eso
            // mostramos solo los primeros para que la ficha siga siendo legible.
            tvDetailPublishers.text = formatList(book.publishers, MAX_PUBLISHERS)
            tvDetailSubjects.text = formatList(book.subjects, MAX_SUBJECTS)

            val coverUrl = book.getCoverUrl(Book.COVER_SIZE_LARGE)
            if (coverUrl != null) {
                Picasso.get()
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(ivDetailCover)
            } else {
                ivDetailCover.setImageResource(R.drawable.ic_book_placeholder)
            }

            btnOpenWeb.setOnClickListener { openInBrowser(book.getWebUrl()) }
            btnBack.setOnClickListener { requireActivity().finish() }
        }
    }

    /** Junta la lista con comas, recortandola si es muy larga. */
    private fun formatList(items: List<String>?, max: Int): String {
        if (items.isNullOrEmpty()) return getString(R.string.not_available)

        val shown = items.take(max).joinToString(", ")
        val remaining = items.size - max

        return if (remaining > 0) {
            getString(R.string.list_with_more_format, shown, remaining)
        } else {
            shown
        }
    }

    /** Abre la ficha del libro en el navegador del celular. */
    private fun openInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Por si el dispositivo no tiene ningun navegador instalado.
            Toast.makeText(requireContext(), R.string.error_no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para no dejar la vista colgada en memoria.
        _binding = null
    }

    companion object {
        private const val ARG_BOOK = "arg_book"

        private const val MAX_PUBLISHERS = 6
        private const val MAX_SUBJECTS = 10

        /**
         * Forma recomendada de crear un Fragment que necesita datos: nunca se le
         * pasan por constructor, porque al recrearlo el sistema usa el constructor
         * vacio y los perderia. Van en un Bundle de argumentos, que si sobrevive.
         */
        fun newInstance(book: Book): BookDetailFragment {
            return BookDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_BOOK, book)
                }
            }
        }
    }
}

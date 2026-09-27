package com.example.parcial1buscadordelibros

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.parcial1buscadordelibros.databinding.ActivityDetailBinding
import com.example.parcial1buscadordelibros.model.Book

/**
 * Pantalla 2: no dibuja nada por su cuenta, solo hace de contenedor del
 * BookDetailFragment, que es el que muestra la ficha del libro.
 *
 * Recibe el libro por Intent desde MainActivity y se lo pasa al fragment
 * por argumentos.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        // Recuperamos el libro que mando la pantalla anterior. getSerializableExtra
        // saca lo que se guardo con putExtra usando la MISMA clave, y 'as?' lo castea
        // a Book (si no puede, devuelve null en vez de romper).
        // En Android nuevo aparece tachado, pero funciona igual.
        val book = intent.getSerializableExtra(EXTRA_BOOK) as? Book

        // Si por algun motivo no llego nada, cerramos en vez de mostrar una ficha vacia.
        if (book == null) {
            finish()
            return
        }

        // Solo montamos el fragment la primera vez. Si la pantalla se recrea (al
        // rotar), el FragmentManager ya lo tiene guardado y lo vuelve a poner solo:
        // si no chequearamos esto, quedarian dos fragments encimados.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BookDetailFragment.newInstance(book))
                .commit()
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        /**
         * Clave del Intent. Va en el companion object para que el que guarda y el
         * que lee usen exactamente el mismo texto.
         */
        const val EXTRA_BOOK = "extra_book"
    }
}

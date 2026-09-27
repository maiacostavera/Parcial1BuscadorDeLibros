# Parcial 1 — Buscador de Libros

Aplicación Android en Kotlin que permite buscar libros por título o autor usando la
**Open Library Search API**, y ver la ficha completa de cada uno.

Primer examen de Desarrollo de Aplicaciones Android con Kotlin — **Tema 3**
Mailen Acosta Vera

## Video demostrativo

**[Ver video](https://drive.google.com/file/d/15Yn_ktc3khAcURp63zakOD_SGGW66jaj/view?usp=sharing)**

## La API

Se usa la Search API de Open Library, que es pública y gratuita (no hace falta API key):

```
GET https://openlibrary.org/search.json?q={busqueda}&limit=30&fields={campos}
```

El parámetro `fields` es importante: sin él la API no devuelve ni el número de
páginas, ni las editoriales, ni los temas. Los campos que pide la app son:

`key, title, author_name, first_publish_year, cover_i, number_of_pages_median, publisher, subject`

Las portadas salen del servidor de imágenes de Open Library, armando la URL con el
`cover_i` de cada libro:

```
https://covers.openlibrary.org/b/id/{cover_i}-M.jpg
```

## Pantallas

**1. Búsqueda y listado (`MainActivity`)**
Un campo de texto para buscar por título o autor y un `RecyclerView` con los
resultados. Cada fila muestra la portada, el título, los autores y el año de
primera publicación.

**2. Detalle (`DetailActivity` + `BookDetailFragment`)**
Al tocar un libro se abre la segunda Activity, que hospeda el Fragment con la ficha:
portada en grande, número de páginas, editoriales, lista de temas y un botón que
abre el libro en la web de Open Library.

## Arquitectura

La app sigue **MVVM (Model - View - ViewModel)** con **Repository Pattern**:

```
app/src/main/java/com/example/parcial1buscadordelibros/
├── model/
│   ├── Book.kt                    Modelo de un libro (Serializable)
│   └── BookSearchResponse.kt      Respuesta de /search.json
├── network/
│   ├── OpenLibraryApiService.kt   Endpoints de Retrofit
│   ├── RetrofitClient.kt          Configuración de la conexión (singleton)
│   └── NetworkChecker.kt          Consulta si hay red disponible
├── repository/
│   ├── BookRepository.kt          Única puerta de entrada a los datos
│   └── SearchResult.kt            Los 4 resultados posibles de una búsqueda
├── adapter/
│   └── BookAdapter.kt             Adapter del RecyclerView + su ViewHolder
├── viewmodel/
│   ├── BookViewModel.kt           Estados de la pantalla
│   └── BookViewModelFactory.kt    Arma el ViewModel con su repositorio
├── MainActivity.kt                Pantalla 1: buscador y lista
├── DetailActivity.kt              Pantalla 2: contenedor del Fragment
└── BookDetailFragment.kt          Fragment con la ficha del libro
```

El flujo de una búsqueda es siempre el mismo:

```
MainActivity → BookViewModel → BookRepository → NetworkChecker + Retrofit
                     ↑                                    │
                     └────────── SearchResult ────────────┘
```

- **Model**: las data classes. Solo datos, sin lógica.
- **View** (Activities, Fragment y Adapter): muestran datos y escuchan toques.
  No saben que existe Retrofit.
- **ViewModel**: traduce lo que devuelve el repositorio a los tres `LiveData` que
  la pantalla observa (`books`, `isLoading` y `errorMessage`). Como sobrevive a la
  rotación, los resultados no se pierden al girar el celular.
- **Repository**: el único que habla con la red. Chequea la conexión, llama a la
  API e interpreta la respuesta.

La comunicación va en un solo sentido: la Activity le pide una búsqueda al
ViewModel y después se limita a observar los `LiveData`. Cuando cambian, los
observers redibujan la pantalla solos.

### Repository Pattern

`BookRepository` concentra todo el acceso a datos y devuelve un `SearchResult`, que
es una `sealed class` con los cuatro finales posibles: `Success`, `NoConnection`,
`HttpError` y `Unexpected`. Gracias a eso el ViewModel nunca ve un `Response` ni un
`Callback` de Retrofit, y el `when` que los procesa está obligado por el compilador
a cubrir los cuatro casos.

Si mañana hubiera que cambiar de API o agregar una caché, se toca un solo archivo.

### Inyección de dependencias

Ni el ViewModel ni el repositorio crean sus propias dependencias: las reciben por
constructor. `MainActivity` es la que arma la cadena y la inyecta a través de
`BookViewModelFactory`:

```kotlin
private val viewModel: BookViewModel by viewModels {
    BookViewModelFactory(
        repository = BookRepository(
            apiService = RetrofitClient.openLibraryApiService,
            networkChecker = NetworkChecker(applicationContext)
        )
    )
}
```

La fábrica hace falta porque `BookViewModel` no tiene constructor vacío, así que
Android no sabe cómo construirlo solo. La ventaja de hacerlo así es que para
testear se le podría pasar un repositorio falso sin tocar el ViewModel.

### Paso de datos entre pantallas

`Book` implementa `Serializable`, así que el libro seleccionado viaja:

1. de `MainActivity` a `DetailActivity` dentro del **Intent** (`putExtra`), y
2. de `DetailActivity` al `BookDetailFragment` dentro del **Bundle de argumentos**,
   usando el patrón `newInstance()`.

Los argumentos no se pasan por constructor a propósito: cuando el sistema recrea un
Fragment usa el constructor vacío, así que se perderían. En el Bundle sobreviven.

## Manejo de errores

| Situación | Qué hace la app |
|---|---|
| Buscando | Muestra un `ProgressBar` y deshabilita el botón Buscar |
| Sin red (modo avión, sin datos) | `NetworkChecker` corta antes de llamar y avisa al instante |
| Se corta la conexión en el medio | `IOException` en `onFailure` → mismo mensaje |
| Error HTTP (404, 500, etc.) | Mensaje con el código que devolvió el servidor |
| Búsqueda sin resultados | Mensaje "No encontramos libros con esa búsqueda" |
| Campo de búsqueda vacío | Toast pidiendo que escriba algo |
| Libro sin portada / sin datos | Imagen de reemplazo y "No disponible" |

Hay dos controles de red a propósito: `NetworkChecker` evita salir a la API cuando
ya se sabe que no hay conexión (el usuario ve el aviso al toque, sin esperar el
timeout), y la `IOException` cubre el caso de que la red se caiga en el medio de
la llamada.

Todos los campos del modelo menos `key` y `title` son nulables, porque la API no
completa la misma información para todos los libros.

## Tecnologías

- Kotlin
- **Retrofit 3.0.0** + **Gson** para consumir la API
- **Picasso 2.8** para descargar y mostrar las portadas
- **ViewModel + LiveData** (androidx.lifecycle)
- **RecyclerView** con Adapter y ViewHolder propios
- **View Binding** (sin `findViewById` en ningún lado)
- **Material 3** para los componentes y el tema (con modo oscuro)

## Cómo correr el proyecto

1. Abrir la carpeta con Android Studio y esperar a que termine el Gradle Sync.
2. Elegir un emulador o un dispositivo con Android 7.0 (API 24) o superior.
3. Darle a Run.

Hace falta conexión a internet, tanto para el sync de Gradle como para que la app
pueda consultar la API.

- `minSdk` 24
- `targetSdk` / `compileSdk` 36

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.parcial1buscadordelibros"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.parcial1buscadordelibros"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // View Binding: por cada layout XML se genera una clase Binding con las vistas
    // que tienen id. Es lo que usamos en vez de findViewById.
    buildFeatures {
        viewBinding = true
    }

    lint {
        // Falso positivo: Picasso incluye una clase para mostrar imagenes en
        // notificaciones que la app nunca usa, y lint pide el permiso igual.
        disable += "NotificationPermission"
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material)

    // MVVM: ViewModel + LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Lista de resultados
    implementation(libs.androidx.recyclerview)

    // Retrofit para consumir la API + Gson para pasar el JSON a data classes
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Picasso para bajar y mostrar las portadas
    implementation("com.squareup.picasso:picasso:2.8")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

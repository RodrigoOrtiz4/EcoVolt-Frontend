package com.ecovolt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ecovolt.app.data.local.SharedPrefsConsejoStorage
import com.ecovolt.app.data.local.SharedPrefsLecturaStorage
import com.ecovolt.app.data.local.SharedPrefsSesionStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Deben crearse aquí, antes de que la actividad arranque, porque registran el resultado de la cámara
        val lector = AndroidLectorMedidor(this)
        val selectorFoto = AndroidSelectorFoto(this)   // fotos de los reportes (cámara o galería)

        setContent {
            App(
                storage = SharedPrefsLecturaStorage(applicationContext),
                sesionStorage = SharedPrefsSesionStorage(applicationContext),
                lector = lector,
                selectorFoto = selectorFoto,
                consejoStorage = SharedPrefsConsejoStorage(applicationContext)
            )
        }
    }
}

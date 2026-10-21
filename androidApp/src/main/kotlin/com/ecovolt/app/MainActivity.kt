package com.ecovolt.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ecovolt.app.data.local.SharedPrefsLecturaStorage
import com.ecovolt.app.data.local.SharedPrefsSesionStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Debe crearse aquí, antes de que la actividad arranque, porque registra el resultado de la cámara
        val lector = AndroidLectorMedidor(this)

        setContent {
            App(
                storage = SharedPrefsLecturaStorage(applicationContext),
                sesionStorage = SharedPrefsSesionStorage(applicationContext),
                lector = lector
            )
        }
    }
}

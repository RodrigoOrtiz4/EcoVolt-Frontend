package com.ecovolt.app.navigation

// Pantallas de la app. Para agregar una nueva solo se suma un objeto aquí.
sealed interface Screen {
    data object Login : Screen
    data object Registro : Screen
    data object Inicio : Screen
    data object Lecturas : Screen
    data object Captura : Screen   // leer el medidor con la cámara (OCR)
}

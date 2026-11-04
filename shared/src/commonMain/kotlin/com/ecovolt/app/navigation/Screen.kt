package com.ecovolt.app.navigation

// Pantallas de la app. Para agregar una nueva solo se suma un objeto aquí.
sealed interface Screen {
    data object Login : Screen
    data object Registro : Screen
    data object Inicio : Screen
    data object Lecturas : Screen
    data object Captura : Screen   // leer el medidor con la cámara (OCR)
    data object Reportes : Screen      // RFN-19: lista de reportes de desperdicio o falla
    data object NuevoReporte : Screen  // RFN-19: formulario con foto, área y descripción
    data object Consejos : Screen      // RFN-25: consejos de ahorro para escuela y hogar
}

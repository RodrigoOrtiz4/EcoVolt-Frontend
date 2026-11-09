package com.ecovolt.app.config

object AppConfig {
    // Dirección del backend de EcoVolt (proyecto ecovolt-backend).
    //  - Emulador de Android: 10.0.2.2 es la computadora donde corre el emulador.
    //  - Celular físico: usa la IP de tu computadora en la red Wi-Fi, por ejemplo http://192.168.1.50:3000
    //    (el backend la muestra al arrancar). Celular y computadora deben estar en la misma red.
    const val BASE_URL = "https://ecovolt-backend-2jbr.onrender.com"

    const val LECTURAS_PATH = "/lecturas"
    const val LOGIN_PATH = "/auth/login"
    const val REGISTER_PATH = "/auth/register"
    const val CAMBIAR_CONTRASENA_PATH = "/auth/cambiar-contrasena"
    const val REPORTES_PATH = "/reportes"      // RFN-19
    const val CONSEJOS_PATH = "/consejos"      // RFN-25
    const val TIMEOUT_MS = 15_000L
    const val TIMEOUT_SUBIDA_MS = 45_000L      // para enviar reportes con foto
    const val STORAGE_NAME = "ecovolt_storage"
}

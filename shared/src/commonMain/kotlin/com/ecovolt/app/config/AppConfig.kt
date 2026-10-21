package com.ecovolt.app.config

object AppConfig {
    // Dirección del backend de EcoVolt (proyecto ecovolt-backend).
    //  - Emulador de Android: 10.0.2.2 es la computadora donde corre el emulador.
    //  - Celular físico: usa la IP de tu computadora en la red Wi-Fi, por ejemplo http://192.168.1.50:3000
    //    (el backend la muestra al arrancar). Celular y computadora deben estar en la misma red.
    const val BASE_URL = "http://10.0.2.2:3000"

    const val LECTURAS_PATH = "/lecturas"
    const val LOGIN_PATH = "/auth/login"
    const val REGISTER_PATH = "/auth/register"
    const val TIMEOUT_MS = 15_000L
    const val STORAGE_NAME = "ecovolt_storage"
}

package com.ecovolt.app.service

// Contrato para compartir un archivo de texto (el CSV del historial) con otras apps:
// correo, WhatsApp, Drive... Cómo se hace depende de la plataforma (androidApp/AndroidCompartidor).
interface Compartidor {
    // Devuelve null si se abrió el menú para compartir, o el mensaje de error
    fun compartirArchivo(nombreArchivo: String, tipoMime: String, contenido: String): String?
}

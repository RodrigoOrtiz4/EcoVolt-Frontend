package com.ecovolt.app.service

// Contrato para leer el medidor con la cámara. El código compartido solo conoce esto;
// cómo se abre la cámara y se hace el OCR depende de la plataforma (androidApp).
interface LectorMedidor {
    // Abre la cámara, toma la foto, reconoce el texto y avisa el resultado
    fun capturar(alTerminar: (ResultadoCaptura) -> Unit)
}

sealed interface ResultadoCaptura {
    data class Texto(val texto: String) : ResultadoCaptura
    data object Cancelado : ResultadoCaptura
    data class Error(val mensaje: String) : ResultadoCaptura
}

package com.ecovolt.app.service

// Contrato para leer el medidor con la cámara. El código compartido solo conoce esto;
// cómo se abre la cámara y se hace el OCR depende de la plataforma (androidApp).
interface LectorMedidor {
    // Abre la cámara, toma la foto, reconoce el texto y avisa el resultado
    fun capturar(alTerminar: (ResultadoCaptura) -> Unit)
}

sealed interface ResultadoCaptura {
    // `foto` es una copia pequeña (JPG) de la foto tomada, para mostrarla al confirmar el número.
    // Es `class` y no `data class` porque un ByteArray no se compara bien con equals().
    class Texto(val texto: String, val foto: ByteArray? = null) : ResultadoCaptura
    data object Cancelado : ResultadoCaptura
    data class Error(val mensaje: String) : ResultadoCaptura
}

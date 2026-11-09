package com.ecovolt.app.service

// Contrato para conseguir la foto de un reporte. El código compartido solo conoce esto;
// cómo se abre la cámara o la galería depende de la plataforma (androidApp/AndroidSelectorFoto).
// Es la misma idea que LectorMedidor, pero aquí se quiere la imagen, no el texto.
interface SelectorFoto {
    fun tomarFoto(alTerminar: (ResultadoFoto) -> Unit)
    fun elegirDeGaleria(alTerminar: (ResultadoFoto) -> Unit)
}

// Foto lista para enviar: la plataforma ya la redujo y la comprimió en JPG.
// Es `class` y no `data class` porque un ByteArray no se compara bien con equals().
class Foto(val bytes: ByteArray)

sealed interface ResultadoFoto {
    data class Obtenida(val foto: Foto) : ResultadoFoto
    data object Cancelada : ResultadoFoto
    data class Error(val mensaje: String) : ResultadoFoto
}

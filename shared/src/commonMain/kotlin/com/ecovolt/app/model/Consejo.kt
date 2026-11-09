package com.ecovolt.app.model

// Dónde aplica el consejo (RFN-25)
enum class AmbitoConsejo(val etiqueta: String, val icono: String) {
    ESCUELA("Escuela", "🏫"),
    HOGAR("Hogar", "🏠")
}

data class Consejo(
    val id: Int,
    val ambito: AmbitoConsejo,
    val categoria: String,     // "Iluminación", "Equipos", ...
    val icono: String,         // emoji de la tarjeta
    val titulo: String,
    val texto: String
)

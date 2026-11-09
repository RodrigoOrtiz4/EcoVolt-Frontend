package com.ecovolt.app.data.remote

import com.ecovolt.app.model.AmbitoConsejo
import com.ecovolt.app.model.Consejo
import kotlinx.serialization.Serializable

// Formato del JSON de GET /consejos. También se usa para guardarlos en el teléfono.
@Serializable
data class ConsejoDto(
    val id: Int,
    val ambito: String,               // "escuela" o "hogar"
    val categoria: String,
    val icono: String,
    val titulo: String,
    val texto: String
)

fun ConsejoDto.toModel() = Consejo(
    id = id,
    ambito = if (ambito == "hogar") AmbitoConsejo.HOGAR else AmbitoConsejo.ESCUELA,
    categoria = categoria,
    icono = icono,
    titulo = titulo,
    texto = texto
)

fun Consejo.toDto() = ConsejoDto(
    id = id,
    ambito = if (ambito == AmbitoConsejo.HOGAR) "hogar" else "escuela",
    categoria = categoria,
    icono = icono,
    titulo = titulo,
    texto = texto
)

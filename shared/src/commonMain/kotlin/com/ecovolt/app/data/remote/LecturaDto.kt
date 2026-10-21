package com.ecovolt.app.data.remote

import com.ecovolt.app.model.Lectura
import com.ecovolt.app.model.MomentoLectura
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
// Formato del JSON que responde la API. Se separa del modelo para que
// un cambio en la API no afecte al resto de la app.
@Serializable
data class LecturaDto(
    val id: Int,
    val fecha: String,
    val momento: String,                 // "viernes" o "lunes"
    @SerialName("valor_kwh") val valorKwh: Double
)
fun LecturaDto.toModel() = Lectura(
    id = id,
    fecha = fecha,
    momento = if (momento == "lunes") MomentoLectura.LUNES_INICIO else MomentoLectura.VIERNES_CIERRE,
    valorKwh = valorKwh
)

// Lo que se envía en POST /lecturas al guardar una lectura nueva. No lleva id: lo asigna el backend.
@Serializable
data class NuevaLecturaDto(
    val fecha: String,
    val momento: String,                 // "viernes" o "lunes"
    @SerialName("valor_kwh") val valorKwh: Double
)

fun MomentoLectura.aTextoApi(): String = if (this == MomentoLectura.LUNES_INICIO) "lunes" else "viernes"

fun Lectura.toDto() = LecturaDto(
    id = id,
    fecha = fecha,
    momento = if (momento == MomentoLectura.LUNES_INICIO) "lunes" else "viernes",
    valorKwh = valorKwh
)
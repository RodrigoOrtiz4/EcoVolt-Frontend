package com.ecovolt.app.data.remote

import com.ecovolt.app.model.Lectura
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.model.OrigenLectura
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Formato del JSON que responde la API. Se separa del modelo para que
// un cambio en la API no afecte al resto de la app.
// hora, origen y nota son opcionales: las lecturas antiguas (y las guardadas en el teléfono
// por la versión anterior de la app) no los tienen.
@Serializable
data class LecturaDto(
    val id: Int,
    val fecha: String,
    val momento: String,                 // "viernes" o "lunes"
    @SerialName("valor_kwh") val valorKwh: Double,
    val hora: String? = null,
    val origen: String? = null,          // "ocr" o "manual"
    val nota: String? = null
)

fun LecturaDto.toModel() = Lectura(
    id = id,
    fecha = fecha,
    momento = if (momento == "lunes") MomentoLectura.LUNES_INICIO else MomentoLectura.VIERNES_CIERRE,
    valorKwh = valorKwh,
    hora = hora,
    origen = when (origen) {
        "ocr" -> OrigenLectura.OCR
        "manual" -> OrigenLectura.MANUAL
        else -> null
    },
    nota = nota
)

// Lo que se envía en POST /lecturas al guardar una lectura nueva. No lleva id ni hora: los pone el backend.
@Serializable
data class NuevaLecturaDto(
    val fecha: String,
    val momento: String,                 // "viernes" o "lunes"
    @SerialName("valor_kwh") val valorKwh: Double,
    val origen: String? = null,
    val nota: String? = null
)

fun MomentoLectura.aTextoApi(): String = if (this == MomentoLectura.LUNES_INICIO) "lunes" else "viernes"

fun OrigenLectura.aTextoApi(): String = if (this == OrigenLectura.OCR) "ocr" else "manual"

fun Lectura.toDto() = LecturaDto(
    id = id,
    fecha = fecha,
    momento = momento.aTextoApi(),
    valorKwh = valorKwh,
    hora = hora,
    origen = origen?.aTextoApi(),
    nota = nota
)

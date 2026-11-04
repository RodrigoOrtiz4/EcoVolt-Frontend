package com.ecovolt.app.data.remote

import com.ecovolt.app.model.Reporte
import com.ecovolt.app.model.TipoReporte
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Formato del JSON de /reportes. Igual que con las lecturas, se separa del modelo
// para que un cambio en la API no afecte al resto de la app.
@Serializable
data class ReporteDto(
    val id: Int,
    val tipo: String,                 // "desperdicio" o "falla"
    val area: String,
    val descripcion: String,
    val fecha: String,
    val autor: String
)

fun ReporteDto.toModel() = Reporte(
    id = id,
    tipo = if (tipo == "falla") TipoReporte.FALLA else TipoReporte.DESPERDICIO,
    area = area,
    descripcion = descripcion,
    fecha = fecha,
    autor = autor
)

// Lo que se envía en POST /reportes. La foto va como texto Base64 dentro del mismo JSON.
// No lleva id, fecha ni autor: los pone el backend.
@Serializable
data class NuevoReporteDto(
    val tipo: String,
    val area: String,
    val descripcion: String,
    @SerialName("foto_base64") val fotoBase64: String
)

fun TipoReporte.aTextoApi(): String = if (this == TipoReporte.FALLA) "falla" else "desperdicio"

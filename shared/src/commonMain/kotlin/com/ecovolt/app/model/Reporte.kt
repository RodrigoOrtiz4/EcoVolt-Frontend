package com.ecovolt.app.model

// Tipo de reporte (RFN-19). Cada uno trae el texto, el ícono y una ayuda para la pantalla.
enum class TipoReporte(val etiqueta: String, val icono: String, val ayuda: String) {
    DESPERDICIO("Desperdicio", "⚠️", "Luces, ventiladores o equipos encendidos sin que nadie los use."),
    FALLA("Falla", "🔧", "Algo descompuesto: contacto quemado, lámpara fundida, cable suelto...")
}

// Reporte ya guardado en el servidor. La foto no viene aquí: se descarga aparte cuando se quiere ver.
data class Reporte(
    val id: Int,
    val tipo: TipoReporte,
    val area: String,          // "Salón 3B"
    val descripcion: String,
    val fecha: String,         // "2026-10-28 16:45"
    val autor: String          // nombre de quien lo envió
)

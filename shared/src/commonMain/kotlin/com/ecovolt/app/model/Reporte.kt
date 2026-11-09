package com.ecovolt.app.model

// Tipo de reporte (RFN-19). Cada uno trae el texto y una ayuda para la pantalla.
enum class TipoReporte(val etiqueta: String, val ayuda: String) {
    DESPERDICIO("Desperdicio", "Luces, ventiladores o equipos encendidos sin que nadie los use."),
    FALLA("Falla", "Algo descompuesto: contacto quemado, lámpara fundida, cable suelto...")
}

// En qué va la atención del reporte. Dirección lo actualiza.
enum class EstatusReporte(val etiqueta: String, val textoApi: String) {
    PENDIENTE("Pendiente", "pendiente"),
    EN_PROCESO("En proceso", "en_proceso"),
    RESUELTO("Resuelto", "resuelto");

    companion object {
        fun deTexto(texto: String?): EstatusReporte = entries.firstOrNull { it.textoApi == texto } ?: PENDIENTE
    }
}

// Reporte ya guardado en el servidor. La foto no viene aquí: se descarga aparte cuando se quiere ver.
data class Reporte(
    val id: Int,
    val tipo: TipoReporte,
    val area: String,          // "Salón 3B"
    val descripcion: String,
    val fecha: String,         // "2026-10-28 16:45"
    val autor: String,         // nombre de quien lo envió
    val estatus: EstatusReporte = EstatusReporte.PENDIENTE
) {
    // Folio que se le muestra a quien reporta: ECO-042
    val folio: String get() = "ECO-" + id.toString().padStart(3, '0')
}

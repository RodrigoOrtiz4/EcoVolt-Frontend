package com.ecovolt.app.model

// Viernes al cerrar la escuela (cierre) y lunes al abrir (apertura).
// La resta lunes - viernes es el consumo del fin de semana.
enum class MomentoLectura(val etiqueta: String, val dia: String, val horario: String) {
    VIERNES_CIERRE("Cierre", "Viernes", "13:00–15:00 h"),
    LUNES_INICIO("Apertura", "Lunes", "7:30–9:00 h")
}

// Cómo se obtuvo el número
enum class OrigenLectura(val etiqueta: String) {
    OCR("Foto + OCR"),
    MANUAL("Captura manual")
}

data class Lectura(
    val id: Int,
    val fecha: String,            // ejemplo: "2026-09-25"
    val momento: MomentoLectura,
    val valorKwh: Double,
    val hora: String? = null,     // "13:18"; las lecturas antiguas no la tienen
    val origen: OrigenLectura? = null,
    val nota: String? = null      // explicación si se cambió o reinició el medidor
)

// Orden cronológico: por fecha y, si son del mismo día, en el orden en que se guardaron
fun List<Lectura>.cronologicas(): List<Lectura> = sortedWith(compareBy({ it.fecha }, { it.id }))

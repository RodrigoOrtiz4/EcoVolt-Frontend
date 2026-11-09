package com.ecovolt.app.model

import kotlinx.serialization.Serializable

// Roles de EcoVolt. Cada rol trae el texto que se muestra en pantalla.
@Serializable
enum class Rol(val etiqueta: String, val descripcion: String) {
    DIRECTOR("Director", "Consulta el consumo de la escuela y atiende los reportes."),
    DOCENTE("Docente", "Reporta desperdicios y comparte consejos de ahorro con tu grupo."),
    ALUMNO("Alumno", "Reporta desperdicios y aprende a ahorrar energía en la escuela y en casa."),
    RESPONSABLE("Responsable de lecturas", "Captura la lectura del medidor el viernes y el lunes.");

    // Director y responsable capturan lecturas y atienden reportes
    val capturaLecturas: Boolean get() = this == DIRECTOR || this == RESPONSABLE
    val atiendeReportes: Boolean get() = this == DIRECTOR || this == RESPONSABLE
}

package com.ecovolt.app.service

// La fecha y la hora de hoy dependen de la plataforma.
// fechaHoy() devuelve el formato 2026-10-13 y horaActual() el formato 13:18.
expect fun fechaHoy(): String

expect fun horaActual(): String

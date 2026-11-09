package com.ecovolt.app.service

// Cálculos con fechas "AAAA-MM-DD" sin librerías externas.
// Internamente cada fecha se convierte en un número de día (días desde el 1 de enero de 1970),
// así sumar días o saber qué día de la semana es se vuelve una simple cuenta.
object Fechas {

    private val DIAS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    private val MESES = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

    const val LUNES = 1
    const val VIERNES = 5

    // "2026-09-25" → número de día, o null si el texto no es una fecha
    fun aDia(fecha: String): Long? {
        val partes = fecha.trim().take(10).split('-')
        if (partes.size != 3) return null
        val anio = partes[0].toIntOrNull() ?: return null
        val mes = partes[1].toIntOrNull() ?: return null
        val dia = partes[2].toIntOrNull() ?: return null
        if (mes !in 1..12 || dia !in 1..31) return null
        return diasDesdeCivil(anio, mes, dia)
    }

    // Número de día → "2026-09-25"
    fun deDia(numero: Long): String {
        val (anio, mes, dia) = civilDesdeDias(numero)
        return "$anio-${mes.toString().padStart(2, '0')}-${dia.toString().padStart(2, '0')}"
    }

    // 1 = lunes ... 7 = domingo (el 1 de enero de 1970 fue jueves)
    fun diaSemana(numero: Long): Int = moduloPositivo(numero + 3, 7) + 1

    fun diaSemana(fecha: String): Int? = aDia(fecha)?.let { diaSemana(it) }

    fun sumarDias(fecha: String, dias: Int): String? = aDia(fecha)?.let { deDia(it + dias) }

    // "Viernes 25 sep"
    fun corta(fecha: String): String {
        val numero = aDia(fecha) ?: return fecha
        val (_, mes, dia) = civilDesdeDias(numero)
        return "${DIAS[diaSemana(numero) - 1]} $dia ${MESES[mes - 1]}"
    }

    // "25 sep"
    fun diaMes(fecha: String): String {
        val numero = aDia(fecha) ?: return fecha
        val (_, mes, dia) = civilDesdeDias(numero)
        return "$dia ${MESES[mes - 1]}"
    }

    // "25"
    fun soloDia(fecha: String): String {
        val numero = aDia(fecha) ?: return fecha
        return civilDesdeDias(numero).third.toString()
    }

    // Algoritmo de Howard Hinnant (days_from_civil), válido para cualquier fecha del calendario gregoriano
    private fun diasDesdeCivil(anioOriginal: Int, mes: Int, dia: Int): Long {
        val anio = if (mes <= 2) anioOriginal - 1 else anioOriginal
        val era = (if (anio >= 0) anio else anio - 399) / 400
        val anioDeEra = anio - era * 400
        val mesDesdeMarzo = (mes + 9) % 12
        val diaDelAnio = (153 * mesDesdeMarzo + 2) / 5 + dia - 1
        val diaDeEra = anioDeEra * 365 + anioDeEra / 4 - anioDeEra / 100 + diaDelAnio
        return era * 146097L + diaDeEra - 719468
    }

    private fun civilDesdeDias(numero: Long): Triple<Int, Int, Int> {
        val z = numero + 719468
        val era = (if (z >= 0) z else z - 146096) / 146097
        val diaDeEra = z - era * 146097
        val anioDeEra = (diaDeEra - diaDeEra / 1460 + diaDeEra / 36524 - diaDeEra / 146096) / 365
        val diaDelAnio = diaDeEra - (365 * anioDeEra + anioDeEra / 4 - anioDeEra / 100)
        val mesDesdeMarzo = (5 * diaDelAnio + 2) / 153
        val dia = diaDelAnio - (153 * mesDesdeMarzo + 2) / 5 + 1
        val mes = if (mesDesdeMarzo < 10) mesDesdeMarzo + 3 else mesDesdeMarzo - 9
        val anio = anioDeEra + era * 400 + if (mes <= 2) 1 else 0
        return Triple(anio.toInt(), mes.toInt(), dia.toInt())
    }
}

// Residuo siempre positivo (para fechas anteriores a 1970 el % de Kotlin da negativo)
private fun moduloPositivo(a: Long, b: Int): Int {
    val r = (a % b).toInt()
    return if (r < 0) r + b else r
}

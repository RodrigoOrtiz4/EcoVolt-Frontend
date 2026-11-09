package com.ecovolt.app.service

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

// Números con separador de miles y punto decimal, como en el diseño: 8,742.6
// Con conSigno = true los positivos llevan "+" (diferencias: "+128.4").
fun formatoNumero(valor: Double, decimales: Int = 1, conSigno: Boolean = false): String {
    if (valor.isNaN() || valor.isInfinite()) return "—"
    val factor = 10.0.pow(decimales).toLong()
    val escalado = (abs(valor) * factor).roundToLong()
    val entero = escalado / factor
    val fraccion = escalado % factor
    val enteroTexto = entero.toString().reversed().chunked(3).joinToString(",").reversed()
    val signo = when {
        valor < 0 && escalado != 0L -> "-"
        conSigno -> "+"
        else -> ""
    }
    val parteDecimal = if (decimales > 0) "." + fraccion.toString().padStart(decimales, '0') else ""
    return signo + enteroTexto + parteDecimal
}

// "8,742.6 kWh"
fun formatoKwh(valor: Double, conSigno: Boolean = false): String = formatoNumero(valor, 1, conSigno) + " kWh"

// Convierte lo que escribe la persona en número. Acepta "8,742.6" (coma de miles, como se ve en
// pantalla), "8742.6" y también "8742,6" (coma decimal). Devuelve null si no es un número.
fun numeroDeTexto(texto: String): Double? {
    val limpio = texto.trim().replace(" ", "")
    if (limpio.isEmpty()) return null
    val comas = limpio.count { it == ',' }
    val normalizado = when {
        limpio.contains('.') -> limpio.replace(",", "")                                   // 8,742.6
        comas == 1 && limpio.substringAfter(',').length != 3 -> limpio.replace(',', '.')  // 8742,6
        else -> limpio.replace(",", "")                                                   // 8,742
    }
    if (normalizado.count { it == '.' } > 1) return null
    return normalizado.toDoubleOrNull()?.takeIf { !it.isNaN() && !it.isInfinite() }
}

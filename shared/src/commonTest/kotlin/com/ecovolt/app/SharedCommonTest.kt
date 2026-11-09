package com.ecovolt.app

import com.ecovolt.app.model.Lectura
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.model.cronologicas
import com.ecovolt.app.service.CicloSemanal
import com.ecovolt.app.service.Fechas
import com.ecovolt.app.service.formatoNumero
import com.ecovolt.app.service.numeroDeTexto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Pruebas de la lógica que no depende de la pantalla: formatos, fechas y ciclo semanal.
// Se ejecutan con: ./gradlew :shared:testAndroidHostTest
class SharedCommonTest {

    private fun lectura(id: Int, fecha: String, momento: MomentoLectura) = Lectura(id, fecha, momento, 1000.0 + id)

    private val historial = listOf(
        lectura(1, "2026-09-18", MomentoLectura.VIERNES_CIERRE),
        lectura(2, "2026-09-21", MomentoLectura.LUNES_INICIO),
        lectura(3, "2026-09-25", MomentoLectura.VIERNES_CIERRE)
    )

    @Test
    fun formatoDeNumeros() {
        assertEquals("8,742.6", formatoNumero(8742.6))
        assertEquals("+128.4", formatoNumero(128.4, conSigno = true))
        assertEquals("-100.0", formatoNumero(-100.0))
        assertEquals("535", formatoNumero(535.0, 0))
    }

    @Test
    fun leerNumerosEscritos() {
        assertEquals(8742.6, numeroDeTexto("8,742.6"))
        assertEquals(8742.6, numeroDeTexto("8742,6"))
        assertEquals(8742.0, numeroDeTexto("8,742"))
        assertNull(numeroDeTexto("1.2.3"))
        assertNull(numeroDeTexto(""))
    }

    @Test
    fun fechasEnEspanol() {
        assertEquals("Viernes 25 sep", Fechas.corta("2026-09-25"))
        assertEquals("Lunes 28 sep", Fechas.corta("2026-09-28"))
        assertEquals("2027-01-02", Fechas.sumarDias("2026-12-30", 3))
        assertEquals("2024-02-29", Fechas.sumarDias("2024-03-01", -1))
    }

    @Test
    fun lunesConCierreHechoPideApertura() {
        val ciclo = CicloSemanal.calcular(historial, "2026-09-28")!!
        assertEquals(MomentoLectura.LUNES_INICIO, ciclo.momento)
        assertTrue(ciclo.esHoy)
        assertEquals(0.5f, ciclo.progreso)
    }

    @Test
    fun viernesSinCierrePideCierre() {
        val ciclo = CicloSemanal.calcular(historial.dropLast(1), "2026-09-25")!!
        assertEquals(MomentoLectura.VIERNES_CIERRE, ciclo.momento)
        assertTrue(ciclo.esHoy)
        assertFalse(ciclo.atrasada)
    }

    @Test
    fun cicloCompletoApuntaAlSiguienteViernes() {
        val ciclo = CicloSemanal.calcular(historial + lectura(4, "2026-09-28", MomentoLectura.LUNES_INICIO), "2026-09-30")!!
        assertTrue(ciclo.completo)
        assertEquals("2026-10-02", ciclo.fecha)
    }

    @Test
    fun ordenCronologico() {
        val desordenadas = listOf(
            lectura(5, "2026-01-02", MomentoLectura.LUNES_INICIO),
            lectura(2, "2026-01-02", MomentoLectura.LUNES_INICIO),
            lectura(9, "2025-12-30", MomentoLectura.LUNES_INICIO)
        )
        assertEquals(listOf(9, 2, 5), desordenadas.cronologicas().map { it.id })
    }
}

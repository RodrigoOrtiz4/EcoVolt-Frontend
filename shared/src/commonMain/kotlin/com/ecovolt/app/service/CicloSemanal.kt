package com.ecovolt.app.service

import com.ecovolt.app.model.Lectura
import com.ecovolt.app.model.MomentoLectura

// Estado del ciclo semanal de lecturas: cierre el viernes y apertura el lunes.
// Con él la pantalla de inicio sabe qué lectura toca, para cuándo y si ya se atrasó.
data class CicloLectura(
    val momento: MomentoLectura,   // la lectura que sigue
    val fecha: String,             // el día en que toca ("2026-09-28")
    val esHoy: Boolean,
    val atrasada: Boolean,
    val progreso: Float,           // 0 = falta todo, 0.5 = ya está el cierre, 1 = ciclo completo
    val completo: Boolean          // si es true, `momento` y `fecha` son de la siguiente semana
)

object CicloSemanal {

    // Se aceptan lecturas tomadas un poco tarde: el cierre hasta el domingo y la apertura hasta el jueves
    private const val MARGEN_CIERRE = 2
    private const val MARGEN_APERTURA = 3

    fun calcular(lecturas: List<Lectura>, hoy: String): CicloLectura? {
        val diaHoy = Fechas.aDia(hoy) ?: return null
        val diaSemana = Fechas.diaSemana(diaHoy)

        // Viernes más reciente (hoy incluido) y el lunes que le sigue
        val viernes = diaHoy - ((diaSemana - Fechas.VIERNES + 7) % 7)
        val lunes = viernes + 3

        fun hay(momento: MomentoLectura, desde: Long, margen: Int) = lecturas.any { lectura ->
            val dia = Fechas.aDia(lectura.fecha)
            lectura.momento == momento && dia != null && dia >= desde && dia <= desde + margen
        }

        val cierreHecho = hay(MomentoLectura.VIERNES_CIERRE, viernes, MARGEN_CIERRE)
        val aperturaHecha = hay(MomentoLectura.LUNES_INICIO, lunes, MARGEN_APERTURA)

        return if (diaHoy >= lunes) {
            // De lunes a jueves: toca la apertura (o el ciclo ya terminó)
            if (!aperturaHecha) {
                CicloLectura(
                    momento = MomentoLectura.LUNES_INICIO,
                    fecha = Fechas.deDia(lunes),
                    esHoy = diaHoy == lunes,
                    atrasada = diaHoy > lunes,
                    progreso = if (cierreHecho) 0.5f else 0f,
                    completo = false
                )
            } else {
                CicloLectura(
                    momento = MomentoLectura.VIERNES_CIERRE,
                    fecha = Fechas.deDia(viernes + 7),
                    esHoy = false,
                    atrasada = false,
                    progreso = 1f,
                    completo = true
                )
            }
        } else if (!cierreHecho) {
            // Viernes, sábado o domingo sin cierre
            CicloLectura(
                momento = MomentoLectura.VIERNES_CIERRE,
                fecha = Fechas.deDia(viernes),
                esHoy = diaHoy == viernes,
                atrasada = diaHoy > viernes,
                progreso = 0f,
                completo = false
            )
        } else {
            // Ya está el cierre: la siguiente es la apertura del lunes
            CicloLectura(
                momento = MomentoLectura.LUNES_INICIO,
                fecha = Fechas.deDia(lunes),
                esHoy = false,
                atrasada = false,
                progreso = 0.5f,
                completo = false
            )
        }
    }
}

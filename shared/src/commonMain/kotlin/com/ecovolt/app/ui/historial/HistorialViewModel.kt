package com.ecovolt.app.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.remote.SesionExpiradaException
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.model.Lectura
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.service.Compartidor
import com.ecovolt.app.service.Fechas
import com.ecovolt.app.service.fechaHoy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.round

// Una lectura con el consumo de su periodo (la diferencia contra la lectura anterior a ella)
data class RegistroLectura(val lectura: Lectura, val consumo: Double?)

data class HistorialUiState(
    val lecturas: List<Lectura> = emptyList(),          // todas, en orden cronológico
    val solo30Dias: Boolean = true,
    val momento: MomentoLectura? = null,                // null = apertura y cierre
    val consumoFinDeSemana: Double? = null,
    val consumoFantasma: Boolean = false,
    val cargando: Boolean = false,
    val mensaje: String? = null
) {
    // Cada lectura con su consumo. Se calcula con TODAS las lecturas y después se filtra,
    // así el filtro no cambia el consumo de cada periodo.
    private val registros: List<RegistroLectura> get() =
        lecturas.mapIndexed { i, lectura ->
            RegistroLectura(lectura, if (i == 0) null else lectura.valorKwh - lecturas[i - 1].valorKwh)
        }

    // Lo que se ve en la lista y la gráfica, de la más reciente a la más antigua
    val visibles: List<RegistroLectura> get() {
        val limite = if (solo30Dias) Fechas.sumarDias(fechaHoy(), -30) else null
        return registros
            .filter { limite == null || it.lectura.fecha >= limite }
            .filter { momento == null || it.lectura.momento == momento }
            .reversed()
    }

    val ultima: Lectura? get() = lecturas.lastOrNull()
}

class HistorialViewModel(
    private val repository: LecturaRepository,
    private val compartidor: Compartidor
) : ViewModel() {

    private val _estado = MutableStateFlow(HistorialUiState())
    val estado: StateFlow<HistorialUiState> = _estado.asStateFlow()

    init {
        aplicar(repository.lecturasGuardadas())
    }

    private fun aplicar(lecturas: List<Lectura>) {
        val consumo = repository.consumoFinDeSemana(lecturas)
        _estado.update {
            it.copy(
                lecturas = lecturas,
                consumoFinDeSemana = consumo,
                consumoFantasma = consumo != null && repository.hayConsumoFantasma(consumo)
            )
        }
    }

    // Se llama cada vez que se abre la pantalla: muestra lo guardado y actualiza desde el servidor
    fun cargar() {
        aplicar(repository.lecturasGuardadas())
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            repository.sincronizar()
                .onSuccess { lecturas ->
                    aplicar(lecturas)
                    _estado.update { it.copy(cargando = false) }
                }
                .onFailure { error ->
                    val mensaje = if (error is SesionExpiradaException) {
                        "Tu sesión expiró. Vuelve a iniciar sesión."
                    } else {
                        "Sin conexión con el servidor. Mostrando las lecturas guardadas en el teléfono."
                    }
                    _estado.update { it.copy(cargando = false, mensaje = mensaje) }
                }
        }
    }

    fun alternar30Dias() {
        _estado.update { it.copy(solo30Dias = !it.solo30Dias) }
    }

    // Tocar el chip elegido lo quita (vuelven a verse apertura y cierre)
    fun filtrarMomento(momento: MomentoLectura) {
        _estado.update { it.copy(momento = if (it.momento == momento) null else momento) }
    }

    fun cerrarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    // CSV con lo que se está viendo (respeta los filtros)
    fun exportar() {
        val registros = _estado.value.visibles.reversed()
        if (registros.isEmpty()) {
            _estado.update { it.copy(mensaje = "No hay lecturas para exportar con estos filtros.") }
            return
        }
        val csv = buildString {
            appendLine("fecha,dia,momento,hora,lectura_kwh,consumo_periodo_kwh,origen,nota")
            registros.forEach { (lectura, consumo) ->
                val campos = listOf(
                    lectura.fecha,
                    Fechas.corta(lectura.fecha).substringBefore(' '),
                    lectura.momento.etiqueta,
                    lectura.hora ?: "",
                    lectura.valorKwh.toString(),
                    consumo?.let { (round(it * 10) / 10).toString() } ?: "",
                    lectura.origen?.etiqueta ?: "",
                    lectura.nota ?: ""
                )
                appendLine(campos.joinToString(",") { campoCsv(it) })
            }
        }
        val error = compartidor.compartirArchivo("ecovolt_lecturas_${fechaHoy()}.csv", "text/csv", csv)
        if (error != null) _estado.update { it.copy(mensaje = error) }
    }

    // Entre comillas si trae comas, comillas o saltos de línea
    private fun campoCsv(valor: String): String =
        if (valor.any { it == ',' || it == '"' || it == '\n' }) "\"" + valor.replace("\"", "\"\"") + "\"" else valor
}

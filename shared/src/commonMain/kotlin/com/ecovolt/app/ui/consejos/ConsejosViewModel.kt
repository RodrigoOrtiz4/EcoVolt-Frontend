package com.ecovolt.app.ui.consejos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.repository.ConsejoRepository
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.model.AmbitoConsejo
import com.ecovolt.app.model.Consejo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Todo lo que la pantalla de consejos necesita mostrar (RFN-25)
data class ConsejosUiState(
    val ambito: AmbitoConsejo = AmbitoConsejo.ESCUELA,
    val consejos: List<Consejo> = emptyList(),      // todos (escuela y hogar)
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val consumoFinDeSemana: Double? = null,         // calculado con las lecturas guardadas en el teléfono
    val consumoFantasma: Boolean = false
) {
    // Solo los del ámbito elegido. Es una propiedad calculada: no se guarda, se obtiene al pedirla.
    val visibles: List<Consejo> get() = consejos.filter { it.ambito == ambito }
}

class ConsejosViewModel(
    private val consejoRepository: ConsejoRepository,
    private val lecturaRepository: LecturaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ConsejosUiState())
    val estado: StateFlow<ConsejosUiState> = _estado.asStateFlow()

    fun elegirAmbito(ambito: AmbitoConsejo) {
        _estado.update { it.copy(ambito = ambito) }
    }

    // Se llama cada vez que se abre la pantalla
    fun cargar() {
        if (_estado.value.cargando) return

        // 1) Al instante: lo guardado en el teléfono (sirve sin internet)
        val guardados = consejoRepository.consejosGuardados()
        _estado.update { if (guardados.isNotEmpty()) it.copy(consejos = guardados) else it }
        revisarConsumo()

        // 2) Después intenta traer la versión más reciente del servidor
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            consejoRepository.sincronizar()
                .onSuccess { consejos -> _estado.update { it.copy(consejos = consejos, cargando = false) } }
                .onFailure {
                    val mensaje = if (_estado.value.consejos.isEmpty()) {
                        "Sin conexión con el servidor. Conéctate a internet para descargar los consejos."
                    } else {
                        "Sin conexión. Mostrando los consejos guardados."
                    }
                    _estado.update { it.copy(cargando = false, mensaje = mensaje) }
                }
        }
    }

    // Recomendación personalizada: si con las lecturas del medidor se ve que el fin de semana
    // (con la escuela cerrada) se gastó más de lo normal, se avisa arriba de los consejos.
    private fun revisarConsumo() {
        val lecturas = lecturaRepository.lecturasGuardadas()
        val consumo = lecturaRepository.consumoFinDeSemana(lecturas)
        _estado.update {
            it.copy(
                consumoFinDeSemana = consumo,
                consumoFantasma = consumo != null && lecturaRepository.hayConsumoFantasma(consumo)
            )
        }
    }
}

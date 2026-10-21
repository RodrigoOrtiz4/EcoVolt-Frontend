package com.ecovolt.app.ui.lecturas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.remote.SesionExpiradaException
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.model.Lectura
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Todo lo que la pantalla necesita mostrar, en un solo objeto
data class LecturasUiState(
    val lecturas: List<Lectura> = emptyList(),
    val consumoFinDeSemana: Double? = null,
    val cargando: Boolean = false,
    val mensaje: String? = null
)

class LecturasViewModel(private val repository: LecturaRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LecturasUiState())
    val estado: StateFlow<LecturasUiState> = _estado.asStateFlow()

    init {
        // 1) Muestra primero lo guardado en el teléfono (funciona sin internet)
        val locales = repository.lecturasGuardadas()
        _estado.value = LecturasUiState(
            lecturas = locales,
            consumoFinDeSemana = repository.consumoFinDeSemana(locales)
        )
        // 2) Después intenta actualizar desde la API
        sincronizar()
    }

    fun sincronizar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            repository.sincronizar()
                .onSuccess { lecturas ->
                    _estado.update {
                        it.copy(
                            lecturas = lecturas,
                            consumoFinDeSemana = repository.consumoFinDeSemana(lecturas),
                            cargando = false,
                            mensaje = "Lecturas actualizadas"
                        )
                    }
                }
                .onFailure { error ->
                    val mensaje = if (error is SesionExpiradaException) {
                        "Tu sesión expiró. Cierra sesión y vuelve a entrar. Mostrando lecturas guardadas."
                    } else {
                        "Sin conexión con el servidor. Mostrando lecturas guardadas."
                    }
                    _estado.update { it.copy(cargando = false, mensaje = mensaje) }
                }
        }
    }
}
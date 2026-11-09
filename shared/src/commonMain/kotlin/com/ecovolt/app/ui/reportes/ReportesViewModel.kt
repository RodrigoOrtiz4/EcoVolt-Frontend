package com.ecovolt.app.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.remote.SesionExpiradaException
import com.ecovolt.app.data.remote.ServidorRechazoException
import com.ecovolt.app.data.repository.ReporteRepository
import com.ecovolt.app.model.EstatusReporte
import com.ecovolt.app.model.Reporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Todo lo que la lista de reportes necesita mostrar
data class ReportesUiState(
    val reportes: List<Reporte> = emptyList(),
    val filtro: EstatusReporte? = null,             // null = todos
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val fotos: Map<Int, ByteArray> = emptyMap(),   // id del reporte → foto ya descargada
    val cargandoFotos: Set<Int> = emptySet(),      // ids cuya foto se está descargando
    val actualizando: Set<Int> = emptySet()        // ids cuyo estatus se está cambiando
) {
    val visibles: List<Reporte> get() = if (filtro == null) reportes else reportes.filter { it.estatus == filtro }

    fun cuantos(estatus: EstatusReporte): Int = reportes.count { it.estatus == estatus }
}

class ReportesViewModel(private val repository: ReporteRepository) : ViewModel() {

    private val _estado = MutableStateFlow(ReportesUiState())
    val estado: StateFlow<ReportesUiState> = _estado.asStateFlow()

    // Se llama cada vez que se abre la pantalla, así aparece el reporte que se acaba de enviar
    fun cargar() {
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            repository.reportes()
                .onSuccess { reportes -> _estado.update { it.copy(reportes = reportes, cargando = false) } }
                .onFailure { error ->
                    val mensaje = if (error is SesionExpiradaException) {
                        "Tu sesión expiró. Vuelve a iniciar sesión."
                    } else {
                        "Sin conexión con el servidor. Intenta de nuevo."
                    }
                    _estado.update { it.copy(cargando = false, mensaje = mensaje) }
                }
        }
    }

    fun filtrar(estatus: EstatusReporte?) {
        _estado.update { it.copy(filtro = if (it.filtro == estatus) null else estatus) }
    }

    // Las fotos se descargan solo cuando alguien las quiere ver: así la lista carga rápido
    fun verFoto(id: Int) {
        val actual = _estado.value
        if (id in actual.fotos || id in actual.cargandoFotos) return
        viewModelScope.launch {
            _estado.update { it.copy(cargandoFotos = it.cargandoFotos + id) }
            repository.foto(id)
                .onSuccess { bytes ->
                    _estado.update { it.copy(fotos = it.fotos + (id to bytes), cargandoFotos = it.cargandoFotos - id) }
                }
                .onFailure {
                    _estado.update { it.copy(cargandoFotos = it.cargandoFotos - id, mensaje = "No se pudo descargar la foto.") }
                }
        }
    }

    fun ocultarFoto(id: Int) {
        _estado.update { it.copy(fotos = it.fotos - id) }
    }

    // Director o responsable: "Marcar en proceso" / "Marcar resuelto"
    fun cambiarEstatus(id: Int, estatus: EstatusReporte) {
        if (id in _estado.value.actualizando) return
        viewModelScope.launch {
            _estado.update { it.copy(actualizando = it.actualizando + id, mensaje = null) }
            repository.cambiarEstatus(id, estatus)
                .onSuccess { cambiado ->
                    _estado.update { estado ->
                        estado.copy(
                            reportes = estado.reportes.map { if (it.id == cambiado.id) cambiado else it },
                            actualizando = estado.actualizando - id
                        )
                    }
                }
                .onFailure { causa ->
                    val mensaje = when (causa) {
                        is SesionExpiradaException -> "Tu sesión expiró. Vuelve a iniciar sesión."
                        is ServidorRechazoException -> causa.message ?: "No se pudo cambiar el estatus."
                        else -> "Sin conexión con el servidor. Intenta de nuevo."
                    }
                    _estado.update { it.copy(actualizando = it.actualizando - id, mensaje = mensaje) }
                }
        }
    }
}

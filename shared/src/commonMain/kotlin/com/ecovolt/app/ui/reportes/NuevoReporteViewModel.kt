package com.ecovolt.app.ui.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.remote.SesionExpiradaException
import com.ecovolt.app.data.remote.ServidorRechazoException
import com.ecovolt.app.data.repository.ReporteRepository
import com.ecovolt.app.model.Reporte
import com.ecovolt.app.model.TipoReporte
import com.ecovolt.app.service.Foto
import com.ecovolt.app.service.ResultadoFoto
import com.ecovolt.app.service.SelectorFoto
import com.ecovolt.app.service.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Todo lo que el formulario de reporte necesita mostrar (RFN-19: foto, área y descripción)
data class NuevoReporteUiState(
    val tipo: TipoReporte = TipoReporte.DESPERDICIO,
    val area: String = "",
    val descripcion: String = "",
    val foto: Foto? = null,
    val procesandoFoto: Boolean = false,   // la cámara o la galería están abiertas, o se está reduciendo la foto
    val enviando: Boolean = false,         // se está enviando el reporte al backend
    val errorArea: String? = null,
    val errorDescripcion: String? = null,
    val errorFoto: String? = null,
    val mensaje: String? = null
) {
    // Mientras pasa algo de esto no se permite tocar los botones (evita dobles envíos)
    val ocupado: Boolean get() = procesandoFoto || enviando
}

class NuevoReporteViewModel(
    private val selector: SelectorFoto,
    private val repository: ReporteRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(NuevoReporteUiState())
    val estado: StateFlow<NuevoReporteUiState> = _estado.asStateFlow()

    fun elegirTipo(tipo: TipoReporte) {
        _estado.update { it.copy(tipo = tipo) }
    }

    fun alCambiarArea(valor: String) {
        if (valor.length > 50) return
        _estado.update { it.copy(area = valor, errorArea = null) }
    }

    // Botones rápidos ("Salón", "Baños"...): escriben el área para no teclearla completa
    fun usarArea(sugerencia: String) = alCambiarArea(sugerencia)

    fun alCambiarDescripcion(valor: String) {
        if (valor.length > 500) return
        _estado.update { it.copy(descripcion = valor, errorDescripcion = null) }
    }

    fun tomarFoto() = pedirFoto { alTerminar -> selector.tomarFoto(alTerminar) }

    fun elegirDeGaleria() = pedirFoto { alTerminar -> selector.elegirDeGaleria(alTerminar) }

    fun quitarFoto() {
        _estado.update { it.copy(foto = null) }
    }

    // Las dos formas de conseguir la foto terminan igual; solo cambia qué se abre.
    // `abrir` recibe la función que se debe llamar cuando la plataforma tenga el resultado.
    private fun pedirFoto(abrir: ((ResultadoFoto) -> Unit) -> Unit) {
        if (_estado.value.ocupado) return
        _estado.update { it.copy(procesandoFoto = true, mensaje = null) }

        abrir { resultado ->
            when (resultado) {
                is ResultadoFoto.Obtenida -> _estado.update {
                    it.copy(procesandoFoto = false, foto = resultado.foto, errorFoto = null)
                }
                ResultadoFoto.Cancelada -> _estado.update { it.copy(procesandoFoto = false) }
                is ResultadoFoto.Error -> _estado.update { it.copy(procesandoFoto = false, mensaje = resultado.mensaje) }
            }
        }
    }

    // Se llama al salir del formulario sin enviar: no se guarda un borrador a medias
    fun limpiar() {
        if (!_estado.value.ocupado) _estado.value = NuevoReporteUiState()
    }

    fun cerrarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    fun enviar(alEnviar: (Reporte) -> Unit) {
        val actual = _estado.value
        if (actual.ocupado) return

        // 1) Validar todo antes de enviar (el backend lo vuelve a revisar)
        val errorArea = Validador.validarArea(actual.area)
        val errorDescripcion = Validador.validarDescripcion(actual.descripcion)
        val errorFoto = if (actual.foto == null) "Agrega una foto de lo que estás reportando" else null
        if (errorArea != null || errorDescripcion != null || errorFoto != null) {
            _estado.update { it.copy(errorArea = errorArea, errorDescripcion = errorDescripcion, errorFoto = errorFoto) }
            return
        }
        val foto = actual.foto ?: return // aquí ya no es null; la línea solo convence al compilador

        // 2) Enviar. Solo si el servidor lo acepta se limpia el formulario y se regresa.
        viewModelScope.launch {
            _estado.update { it.copy(enviando = true, mensaje = null) }
            repository.enviar(actual.tipo, actual.area, actual.descripcion, foto.bytes)
                .onSuccess { reporte ->
                    _estado.value = NuevoReporteUiState()
                    alEnviar(reporte)
                }
                .onFailure { causa ->
                    // Se conserva lo escrito y la foto para reintentar sin capturar todo otra vez
                    val mensaje = when (causa) {
                        is SesionExpiradaException -> "Tu sesión expiró. Vuelve a iniciar sesión para enviar el reporte."
                        is ServidorRechazoException -> causa.message ?: "El servidor rechazó el reporte."
                        else -> "No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo."
                    }
                    _estado.update { it.copy(enviando = false, mensaje = mensaje) }
                }
        }
    }
}

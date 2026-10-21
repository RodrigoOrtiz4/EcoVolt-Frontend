package com.ecovolt.app.ui.captura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.remote.SesionExpiradaException
import com.ecovolt.app.data.remote.ServidorRechazoException
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.service.ExtractorLectura
import com.ecovolt.app.service.LectorMedidor
import com.ecovolt.app.service.ResultadoCaptura
import com.ecovolt.app.service.Validador
import com.ecovolt.app.service.fechaHoy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Todo lo que la pantalla de captura necesita mostrar
data class CapturaUiState(
    val momento: MomentoLectura = MomentoLectura.VIERNES_CIERRE,
    val leyendo: Boolean = false,     // la cámara o el OCR están trabajando
    val guardando: Boolean = false,   // se está enviando la lectura al backend
    val textoOcr: String = "",
    val candidatos: List<String> = emptyList(),
    val valor: String = "",
    val errorValor: String? = null,
    val mensaje: String? = null
)

class CapturaViewModel(
    private val lector: LectorMedidor,
    private val repository: LecturaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(CapturaUiState())
    val estado: StateFlow<CapturaUiState> = _estado.asStateFlow()

    fun elegirMomento(momento: MomentoLectura) {
        _estado.update { it.copy(momento = momento) }
    }

    fun alCambiarValor(valor: String) {
        if (valor.length > 10 || !valor.all { it.isDigit() || it == '.' || it == ',' }) return
        _estado.update { it.copy(valor = valor, errorValor = null) }
    }

    fun usarCandidato(candidato: String) {
        _estado.update { it.copy(valor = candidato, errorValor = null) }
    }

    // 1) Foto  2) OCR  3) Se proponen los números encontrados y el usuario confirma
    fun tomarFoto() {
        if (_estado.value.leyendo || _estado.value.guardando) return
        _estado.update { it.copy(leyendo = true, mensaje = null) }

        lector.capturar { resultado ->
            when (resultado) {
                is ResultadoCaptura.Texto -> {
                    val candidatos = ExtractorLectura.candidatos(resultado.texto)
                    _estado.update {
                        it.copy(
                            leyendo = false,
                            textoOcr = resultado.texto.trim(),
                            candidatos = candidatos,
                            valor = candidatos.firstOrNull() ?: "",
                            errorValor = null,
                            mensaje = if (candidatos.isEmpty()) {
                                "No se encontró un número en la foto. Escríbelo a mano o intenta otra foto."
                            } else {
                                "Revisa que el número coincida con el medidor antes de guardar."
                            }
                        )
                    }
                }
                ResultadoCaptura.Cancelado -> _estado.update { it.copy(leyendo = false) }
                is ResultadoCaptura.Error -> _estado.update { it.copy(leyendo = false, mensaje = resultado.mensaje) }
            }
        }
    }

    // 4) Se envía al backend. Solo si el servidor la acepta se limpia la pantalla y se regresa.
    fun guardar(alGuardar: () -> Unit) {
        val actual = _estado.value
        if (actual.leyendo || actual.guardando) return

        val error = Validador.validarLectura(actual.valor)
        if (error != null) {
            _estado.update { it.copy(errorValor = error) }
            return
        }
        val valor = actual.valor.trim().replace(',', '.').toDouble()

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, mensaje = null) }
            repository.agregarLectura(actual.momento, valor, fechaHoy())
                .onSuccess {
                    _estado.value = CapturaUiState()
                    alGuardar()
                }
                .onFailure { causa ->
                    // Se conserva el número escrito para que se pueda reintentar sin volver a tomar la foto
                    val mensaje = when (causa) {
                        is SesionExpiradaException -> "Tu sesión expiró. Cierra sesión y vuelve a entrar para guardar."
                        is ServidorRechazoException -> causa.message ?: "El servidor rechazó la lectura."
                        else -> "No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo."
                    }
                    _estado.update { it.copy(guardando = false, mensaje = mensaje) }
                }
        }
    }
}

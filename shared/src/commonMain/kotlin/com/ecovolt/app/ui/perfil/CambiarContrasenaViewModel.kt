package com.ecovolt.app.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.repository.AuthRepository
import com.ecovolt.app.data.repository.ResultadoCambioContrasena
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CambiarContrasenaUiState(
    val actual: String = "",
    val nueva: String = "",
    val confirmacion: String = "",
    val mostrarActual: Boolean = false,
    val mostrarNueva: Boolean = false,
    val mostrarConfirmacion: Boolean = false,
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorConfirmacion: String? = null,
    val mensaje: String? = null,
    val guardando: Boolean = false
) {
    // Requisitos que se van palomeando mientras se escribe (mismas reglas que el backend)
    val tieneLongitud: Boolean get() = nueva.length >= 8
    val tieneMayusculaYNumero: Boolean get() = nueva.any { it.isUpperCase() } && nueva.any { it.isDigit() }
}

class CambiarContrasenaViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(CambiarContrasenaUiState())
    val estado: StateFlow<CambiarContrasenaUiState> = _estado.asStateFlow()

    fun alCambiarActual(valor: String) {
        if (valor.length > 32) return
        _estado.update { it.copy(actual = valor, errorActual = null, mensaje = null) }
    }

    fun alCambiarNueva(valor: String) {
        if (valor.length > 32) return
        _estado.update { it.copy(nueva = valor, errorNueva = null, errorConfirmacion = null, mensaje = null) }
    }

    fun alCambiarConfirmacion(valor: String) {
        if (valor.length > 32) return
        _estado.update { it.copy(confirmacion = valor, errorConfirmacion = null, mensaje = null) }
    }

    fun alternarActual() = _estado.update { it.copy(mostrarActual = !it.mostrarActual) }
    fun alternarNueva() = _estado.update { it.copy(mostrarNueva = !it.mostrarNueva) }
    fun alternarConfirmacion() = _estado.update { it.copy(mostrarConfirmacion = !it.mostrarConfirmacion) }

    fun limpiar() {
        if (!_estado.value.guardando) _estado.value = CambiarContrasenaUiState()
    }

    // alExito: el servidor cerró la sesión en todos los dispositivos; hay que volver a entrar.
    // alSesionVencida: el token ya no servía (por ejemplo, se cambió desde otro teléfono).
    fun guardar(alExito: () -> Unit, alSesionVencida: () -> Unit) {
        val actual = _estado.value
        if (actual.guardando) return

        val errorActual = if (actual.actual.isEmpty()) "Escribe tu contraseña actual" else null
        val errorNueva = when {
            actual.nueva.isEmpty() -> "Escribe la nueva contraseña"
            actual.nueva.any { it.isWhitespace() } -> "No puede llevar espacios"
            !actual.tieneLongitud -> "Debe tener al menos 8 caracteres"
            !actual.tieneMayusculaYNumero -> "Incluye una mayúscula y un número"
            actual.nueva == actual.actual -> "Debe ser distinta de la contraseña actual"
            else -> null
        }
        val errorConfirmacion = when {
            actual.confirmacion.isEmpty() -> "Repite la nueva contraseña"
            actual.confirmacion != actual.nueva -> "Las contraseñas no coinciden"
            else -> null
        }
        if (errorActual != null || errorNueva != null || errorConfirmacion != null) {
            _estado.update { it.copy(errorActual = errorActual, errorNueva = errorNueva, errorConfirmacion = errorConfirmacion) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, mensaje = null) }
            when (val resultado = auth.cambiarContrasena(actual.actual, actual.nueva)) {
                ResultadoCambioContrasena.Exito -> {
                    _estado.value = CambiarContrasenaUiState()
                    alExito()
                }
                ResultadoCambioContrasena.SesionVencida -> {
                    _estado.value = CambiarContrasenaUiState()
                    alSesionVencida()
                }
                is ResultadoCambioContrasena.Rechazado -> _estado.update {
                    it.copy(
                        guardando = false,
                        actual = if (resultado.errores.containsKey("actual")) "" else it.actual,
                        errorActual = resultado.errores["actual"],
                        errorNueva = resultado.errores["nueva"],
                        mensaje = if (resultado.errores.isEmpty()) resultado.mensaje else null
                    )
                }
                is ResultadoCambioContrasena.Error -> _estado.update {
                    it.copy(guardando = false, mensaje = resultado.mensaje)
                }
            }
        }
    }
}

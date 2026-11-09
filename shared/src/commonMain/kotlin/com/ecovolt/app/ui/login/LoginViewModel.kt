package com.ecovolt.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.repository.AuthRepository
import com.ecovolt.app.data.repository.ResultadoLogin
import com.ecovolt.app.model.Rol
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.service.Validador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Error grande que se muestra arriba del formulario ("No pudimos iniciar sesión")
data class ErrorLogin(val titulo: String, val texto: String)

// Todo lo que la pantalla de login necesita mostrar
data class LoginUiState(
    val rolElegido: Rol = Rol.RESPONSABLE,   // solo cambia el ejemplo del usuario: el rol real lo da el servidor
    val usuario: String = "",
    val contrasena: String = "",
    val mostrarContrasena: Boolean = false,
    val errorUsuario: String? = null,
    val errorContrasena: String? = null,
    val errorGeneral: ErrorLogin? = null,
    val cargando: Boolean = false
)

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun elegirRol(rol: Rol) {
        _estado.update { it.copy(rolElegido = rol) }
    }

    fun alCambiarUsuario(valor: String) {
        if (valor.length > 20) return
        _estado.update { it.copy(usuario = valor, errorUsuario = null, errorGeneral = null) }
    }

    fun alCambiarContrasena(valor: String) {
        if (valor.length > 32) return
        _estado.update { it.copy(contrasena = valor, errorContrasena = null, errorGeneral = null) }
    }

    fun alternarContrasena() {
        _estado.update { it.copy(mostrarContrasena = !it.mostrarContrasena) }
    }

    // Al salir de la pantalla se borra lo escrito (la contraseña no debe quedarse en memoria)
    fun limpiar() {
        _estado.value = LoginUiState(rolElegido = _estado.value.rolElegido)
    }

    fun iniciarSesion(alExito: (Usuario) -> Unit) {
        val actual = _estado.value
        if (actual.cargando) return

        // 1) Validar los campos antes de consultar nada
        val errorUsuario = Validador.validarUsuario(actual.usuario)
        val errorContrasena = if (actual.contrasena.isEmpty()) "Escribe tu contraseña" else null
        if (errorUsuario != null || errorContrasena != null) {
            _estado.update { it.copy(errorUsuario = errorUsuario, errorContrasena = errorContrasena) }
            return
        }

        // 2) Verificar las credenciales
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            when (val resultado = auth.iniciarSesion(actual.usuario, actual.contrasena)) {
                is ResultadoLogin.Exito -> {
                    limpiar()
                    alExito(resultado.usuario)
                }
                is ResultadoLogin.CredencialesInvalidas -> _estado.update {
                    it.copy(
                        cargando = false,
                        contrasena = "",
                        errorUsuario = "Revisa el usuario",
                        errorContrasena = "Contraseña incorrecta",
                        errorGeneral = ErrorLogin(
                            titulo = "No pudimos iniciar sesión",
                            texto = textoIntentos(resultado.intentosRestantes)
                        )
                    )
                }
                is ResultadoLogin.Bloqueado -> _estado.update {
                    it.copy(
                        cargando = false,
                        contrasena = "",
                        errorGeneral = ErrorLogin("Acceso bloqueado por seguridad", resultado.mensaje)
                    )
                }
                is ResultadoLogin.Error -> _estado.update {
                    it.copy(cargando = false, errorGeneral = ErrorLogin("No pudimos iniciar sesión", resultado.mensaje))
                }
            }
        }
    }

    private fun textoIntentos(restantes: Int?): String = when (restantes) {
        null -> "Usuario o contraseña incorrectos."
        0 -> "Usuario o contraseña incorrectos. Se bloqueó el acceso por 15 minutos."
        1 -> "Usuario o contraseña incorrectos. Te queda 1 intento."
        else -> "Usuario o contraseña incorrectos. Te quedan $restantes intentos."
    }
}

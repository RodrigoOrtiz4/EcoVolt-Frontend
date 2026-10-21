package com.ecovolt.app.data.repository

import com.ecovolt.app.data.local.SesionStorage
import com.ecovolt.app.data.remote.AuthApi
import com.ecovolt.app.data.remote.RespuestaAuth
import com.ecovolt.app.model.Usuario
import kotlin.coroutines.cancellation.CancellationException

sealed interface ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin
    data object CredencialesInvalidas : ResultadoLogin
    data class Error(val mensaje: String) : ResultadoLogin
}

sealed interface ResultadoRegistro {
    data class Exito(val usuario: Usuario) : ResultadoRegistro
    data object UsuarioExistente : ResultadoRegistro
    data class Error(val mensaje: String) : ResultadoRegistro
}

private const val SIN_SERVIDOR = "No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo."

// Lógica del login y del registro. Ya no valida contraseñas ni guarda usuarios: eso lo hace el
// backend. Aquí solo se envían los datos, se interpreta la respuesta y se recuerda la sesión.
class AuthRepository(
    private val api: AuthApi,
    private val sesion: SesionStorage
) {

    suspend fun iniciarSesion(usuario: String, contrasena: String): ResultadoLogin {
        val respuesta = try {
            api.login(usuario.trim(), contrasena)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ResultadoLogin.Error("DEBUG: $e")
        }

        return when (respuesta) {
            is RespuestaAuth.Aceptada -> {
                sesion.guardar(respuesta.datos.usuario, respuesta.datos.token)
                ResultadoLogin.Exito(respuesta.datos.usuario)
            }
            is RespuestaAuth.Rechazada -> when (respuesta.codigo) {
                401 -> ResultadoLogin.CredencialesInvalidas
                else -> ResultadoLogin.Error(respuesta.mensaje)
            }
        }
    }

    // Todo registro entra como ALUMNO: el rol lo decide el servidor, la app no puede elegirlo.
    suspend fun registrar(nombre: String, usuario: String, contrasena: String): ResultadoRegistro {
        val respuesta = try {
            api.registrar(nombre.trim(), usuario.trim(), contrasena)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ResultadoRegistro.Error(SIN_SERVIDOR)
        }

        return when (respuesta) {
            is RespuestaAuth.Aceptada -> {
                // Al terminar el registro la persona entra directo, sin volver a escribir sus datos
                sesion.guardar(respuesta.datos.usuario, respuesta.datos.token)
                ResultadoRegistro.Exito(respuesta.datos.usuario)
            }
            is RespuestaAuth.Rechazada -> when (respuesta.codigo) {
                409 -> ResultadoRegistro.UsuarioExistente
                else -> ResultadoRegistro.Error(respuesta.mensaje)
            }
        }
    }

    // Una sesión sin token (guardada por una versión anterior de la app) no sirve con el backend:
    // se trata como si no hubiera sesión y se pide iniciar de nuevo.
    fun sesionActual(): Usuario? =
        if (sesion.obtenerToken() == null) null else sesion.obtener()

    fun cerrarSesion() = sesion.cerrar()
}

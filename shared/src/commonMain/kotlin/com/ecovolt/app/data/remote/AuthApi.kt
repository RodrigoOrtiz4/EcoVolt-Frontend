package com.ecovolt.app.data.remote

import com.ecovolt.app.config.AppConfig
import com.ecovolt.app.model.Usuario
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Formatos del JSON que habla con el backend (rutas /auth/...)
@Serializable
data class LoginRequest(val usuario: String, val contrasena: String)

@Serializable
data class RegistroRequest(val nombre: String, val usuario: String, val contrasena: String)

@Serializable
data class CambioContrasenaRequest(val actual: String, val nueva: String)

// El backend responde con el usuario (sin contraseña) y un token para las rutas protegidas
@Serializable
data class AuthResponse(val token: String, val usuario: Usuario)

@Serializable
private data class ErrorResponse(
    val mensaje: String? = null,
    val errores: Map<String, String>? = null,
    @SerialName("intentos_restantes") val intentosRestantes: Int? = null
)

// El servidor sí respondió: o aceptó la petición o la rechazó con un código HTTP
sealed interface RespuestaAuth {
    data class Aceptada(val datos: AuthResponse) : RespuestaAuth
    data class Rechazada(
        val codigo: Int,
        val mensaje: String,
        val errores: Map<String, String> = emptyMap(),
        val intentosRestantes: Int? = null    // solo en el login: cuántos intentos quedan antes del bloqueo
    ) : RespuestaAuth
}

// Cliente HTTP del backend para iniciar sesión, registrarse y cambiar la contraseña.
// Si no hay conexión con el servidor lanza una excepción; quien lo llama (AuthRepository) la maneja.
class AuthApi(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun login(usuario: String, contrasena: String): RespuestaAuth =
        enviar(AppConfig.LOGIN_PATH, json.encodeToString(LoginRequest(usuario, contrasena)))

    suspend fun registrar(nombre: String, usuario: String, contrasena: String): RespuestaAuth =
        enviar(AppConfig.REGISTER_PATH, json.encodeToString(RegistroRequest(nombre, usuario, contrasena)))

    // Devuelve null si el servidor aceptó el cambio, o el rechazo con su motivo
    suspend fun cambiarContrasena(token: String, actual: String, nueva: String): RespuestaAuth.Rechazada? {
        val respuesta = client.post("${AppConfig.BASE_URL}${AppConfig.CAMBIAR_CONTRASENA_PATH}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(CambioContrasenaRequest(actual, nueva)))
        }
        return if (respuesta.status.isSuccess()) null else rechazo(respuesta)
    }

    private suspend fun enviar(ruta: String, cuerpoJson: String): RespuestaAuth {
        val respuesta = client.post("${AppConfig.BASE_URL}$ruta") {
            contentType(ContentType.Application.Json)
            setBody(cuerpoJson)
        }
        return if (respuesta.status.isSuccess()) {
            RespuestaAuth.Aceptada(json.decodeFromString<AuthResponse>(respuesta.bodyAsText()))
        } else {
            rechazo(respuesta)
        }
    }

    private suspend fun rechazo(respuesta: HttpResponse): RespuestaAuth.Rechazada {
        val error = try {
            json.decodeFromString<ErrorResponse>(respuesta.bodyAsText())
        } catch (e: Exception) {
            null
        }
        return RespuestaAuth.Rechazada(
            codigo = respuesta.status.value,
            mensaje = error?.mensaje ?: "El servidor rechazó la solicitud",
            errores = error?.errores ?: emptyMap(),
            intentosRestantes = error?.intentosRestantes
        )
    }
}

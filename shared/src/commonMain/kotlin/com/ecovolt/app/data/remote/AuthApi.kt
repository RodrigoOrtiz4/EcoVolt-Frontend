package com.ecovolt.app.data.remote

import com.ecovolt.app.config.AppConfig
import com.ecovolt.app.model.Usuario
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Formatos del JSON que habla con el backend (rutas /auth/login y /auth/register)
@Serializable
data class LoginRequest(val usuario: String, val contrasena: String)

@Serializable
data class RegistroRequest(val nombre: String, val usuario: String, val contrasena: String)

// El backend responde con el usuario (sin contraseña) y un token para las rutas protegidas
@Serializable
data class AuthResponse(val token: String, val usuario: Usuario)

@Serializable
private data class ErrorResponse(val mensaje: String? = null)

// El servidor sí respondió: o aceptó la petición o la rechazó con un código HTTP
sealed interface RespuestaAuth {
    data class Aceptada(val datos: AuthResponse) : RespuestaAuth
    data class Rechazada(val codigo: Int, val mensaje: String) : RespuestaAuth
}

// Cliente HTTP del backend para iniciar sesión y registrarse.
// Si no hay conexión con el servidor lanza una excepción; quien lo llama (AuthRepository) la maneja.
class AuthApi(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun login(usuario: String, contrasena: String): RespuestaAuth =
        enviar(AppConfig.LOGIN_PATH, json.encodeToString(LoginRequest(usuario, contrasena)))

    suspend fun registrar(nombre: String, usuario: String, contrasena: String): RespuestaAuth =
        enviar(AppConfig.REGISTER_PATH, json.encodeToString(RegistroRequest(nombre, usuario, contrasena)))

    private suspend fun enviar(ruta: String, cuerpoJson: String): RespuestaAuth {
        val respuesta = client.post("${AppConfig.BASE_URL}$ruta") {
            contentType(ContentType.Application.Json)
            setBody(cuerpoJson)
        }
        val texto = respuesta.bodyAsText()
        return if (respuesta.status.isSuccess()) {
            RespuestaAuth.Aceptada(json.decodeFromString<AuthResponse>(texto))
        } else {
            RespuestaAuth.Rechazada(respuesta.status.value, mensajeDe(texto))
        }
    }

    private fun mensajeDe(texto: String): String =
        try {
            json.decodeFromString<ErrorResponse>(texto).mensaje
        } catch (e: Exception) {
            null
        } ?: "El servidor rechazó la solicitud"
}

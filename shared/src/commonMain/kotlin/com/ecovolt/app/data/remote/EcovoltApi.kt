package com.ecovolt.app.data.remote

import com.ecovolt.app.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// El servidor rechazó el token: venció o ya no es válido. Hay que volver a iniciar sesión.
class SesionExpiradaException : Exception("La sesión expiró")

// El servidor respondió, pero no aceptó la petición (datos inválidos, sin permiso, etc.).
// El mensaje es el que mandó el backend, listo para mostrarse en pantalla.
class ServidorRechazoException(mensaje: String) : Exception(mensaje)

@Serializable
private data class ErrorApi(val mensaje: String? = null)

// Lecturas del medidor. Las rutas del backend están protegidas, así que se envía el token de la sesión.
class EcoVoltApi(
    private val client: HttpClient,
    private val tokenProvider: () -> String?
) {

    private val json = Json { ignoreUnknownKeys = true }

    // GET /lecturas: todas las lecturas guardadas en el servidor
    suspend fun obtenerLecturas(): List<LecturaDto> {
        val respuesta = client.get("${AppConfig.BASE_URL}${AppConfig.LECTURAS_PATH}") {
            conToken()
        }
        revisar(respuesta)
        return json.decodeFromString(respuesta.bodyAsText())
    }

    // POST /lecturas: guarda la lectura que se leyó con la cámara y el usuario confirmó.
    // El servidor responde con la misma lectura, ya con el id que le asignó.
    suspend fun enviarLectura(nueva: NuevaLecturaDto): LecturaDto {
        val respuesta = client.post("${AppConfig.BASE_URL}${AppConfig.LECTURAS_PATH}") {
            conToken()
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(nueva))
        }
        revisar(respuesta)
        return json.decodeFromString(respuesta.bodyAsText())
    }

    // Agrega "Authorization: Bearer <token>" si hay una sesión iniciada
    private fun HttpRequestBuilder.conToken() {
        tokenProvider()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    // Convierte las respuestas de error del servidor en excepciones que el repositorio sabe manejar
    private suspend fun revisar(respuesta: HttpResponse) {
        if (respuesta.status == HttpStatusCode.Unauthorized) throw SesionExpiradaException()
        if (!respuesta.status.isSuccess()) {
            val mensaje = try {
                json.decodeFromString<ErrorApi>(respuesta.bodyAsText()).mensaje
            } catch (e: Exception) {
                null
            }
            throw ServidorRechazoException(mensaje ?: "Error del servidor (${respuesta.status.value})")
        }
    }
}

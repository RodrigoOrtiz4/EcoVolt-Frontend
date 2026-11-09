package com.ecovolt.app.data.remote

import com.ecovolt.app.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
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
// `errores` trae el problema de cada campo ("nota", "valor_kwh"...) cuando el backend lo indica.
class ServidorRechazoException(
    mensaje: String,
    val codigo: Int = 0,
    val errores: Map<String, String> = emptyMap()
) : Exception(mensaje)

@Serializable
private data class ErrorApi(val mensaje: String? = null, val errores: Map<String, String>? = null)

// Lecturas, reportes y consejos. Las rutas del backend están protegidas, así que se envía el token de la sesión.
// `alSesionVencida` avisa a la app cuando el servidor rechaza el token (venció o se cambió la
// contraseña en otro dispositivo) para regresar al inicio de sesión.
class EcoVoltApi(
    private val client: HttpClient,
    private val tokenProvider: () -> String?,
    private val alSesionVencida: () -> Unit = {}
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

    // ---- RFN-19: reportes de desperdicio o falla ----

    // POST /reportes: envía el reporte con su foto (Base64 dentro del JSON).
    // El servidor responde con el reporte guardado (id, fecha y autor), sin la foto.
    suspend fun enviarReporte(nuevo: NuevoReporteDto): ReporteDto {
        val respuesta = client.post("${AppConfig.BASE_URL}${AppConfig.REPORTES_PATH}") {
            conToken()
            // Subir la foto con datos móviles puede tardar más que una petición normal
            timeout { requestTimeoutMillis = AppConfig.TIMEOUT_SUBIDA_MS }
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(nuevo))
        }
        revisar(respuesta)
        return json.decodeFromString(respuesta.bodyAsText())
    }

    // GET /reportes: el servidor decide cuáles ve cada rol (todos o solo los propios)
    suspend fun obtenerReportes(): List<ReporteDto> {
        val respuesta = client.get("${AppConfig.BASE_URL}${AppConfig.REPORTES_PATH}") {
            conToken()
        }
        revisar(respuesta)
        return json.decodeFromString(respuesta.bodyAsText())
    }

    // PATCH /reportes/{id}/estatus: dirección marca el reporte como en proceso o resuelto
    suspend fun cambiarEstatusReporte(id: Int, estatus: String): ReporteDto {
        val respuesta = client.patch("${AppConfig.BASE_URL}${AppConfig.REPORTES_PATH}/$id/estatus") {
            conToken()
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(CambioEstatusDto(estatus)))
        }
        revisar(respuesta)
        return json.decodeFromString(respuesta.bodyAsText())
    }

    // GET /reportes/{id}/foto: la imagen tal cual. No es JSON, por eso se lee como bytes.
    suspend fun obtenerFotoReporte(id: Int): ByteArray {
        val respuesta = client.get("${AppConfig.BASE_URL}${AppConfig.REPORTES_PATH}/$id/foto") {
            conToken()
        }
        revisar(respuesta)
        return respuesta.body<ByteArray>()
    }

    // ---- RFN-25: consejos de ahorro ----

    // GET /consejos: ruta pública, no necesita token
    suspend fun obtenerConsejos(): List<ConsejoDto> {
        val respuesta = client.get("${AppConfig.BASE_URL}${AppConfig.CONSEJOS_PATH}")
        revisar(respuesta)
        return json.decodeFromString(respuesta.bodyAsText())
    }

    // Agrega "Authorization: Bearer <token>" si hay una sesión iniciada
    private fun HttpRequestBuilder.conToken() {
        tokenProvider()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

    // Convierte las respuestas de error del servidor en excepciones que el repositorio sabe manejar
    private suspend fun revisar(respuesta: HttpResponse) {
        if (respuesta.status == HttpStatusCode.Unauthorized) {
            alSesionVencida()
            throw SesionExpiradaException()
        }
        if (!respuesta.status.isSuccess()) {
            val error = try {
                json.decodeFromString<ErrorApi>(respuesta.bodyAsText())
            } catch (e: Exception) {
                null
            }
            throw ServidorRechazoException(
                mensaje = error?.mensaje ?: "Error del servidor (${respuesta.status.value})",
                codigo = respuesta.status.value,
                errores = error?.errores ?: emptyMap()
            )
        }
    }
}

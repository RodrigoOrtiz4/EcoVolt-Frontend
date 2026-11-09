package com.ecovolt.app.data.repository

import com.ecovolt.app.data.remote.EcoVoltApi
import com.ecovolt.app.data.remote.NuevoReporteDto
import com.ecovolt.app.data.remote.aTextoApi
import com.ecovolt.app.data.remote.toModel
import com.ecovolt.app.model.EstatusReporte
import com.ecovolt.app.model.Reporte
import com.ecovolt.app.model.TipoReporte
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64

// Repositorio de reportes (RFN-19). Los reportes viven en el servidor (los atiende el director),
// así que aquí no hay almacenamiento local: solo se envían y se consultan.
class ReporteRepository(private val api: EcoVoltApi) {

    // Fotos ya descargadas (id del reporte → bytes) para no pedirlas otra vez al volver a la lista.
    // Vive en memoria: se borra al cerrar la app.
    private val fotos = mutableMapOf<Int, ByteArray>()

    // Convierte la foto a Base64 (texto) para que viaje dentro del JSON y envía el reporte
    suspend fun enviar(tipo: TipoReporte, area: String, descripcion: String, foto: ByteArray): Result<Reporte> =
        try {
            val nuevo = NuevoReporteDto(
                tipo = tipo.aTextoApi(),
                area = area.trim(),
                descripcion = descripcion.trim(),
                fotoBase64 = Base64.Default.encode(foto)
            )
            val guardado = api.enviarReporte(nuevo).toModel()
            fotos[guardado.id] = foto // ya la tenemos: no hace falta descargarla después
            Result.success(guardado)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun reportes(): Result<List<Reporte>> =
        try {
            Result.success(api.obtenerReportes().map { it.toModel() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    // Solo director y responsable: el servidor revisa el rol de nuevo
    suspend fun cambiarEstatus(id: Int, estatus: EstatusReporte): Result<Reporte> =
        try {
            Result.success(api.cambiarEstatusReporte(id, estatus.textoApi).toModel())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    // Primero busca en las ya descargadas; si no está, la pide al servidor y la recuerda
    suspend fun foto(id: Int): Result<ByteArray> {
        fotos[id]?.let { return Result.success(it) }
        return try {
            val bytes = api.obtenerFotoReporte(id)
            fotos[id] = bytes
            Result.success(bytes)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

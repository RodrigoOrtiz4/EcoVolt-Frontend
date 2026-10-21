package com.ecovolt.app.data.repository

import com.ecovolt.app.data.local.LecturaStorage
import com.ecovolt.app.data.remote.EcoVoltApi
import com.ecovolt.app.data.remote.NuevaLecturaDto
import com.ecovolt.app.data.remote.aTextoApi
import com.ecovolt.app.data.remote.toModel
import com.ecovolt.app.model.Lectura
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.service.ConsumoService
import kotlin.coroutines.cancellation.CancellationException

// Repositorio: unifica la fuente remota (API) y la local (almacenamiento).
// La vista nunca habla directo con la API ni con el almacenamiento.
class LecturaRepository(
    private val api: EcoVoltApi,
    private val storage: LecturaStorage,
    private val consumoService: ConsumoService
) {

    // Lecturas guardadas en el dispositivo (funciona sin internet)
    fun lecturasGuardadas(): List<Lectura> = storage.obtener()

    // Envía al backend una lectura capturada con la cámara (POST /lecturas).
    // Si el servidor la acepta, también se agrega a las guardadas en el teléfono
    // para que aparezca de inmediato en "Ver lecturas", aun sin volver a sincronizar.
    suspend fun agregarLectura(momento: MomentoLectura, valorKwh: Double, fecha: String): Result<Lectura> =
        try {
            val guardada = api.enviarLectura(NuevaLecturaDto(fecha, momento.aTextoApi(), valorKwh)).toModel()
            storage.guardar(storage.obtener().filter { it.id != guardada.id } + guardada)
            Result.success(guardada)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    // Descarga de la API, guarda en local y devuelve el resultado
    suspend fun sincronizar(): Result<List<Lectura>> =
        try {
            val lecturas = api.obtenerLecturas().map { it.toModel() }
            storage.guardar(lecturas)
            Result.success(lecturas)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    // Toma la lectura de lunes más reciente y la de viernes anterior a ella
    fun consumoFinDeSemana(lecturas: List<Lectura>): Double? {
        val lunes = lecturas
            .filter { it.momento == MomentoLectura.LUNES_INICIO }
            .maxByOrNull { it.fecha } ?: return null
        val viernes = lecturas
            .filter { it.momento == MomentoLectura.VIERNES_CIERRE && it.fecha < lunes.fecha }
            .maxByOrNull { it.fecha } ?: return null
        return runCatching { consumoService.consumoFinDeSemana(viernes, lunes) }.getOrNull()
    }
}
package com.ecovolt.app.data.repository

import com.ecovolt.app.data.local.ConsejoStorage
import com.ecovolt.app.data.remote.EcoVoltApi
import com.ecovolt.app.data.remote.toModel
import com.ecovolt.app.model.Consejo
import kotlin.coroutines.cancellation.CancellationException

// Repositorio de consejos (RFN-25). Mismo esquema que LecturaRepository:
// lo guardado en el teléfono se muestra al instante y luego se actualiza desde el servidor.
class ConsejoRepository(
    private val api: EcoVoltApi,
    private val storage: ConsejoStorage
) {

    // Consejos guardados en el teléfono (funciona sin internet)
    fun consejosGuardados(): List<Consejo> = storage.obtener()

    // Descarga del servidor, guarda en el teléfono y devuelve el resultado
    suspend fun sincronizar(): Result<List<Consejo>> =
        try {
            val consejos = api.obtenerConsejos().map { it.toModel() }
            storage.guardar(consejos)
            Result.success(consejos)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}

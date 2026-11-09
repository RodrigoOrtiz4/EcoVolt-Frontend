package com.ecovolt.app.data.local

import android.content.Context
import com.ecovolt.app.config.AppConfig
import com.ecovolt.app.data.remote.ConsejoDto
import com.ecovolt.app.data.remote.toDto
import com.ecovolt.app.data.remote.toModel
import com.ecovolt.app.model.Consejo
import kotlinx.serialization.json.Json

// Código específico de Android: guarda los consejos como JSON en SharedPreferences
// (el mismo archivo que las lecturas, con otra clave).
class SharedPrefsConsejoStorage(context: Context) : ConsejoStorage {

    private val prefs = context.getSharedPreferences(AppConfig.STORAGE_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override fun guardar(consejos: List<Consejo>) {
        val texto = json.encodeToString(consejos.map { it.toDto() })
        prefs.edit().putString(KEY, texto).apply()
    }

    override fun obtener(): List<Consejo> {
        val texto = prefs.getString(KEY, null) ?: return emptyList()
        // Si lo guardado está dañado se ignora: se vuelven a descargar del servidor
        return try {
            json.decodeFromString<List<ConsejoDto>>(texto).map { it.toModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private companion object {
        const val KEY = "consejos"
    }
}

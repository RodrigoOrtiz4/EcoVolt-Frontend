package com.ecovolt.app.data.local

import android.content.Context
import com.ecovolt.app.model.Usuario
import kotlinx.serialization.json.Json

// Código específico de Android: recuerda la sesión con SharedPreferences (privado de la app).
// Guarda id, usuario, nombre, rol y el token del backend. Nunca la contraseña.
class SharedPrefsSesionStorage(context: Context) : SesionStorage {

    private val prefs = context.getSharedPreferences("ecovolt_sesion", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    override fun guardar(usuario: Usuario, token: String) {
        prefs.edit()
            .putString(KEY_USUARIO, json.encodeToString(usuario))
            .putString(KEY_TOKEN, token)
            .apply()
    }

    override fun obtener(): Usuario? {
        val texto = prefs.getString(KEY_USUARIO, null) ?: return null
        return try {
            json.decodeFromString<Usuario>(texto)
        } catch (e: Exception) {
            null
        }
    }

    override fun obtenerToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun cerrar() {
        prefs.edit().remove(KEY_USUARIO).remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val KEY_USUARIO = "usuario"
        const val KEY_TOKEN = "token"
    }
}

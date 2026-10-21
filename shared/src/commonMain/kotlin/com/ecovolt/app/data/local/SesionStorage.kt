package com.ecovolt.app.data.local

import com.ecovolt.app.model.Usuario

// Contrato para recordar la sesión en el dispositivo. La implementación depende de la plataforma.
// Además del usuario se guarda el token que entrega el backend para las rutas protegidas.
interface SesionStorage {
    fun guardar(usuario: Usuario, token: String)
    fun obtener(): Usuario?
    fun obtenerToken(): String?
    fun cerrar()
}

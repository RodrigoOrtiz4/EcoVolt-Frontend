package com.ecovolt.app.data.local

import com.ecovolt.app.model.Consejo

// Copia de los consejos en el teléfono para poder verlos sin internet.
// Igual que LecturaStorage: la app solo conoce esta interfaz y cada plataforma la implementa.
interface ConsejoStorage {
    fun guardar(consejos: List<Consejo>)
    fun obtener(): List<Consejo>
}

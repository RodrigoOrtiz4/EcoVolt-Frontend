package com.ecovolt.app

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import com.ecovolt.app.service.Compartidor
import java.io.File

// Código específico de Android para "Exportar" el historial: escribe el archivo en la carpeta
// temporal de la app y abre el menú del sistema para compartirlo (correo, WhatsApp, Drive...).
class AndroidCompartidor(private val actividad: ComponentActivity) : Compartidor {

    override fun compartirArchivo(nombreArchivo: String, tipoMime: String, contenido: String): String? =
        try {
            val carpeta = File(actividad.cacheDir, "exportes").apply { mkdirs() } // misma carpeta que file_paths.xml
            val archivo = File(carpeta, nombreArchivo)
            // El BOM (﻿) hace que Excel reconozca los acentos del CSV
            archivo.writeText("﻿" + contenido, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(actividad, "${actividad.packageName}.fileprovider", archivo)
            val intento = Intent(Intent.ACTION_SEND).apply {
                type = tipoMime
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Historial de lecturas - EcoVolt")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            actividad.startActivity(Intent.createChooser(intento, "Exportar historial"))
            null
        } catch (e: ActivityNotFoundException) {
            "No hay ninguna app para compartir el archivo."
        } catch (e: Exception) {
            "No se pudo exportar el historial."
        }
}

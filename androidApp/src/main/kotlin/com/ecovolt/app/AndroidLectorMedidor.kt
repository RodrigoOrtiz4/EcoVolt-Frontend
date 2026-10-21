package com.ecovolt.app

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.ecovolt.app.service.LectorMedidor
import com.ecovolt.app.service.ResultadoCaptura
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

// Código específico de Android: abre la cámara del sistema y reconoce el texto con ML Kit (sin internet).
// Se crea en MainActivity porque registrar el resultado de la cámara debe hacerse al iniciar la actividad.
class AndroidLectorMedidor(private val actividad: ComponentActivity) : LectorMedidor {

    private var alTerminar: ((ResultadoCaptura) -> Unit)? = null
    private var foto: File? = null
    private val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val lanzador = actividad.registerForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val callback = alTerminar
        val archivo = foto
        alTerminar = null
        if (callback == null || archivo == null) return@registerForActivityResult

        if (!tomada) {
            archivo.delete()
            callback(ResultadoCaptura.Cancelado)
        } else {
            reconocer(archivo, callback)
        }
    }

    override fun capturar(alTerminar: (ResultadoCaptura) -> Unit) {
        val carpeta = File(actividad.cacheDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "medidor_${System.currentTimeMillis()}.jpg")
        this.foto = archivo
        this.alTerminar = alTerminar

        val uri = FileProvider.getUriForFile(actividad, "${actividad.packageName}.fileprovider", archivo)
        try {
            lanzador.launch(uri)
        } catch (e: ActivityNotFoundException) {
            // El teléfono (o el emulador) no tiene una app de cámara instalada
            this.alTerminar = null
            archivo.delete()
            alTerminar(ResultadoCaptura.Error("No se encontró una app de cámara en este dispositivo."))
        }
    }

    private fun reconocer(archivo: File, callback: (ResultadoCaptura) -> Unit) {
        val cargada = cargarBitmap(archivo)
        if (cargada == null) {
            archivo.delete()
            callback(ResultadoCaptura.Error("No se pudo abrir la foto."))
            return
        }
        val (bitmap, grados) = cargada

        reconocedor.process(InputImage.fromBitmap(bitmap, grados))
            .addOnSuccessListener { resultado ->
                archivo.delete() // la foto solo se necesita para leer el número
                callback(ResultadoCaptura.Texto(resultado.text))
            }
            .addOnFailureListener {
                archivo.delete()
                callback(ResultadoCaptura.Error("No se pudo leer el texto de la foto."))
            }
    }

    // Reduce la foto (las de la cámara son enormes) y lee su orientación para que el OCR la vea derecha
    private fun cargarBitmap(archivo: File): Pair<Bitmap, Int>? {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(archivo.path, medidas)

        var reduccion = 1
        while (medidas.outWidth / reduccion > 2500 || medidas.outHeight / reduccion > 2500) reduccion *= 2

        val bitmap = BitmapFactory.decodeFile(
            archivo.path,
            BitmapFactory.Options().apply { inSampleSize = reduccion }
        ) ?: return null

        val grados = when (
            ExifInterface(archivo.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        return bitmap to grados
    }
}

package com.ecovolt.app

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.ecovolt.app.service.Foto
import com.ecovolt.app.service.ResultadoFoto
import com.ecovolt.app.service.SelectorFoto
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlin.math.max

// Código específico de Android para la foto de los reportes (RFN-19).
// Abre la cámara del sistema o el selector de fotos, y después reduce la imagen a 1280 px
// y la comprime en JPG (~150-300 KB) para que se envíe rápido aunque sea con datos móviles.
// Igual que AndroidLectorMedidor, se crea en MainActivity porque registrar los resultados
// debe hacerse al iniciar la actividad.
class AndroidSelectorFoto(private val actividad: ComponentActivity) : SelectorFoto {

    private var alTerminar: ((ResultadoFoto) -> Unit)? = null
    private var archivoCamara: File? = null

    // Cámara del sistema: guarda la foto en el archivo que le indicamos (no hace falta el permiso CAMERA)
    private val camara = actividad.registerForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val callback = alTerminar
        val archivo = archivoCamara
        alTerminar = null
        archivoCamara = null
        if (callback == null || archivo == null) return@registerForActivityResult

        if (!tomada) {
            archivo.delete()
            callback(ResultadoFoto.Cancelada)
        } else {
            // El archivo original (varios MB) se borra en cuanto se tiene la versión reducida
            procesarEnSegundoPlano(callback, alFinal = { archivo.delete() }) { archivo.inputStream() }
        }
    }

    // Selector de fotos del sistema: no necesita permisos de almacenamiento
    private val galeria = actividad.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val callback = alTerminar
        alTerminar = null
        if (callback == null) return@registerForActivityResult

        if (uri == null) {
            callback(ResultadoFoto.Cancelada)
        } else {
            procesarEnSegundoPlano(callback) { actividad.contentResolver.openInputStream(uri) }
        }
    }

    override fun tomarFoto(alTerminar: (ResultadoFoto) -> Unit) {
        val carpeta = File(actividad.cacheDir, "fotos").apply { mkdirs() } // misma carpeta que file_paths.xml
        val archivo = File(carpeta, "reporte_${System.currentTimeMillis()}.jpg")
        this.archivoCamara = archivo
        this.alTerminar = alTerminar

        val uri = FileProvider.getUriForFile(actividad, "${actividad.packageName}.fileprovider", archivo)
        try {
            camara.launch(uri)
        } catch (e: ActivityNotFoundException) {
            this.alTerminar = null
            this.archivoCamara = null
            archivo.delete()
            alTerminar(ResultadoFoto.Error("No se encontró una app de cámara en este dispositivo."))
        }
    }

    override fun elegirDeGaleria(alTerminar: (ResultadoFoto) -> Unit) {
        this.alTerminar = alTerminar
        try {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: ActivityNotFoundException) {
            this.alTerminar = null
            alTerminar(ResultadoFoto.Error("No se encontró una galería de fotos en este dispositivo."))
        }
    }

    // Reducir una foto de 12 megapíxeles tarda un poco: se hace en otro hilo para que la pantalla
    // no se congele, y el resultado se avisa de vuelta en el hilo principal (el de la interfaz).
    // `abrir` se llama varias veces porque un InputStream solo se puede leer una vez.
    private fun procesarEnSegundoPlano(
        callback: (ResultadoFoto) -> Unit,
        alFinal: () -> Unit = {},
        abrir: () -> InputStream?
    ) {
        Thread {
            val resultado = try {
                preparar(abrir)
            } catch (e: Exception) {
                ResultadoFoto.Error("No se pudo procesar la foto.")
            } catch (e: OutOfMemoryError) {
                ResultadoFoto.Error("La foto es demasiado grande. Intenta con otra.")
            } finally {
                alFinal()
            }
            actividad.runOnUiThread { callback(resultado) }
        }.start()
    }

    private fun preparar(abrir: () -> InputStream?): ResultadoFoto {
        // 1) Leer solo las medidas, sin cargar la imagen en memoria
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        (abrir() ?: return noSePudoAbrir()).use { BitmapFactory.decodeStream(it, null, medidas) }
        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) return noSePudoAbrir()

        // 2) Cargarla ya reducida a la mitad, a la cuarta parte... mientras siga siendo más grande de lo necesario
        var reduccion = 1
        while (max(medidas.outWidth, medidas.outHeight) / (reduccion * 2) >= LADO_MAXIMO) reduccion *= 2
        val cargada = (abrir() ?: return noSePudoAbrir()).use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = reduccion })
        } ?: return noSePudoAbrir()

        // 3) Enderezarla (el celular guarda en EXIF cómo estaba girado) y dejar su lado mayor en LADO_MAXIMO
        val grados = (abrir() ?: return noSePudoAbrir()).use { leerGrados(it) }
        val escala = minOf(1f, LADO_MAXIMO.toFloat() / max(cargada.width, cargada.height))
        val matriz = Matrix().apply {
            postScale(escala, escala)
            postRotate(grados.toFloat())
        }
        val lista = Bitmap.createBitmap(cargada, 0, 0, cargada.width, cargada.height, matriz, true)

        // 4) Comprimir en JPG
        val salida = ByteArrayOutputStream()
        lista.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPG, salida)
        if (lista !== cargada) lista.recycle() // createBitmap puede devolver la misma si no hubo cambios
        cargada.recycle()
        return ResultadoFoto.Obtenida(Foto(salida.toByteArray()))
    }

    private fun leerGrados(entrada: InputStream): Int =
        try {
            when (ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0 // algunas imágenes (por ejemplo PNG) no traen esa información
        }

    private fun noSePudoAbrir() = ResultadoFoto.Error("No se pudo abrir la foto.")

    private companion object {
        const val LADO_MAXIMO = 1280   // píxeles del lado más largo
        const val CALIDAD_JPG = 75     // 0-100: buen equilibrio entre nitidez y peso
    }
}

package com.ecovolt.app.service

import androidx.compose.ui.graphics.ImageBitmap

// Convierte los bytes de un JPG o PNG en una imagen que Compose puede dibujar con Image().
// Cómo se decodifica depende de la plataforma (igual que fechaHoy): ver Imagen.android.kt.
// Devuelve null si los bytes no son una imagen válida.
expect fun ByteArray.aImagen(): ImageBitmap?

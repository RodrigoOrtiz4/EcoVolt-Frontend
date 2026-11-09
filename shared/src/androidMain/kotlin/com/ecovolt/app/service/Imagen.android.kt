package com.ecovolt.app.service

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

// Código específico de Android: BitmapFactory lee el JPG/PNG y asImageBitmap() lo pasa a Compose
actual fun ByteArray.aImagen(): ImageBitmap? =
    BitmapFactory.decodeByteArray(this, 0, size)?.asImageBitmap()

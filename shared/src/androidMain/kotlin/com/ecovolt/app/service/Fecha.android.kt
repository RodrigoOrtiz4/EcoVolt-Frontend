package com.ecovolt.app.service

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Código específico de Android
actual fun fechaHoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

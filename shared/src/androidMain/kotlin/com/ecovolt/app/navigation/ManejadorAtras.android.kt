package com.ecovolt.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

// Código específico de Android: BackHandler intercepta el botón o gesto de "atrás"
@Composable
actual fun ManejadorAtras(habilitado: Boolean, alPresionar: () -> Unit) {
    BackHandler(enabled = habilitado, onBack = alPresionar)
}

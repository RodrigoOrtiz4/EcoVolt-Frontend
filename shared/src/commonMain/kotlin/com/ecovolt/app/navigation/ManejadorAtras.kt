package com.ecovolt.app.navigation

import androidx.compose.runtime.Composable

// Botón "atrás" del sistema. En Android regresa a la pantalla anterior de la app
// en lugar de cerrarla. Cada plataforma lo implementa a su manera.
@Composable
expect fun ManejadorAtras(habilitado: Boolean, alPresionar: () -> Unit)

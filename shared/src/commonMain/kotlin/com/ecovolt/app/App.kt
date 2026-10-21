package com.ecovolt.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ecovolt.app.data.local.LecturaStorage
import com.ecovolt.app.data.local.SesionStorage
import com.ecovolt.app.data.remote.AuthApi
import com.ecovolt.app.data.remote.EcoVoltApi
import com.ecovolt.app.data.remote.createHttpClient
import com.ecovolt.app.data.repository.AuthRepository
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.navigation.AppNavHost
import com.ecovolt.app.service.ConsumoService
import com.ecovolt.app.service.LectorMedidor
import com.ecovolt.app.ui.theme.EcoVoltTheme

// Raíz de la UI compartida. Los almacenamientos y la cámara llegan desde fuera porque
// su implementación es específica de cada plataforma.
@Composable
fun App(storage: LecturaStorage, sesionStorage: SesionStorage, lector: LectorMedidor) {
    EcoVoltTheme {
        val client = remember { createHttpClient() }
        val lecturaRepository = remember {
            LecturaRepository(
                // Las lecturas piden el token de la sesión en cada consulta
                api = EcoVoltApi(client, tokenProvider = { sesionStorage.obtenerToken() }),
                storage = storage,
                consumoService = ConsumoService()
            )
        }
        val authRepository = remember { AuthRepository(AuthApi(client), sesionStorage) }
        AppNavHost(lecturaRepository, authRepository, lector)
    }
}

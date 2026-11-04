package com.ecovolt.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ecovolt.app.data.local.ConsejoStorage
import com.ecovolt.app.data.local.LecturaStorage
import com.ecovolt.app.data.local.SesionStorage
import com.ecovolt.app.data.remote.AuthApi
import com.ecovolt.app.data.remote.EcoVoltApi
import com.ecovolt.app.data.remote.createHttpClient
import com.ecovolt.app.data.repository.AuthRepository
import com.ecovolt.app.data.repository.ConsejoRepository
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.data.repository.ReporteRepository
import com.ecovolt.app.navigation.AppNavHost
import com.ecovolt.app.service.ConsumoService
import com.ecovolt.app.service.LectorMedidor
import com.ecovolt.app.service.SelectorFoto
import com.ecovolt.app.ui.theme.EcoVoltTheme

// Raíz de la UI compartida. Los almacenamientos, la cámara del medidor y el selector de fotos
// llegan desde fuera porque su implementación es específica de cada plataforma.
@Composable
fun App(
    storage: LecturaStorage,
    sesionStorage: SesionStorage,
    lector: LectorMedidor,
    selectorFoto: SelectorFoto,
    consejoStorage: ConsejoStorage
) {
    EcoVoltTheme {
        val client = remember { createHttpClient() }
        // Una sola API para lecturas, reportes y consejos. Pide el token de la sesión en cada consulta.
        val api = remember { EcoVoltApi(client, tokenProvider = { sesionStorage.obtenerToken() }) }
        val lecturaRepository = remember {
            LecturaRepository(
                api = api,
                storage = storage,
                consumoService = ConsumoService()
            )
        }
        val authRepository = remember { AuthRepository(AuthApi(client), sesionStorage) }
        val reporteRepository = remember { ReporteRepository(api) }
        val consejoRepository = remember { ConsejoRepository(api, consejoStorage) }
        AppNavHost(lecturaRepository, authRepository, lector, reporteRepository, consejoRepository, selectorFoto)
    }
}

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
import com.ecovolt.app.navigation.Dependencias
import com.ecovolt.app.service.Compartidor
import com.ecovolt.app.service.ConsumoService
import com.ecovolt.app.service.LectorMedidor
import com.ecovolt.app.service.SelectorFoto
import com.ecovolt.app.ui.theme.EcoVoltTheme
import kotlinx.coroutines.flow.MutableStateFlow

// Raíz de la UI compartida. Los almacenamientos, la cámara del medidor, el selector de fotos y
// el compartidor llegan desde fuera porque su implementación es específica de cada plataforma.
@Composable
fun App(
    storage: LecturaStorage,
    sesionStorage: SesionStorage,
    lector: LectorMedidor,
    selectorFoto: SelectorFoto,
    consejoStorage: ConsejoStorage,
    compartidor: Compartidor
) {
    EcoVoltTheme {
        // Se enciende cuando el servidor rechaza el token; la navegación regresa al login
        val sesionVencida = remember { MutableStateFlow(false) }
        val dependencias = remember {
            val client = createHttpClient()
            // Una sola API para lecturas, reportes y consejos. Pide el token de la sesión en cada consulta.
            val api = EcoVoltApi(
                client = client,
                tokenProvider = { sesionStorage.obtenerToken() },
                alSesionVencida = { if (sesionStorage.obtenerToken() != null) sesionVencida.value = true }
            )
            Dependencias(
                lecturas = LecturaRepository(api = api, storage = storage, consumoService = ConsumoService()),
                auth = AuthRepository(AuthApi(client), sesionStorage),
                reportes = ReporteRepository(api),
                consejos = ConsejoRepository(api, consejoStorage),
                lector = lector,
                selectorFoto = selectorFoto,
                compartidor = compartidor
            )
        }
        AppNavHost(
            d = dependencias,
            sesionVencida = sesionVencida,
            alAtenderSesionVencida = { sesionVencida.value = false }
        )
    }
}

package com.ecovolt.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecovolt.app.data.repository.AuthRepository
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.service.LectorMedidor
import com.ecovolt.app.ui.captura.CapturaScreen
import com.ecovolt.app.ui.captura.CapturaViewModel
import com.ecovolt.app.ui.inicio.InicioScreen
import com.ecovolt.app.ui.lecturas.LecturasScreen
import com.ecovolt.app.ui.lecturas.LecturasViewModel
import com.ecovolt.app.ui.login.LoginScreen
import com.ecovolt.app.ui.login.LoginViewModel
import com.ecovolt.app.ui.registro.RegistroScreen
import com.ecovolt.app.ui.registro.RegistroViewModel

// Navegación compartida: si ya hay una sesión guardada entra directo, si no pide login (o registro).
@Composable
fun AppNavHost(
    lecturaRepository: LecturaRepository,
    authRepository: AuthRepository,
    lector: LectorMedidor
) {
    var usuario by remember { mutableStateOf(authRepository.sesionActual()) }
    var pantalla by remember {
        mutableStateOf<Screen>(if (usuario == null) Screen.Login else Screen.Inicio)
    }

    when (pantalla) {
        Screen.Login -> {
            val viewModel = viewModel { LoginViewModel(authRepository) }
            LoginScreen(
                viewModel = viewModel,
                alIrARegistro = { pantalla = Screen.Registro }
            ) { autenticado ->
                usuario = autenticado
                pantalla = Screen.Inicio
            }
        }

        Screen.Registro -> {
            val viewModel = viewModel { RegistroViewModel(authRepository) }
            RegistroScreen(
                viewModel = viewModel,
                alVolverALogin = {
                    viewModel.limpiar()
                    pantalla = Screen.Login
                }
            ) { registrado ->
                usuario = registrado
                pantalla = Screen.Inicio
            }
        }

        Screen.Inicio -> {
            val actual = usuario
            if (actual != null) {
                InicioScreen(
                    usuario = actual,
                    alVerLecturas = { pantalla = Screen.Lecturas },
                    alCapturar = { pantalla = Screen.Captura },
                    alCerrarSesion = {
                        authRepository.cerrarSesion()
                        usuario = null
                        pantalla = Screen.Login
                    }
                )
            }
        }

        Screen.Lecturas -> {
            val viewModel = viewModel { LecturasViewModel(lecturaRepository) }
            Box(Modifier.fillMaxSize()) {
                LecturasScreen(viewModel)
                TextButton(
                    onClick = { pantalla = Screen.Inicio },
                    modifier = Modifier.align(Alignment.TopEnd).safeContentPadding()
                ) { Text("Volver") }
            }
        }

        Screen.Captura -> {
            val viewModel = viewModel { CapturaViewModel(lector, lecturaRepository) }
            CapturaScreen(viewModel) { pantalla = Screen.Inicio }
        }
    }
}

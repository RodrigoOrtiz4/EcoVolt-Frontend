package com.ecovolt.app.ui.registro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.BotonPrincipal
import com.ecovolt.app.ui.components.CampoTexto
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.theme.EcoColores

// Registro de alumnos. Toda cuenta nueva es de ALUMNO: el servidor decide el rol.
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    alVolverALogin: () -> Unit,
    alRegistrar: (Usuario) -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(titulo = "Crear cuenta", subtitulo = "Cuenta de alumno", alVolver = alVolverALogin)

        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text("Únete a EcoVolt", style = MaterialTheme.typography.headlineLarge, color = EcoColores.Texto)
            Spacer(Modifier.height(6.dp))
            Text(
                "Reporta desperdicios de energía en tu escuela y aprende a ahorrar también en casa.",
                style = MaterialTheme.typography.bodyLarge,
                color = EcoColores.TextoSuave
            )

            estado.errorGeneral?.let {
                Spacer(Modifier.height(18.dp))
                Aviso(TipoAviso.ERROR, "No pudimos crear tu cuenta", it)
            }

            Spacer(Modifier.height(22.dp))
            CampoTexto(
                valor = estado.nombre,
                alCambiar = viewModel::alCambiarNombre,
                etiqueta = "Nombre completo",
                placeholder = "Ej. Ana López",
                icono = EcoIconos.User,
                error = estado.errorNombre,
                capitalizacion = KeyboardCapitalization.Words,
                habilitado = !estado.cargando
            )
            Spacer(Modifier.height(18.dp))
            CampoTexto(
                valor = estado.usuario,
                alCambiar = viewModel::alCambiarUsuario,
                etiqueta = "Usuario",
                placeholder = "Ej. ana.lopez",
                icono = EcoIconos.UserPlus,
                error = estado.errorUsuario,
                ayuda = "De 4 a 20 caracteres: letras, números, punto o guion bajo.",
                habilitado = !estado.cargando
            )
            Spacer(Modifier.height(18.dp))
            CampoTexto(
                valor = estado.contrasena,
                alCambiar = viewModel::alCambiarContrasena,
                etiqueta = "Contraseña",
                placeholder = "Mínimo 6 caracteres",
                icono = EcoIconos.Lock,
                error = estado.errorContrasena,
                esContrasena = true,
                mostrarContrasena = estado.mostrarContrasena,
                alAlternarContrasena = viewModel::alternarContrasena,
                teclado = KeyboardType.Password,
                habilitado = !estado.cargando
            )
            Spacer(Modifier.height(18.dp))
            CampoTexto(
                valor = estado.confirmacion,
                alCambiar = viewModel::alCambiarConfirmacion,
                etiqueta = "Confirmar contraseña",
                placeholder = "Repite tu contraseña",
                icono = EcoIconos.Lock,
                error = estado.errorConfirmacion,
                esContrasena = true,
                mostrarContrasena = estado.mostrarContrasena,
                alAlternarContrasena = viewModel::alternarContrasena,
                teclado = KeyboardType.Password,
                accionIme = ImeAction.Done,
                alAccionIme = { viewModel.registrar(alRegistrar) },
                habilitado = !estado.cargando
            )

            Spacer(Modifier.height(26.dp))
            BotonPrincipal(
                texto = "Crear cuenta",
                icono = EcoIconos.UserPlus,
                cargando = estado.cargando,
                alPulsar = { viewModel.registrar(alRegistrar) }
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Ya tienes cuenta?", style = MaterialTheme.typography.bodyLarge, color = EcoColores.TextoSuave)
                TextButton(onClick = alVolverALogin, enabled = !estado.cargando) {
                    Text("Inicia sesión", style = MaterialTheme.typography.titleMedium, color = EcoColores.Verde)
                }
            }
        }
    }
}

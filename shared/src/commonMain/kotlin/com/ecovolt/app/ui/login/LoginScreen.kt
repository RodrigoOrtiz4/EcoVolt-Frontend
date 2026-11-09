package com.ecovolt.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.Rol
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.BotonPrincipal
import com.ecovolt.app.ui.components.BotonTexto
import com.ecovolt.app.ui.components.CampoTexto
import com.ecovolt.app.ui.components.ChipOpcion
import com.ecovolt.app.ui.components.ColoresChip
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.theme.EcoColores

// Roles que entran por "Acceso de adultos", cada uno con el color de su chip en Figma
private val ROLES_ADULTOS = listOf(
    Rol.RESPONSABLE to ColoresChip.Verde,
    Rol.DIRECTOR to ColoresChip.Azul,
    Rol.DOCENTE to ColoresChip.Morado
)

private fun ejemploUsuario(rol: Rol, personal: Boolean): String = when {
    !personal -> "Ej. alumno1"
    rol == Rol.DIRECTOR -> "Ej. director"
    rol == Rol.DOCENTE -> "Ej. docente1"
    else -> "Ej. responsable.lectura"
}

// Inicio de sesión. `personal` = acceso de adultos (responsable, director, docente);
// si es false es el acceso de alumnos, que además permite crear una cuenta.
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    personal: Boolean,
    aviso: String?,
    alVolver: () -> Unit,
    alIrARegistro: () -> Unit,
    alEntrar: (Usuario) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarAyuda by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(
            titulo = if (personal) "Acceso de adultos" else "Acceso de alumnos",
            alVolver = alVolver
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text("Bienvenido de nuevo", style = MaterialTheme.typography.headlineLarge, color = EcoColores.Texto)
            Spacer(Modifier.height(6.dp))
            Text(
                if (personal) "Ingresa con la cuenta asignada por la dirección." else "Entra con el usuario que creaste en EcoVolt.",
                style = MaterialTheme.typography.bodyLarge,
                color = EcoColores.TextoSuave
            )

            if (personal) {
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ROLES_ADULTOS.forEach { (rol, colores) ->
                        ChipOpcion(
                            texto = if (rol == Rol.RESPONSABLE) "Responsable" else rol.etiqueta,
                            seleccionado = estado.rolElegido == rol,
                            alPulsar = { viewModel.elegirRol(rol) },
                            colores = colores
                        )
                    }
                }
            }

            if (aviso != null && estado.errorGeneral == null) {
                Spacer(Modifier.height(18.dp))
                Aviso(TipoAviso.INFO, "Vuelve a iniciar sesión", aviso)
            }
            estado.errorGeneral?.let { error ->
                Spacer(Modifier.height(18.dp))
                Aviso(TipoAviso.ERROR, error.titulo, error.texto)
            }

            Spacer(Modifier.height(22.dp))
            CampoTexto(
                valor = estado.usuario,
                alCambiar = viewModel::alCambiarUsuario,
                etiqueta = "Usuario",
                placeholder = ejemploUsuario(estado.rolElegido, personal),
                icono = EcoIconos.User,
                error = estado.errorUsuario,
                habilitado = !estado.cargando
            )
            Spacer(Modifier.height(18.dp))
            CampoTexto(
                valor = estado.contrasena,
                alCambiar = viewModel::alCambiarContrasena,
                etiqueta = "Contraseña",
                placeholder = "Tu contraseña",
                icono = EcoIconos.Lock,
                error = estado.errorContrasena,
                esContrasena = true,
                mostrarContrasena = estado.mostrarContrasena,
                alAlternarContrasena = viewModel::alternarContrasena,
                teclado = KeyboardType.Password,
                accionIme = ImeAction.Done,
                alAccionIme = { viewModel.iniciarSesion(alEntrar) },
                habilitado = !estado.cargando
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { mostrarAyuda = true }) {
                    Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.titleSmall, color = EcoColores.Verde)
                }
            }
            Spacer(Modifier.height(12.dp))
            BotonPrincipal(
                texto = "Iniciar sesión",
                icono = EcoIconos.LogIn,
                cargando = estado.cargando,
                alPulsar = { viewModel.iniciarSesion(alEntrar) }
            )

            if (!personal) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyLarge, color = EcoColores.TextoSuave)
                    TextButton(onClick = alIrARegistro, enabled = !estado.cargando) {
                        Text("Crear cuenta", style = MaterialTheme.typography.titleMedium, color = EcoColores.Verde)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            BotonTexto("Volver", alPulsar = alVolver, habilitado = !estado.cargando)
        }
    }

    if (mostrarAyuda) {
        AlertDialog(
            onDismissRequest = { mostrarAyuda = false },
            confirmButton = {
                TextButton(onClick = { mostrarAyuda = false }) {
                    Text("Entendido", style = MaterialTheme.typography.titleMedium, color = EcoColores.Verde)
                }
            },
            title = { Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    if (personal) {
                        "Pide a la dirección de tu escuela que restablezca tu contraseña. " +
                            "Cuando entres, cámbiala desde Perfil → Cambiar contraseña."
                    } else {
                        "Pide a tu docente o a la dirección que te ayuden a recuperar tu cuenta."
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            containerColor = EcoColores.Superficie
        )
    }
}

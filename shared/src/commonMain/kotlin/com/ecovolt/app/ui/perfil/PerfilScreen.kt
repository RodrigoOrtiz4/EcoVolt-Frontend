package com.ecovolt.app.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.Rol
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.Etiqueta
import com.ecovolt.app.ui.components.FilaInfo
import com.ecovolt.app.ui.components.Separador
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.components.TituloSeccion
import com.ecovolt.app.ui.theme.EcoColores

// Colores de la etiqueta de cada rol (los mismos de los chips del login)
fun coloresRol(rol: Rol): Pair<Color, Color> = when (rol) {
    Rol.RESPONSABLE -> EcoColores.VerdeClaro to EcoColores.Verde
    Rol.DIRECTOR -> EcoColores.AzulClaro to EcoColores.Azul
    Rol.DOCENTE -> EcoColores.MoradoClaro to EcoColores.Morado
    Rol.ALUMNO -> EcoColores.AmarilloClaro to EcoColores.Ambar
}

// "Ana López" → "AL"
private fun iniciales(nombre: String): String =
    nombre.trim().split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

@Composable
fun PerfilScreen(
    usuario: Usuario,
    alCambiarContrasena: () -> Unit,
    alVerConsejos: (() -> Unit)?,
    alCerrarSesion: () -> Unit
) {
    var confirmarSalida by remember { mutableStateOf(false) }
    var verAcercaDe by remember { mutableStateOf(false) }
    val (fondoRol, tintaRol) = coloresRol(usuario.rol)
    val flecha: @Composable () -> Unit = { Icon(EcoIconos.ChevronRight, contentDescription = null, tint = EcoColores.TextoSuave) }

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(titulo = "Perfil", subtitulo = "Tu cuenta de EcoVolt")
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Tarjeta {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(88.dp).clip(CircleShape).background(fondoRol),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iniciales(usuario.nombre), style = MaterialTheme.typography.headlineLarge, color = tintaRol)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(usuario.nombre, style = MaterialTheme.typography.headlineSmall, color = EcoColores.Texto)
                    Text("@${usuario.usuario}", style = MaterialTheme.typography.bodyLarge, color = EcoColores.TextoSuave)
                    Spacer(Modifier.height(10.dp))
                    Etiqueta(usuario.rol.etiqueta, fondoRol, tintaRol)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        usuario.rol.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EcoColores.TextoSuave
                    )
                }
            }

            TituloSeccion("Cuenta")
            Tarjeta(relleno = 12.dp) {
                FilaInfo(
                    icono = EcoIconos.KeyRound,
                    fondoIcono = EcoColores.VerdeClaro,
                    tinteIcono = EcoColores.Verde,
                    titulo = "Cambiar contraseña",
                    subtitulo = "Mínimo 8 caracteres, una mayúscula y un número",
                    alPulsar = alCambiarContrasena,
                    alFinal = flecha
                )
                if (alVerConsejos != null) {
                    Separador(Modifier.padding(vertical = 2.dp))
                    FilaInfo(
                        icono = EcoIconos.Lightbulb,
                        fondoIcono = EcoColores.AmarilloClaro,
                        tinteIcono = EcoColores.Ambar,
                        titulo = "Consejos de ahorro",
                        subtitulo = "Para la escuela y el hogar",
                        alPulsar = alVerConsejos,
                        alFinal = flecha
                    )
                }
                Separador(Modifier.padding(vertical = 2.dp))
                FilaInfo(
                    icono = EcoIconos.CircleHelp,
                    fondoIcono = EcoColores.AzulClaro,
                    tinteIcono = EcoColores.Azul,
                    titulo = "Acerca de EcoVolt",
                    subtitulo = "Versión 1.0",
                    alPulsar = { verAcercaDe = true },
                    alFinal = flecha
                )
            }

            Tarjeta(relleno = 12.dp) {
                FilaInfo(
                    icono = EcoIconos.LogOut,
                    fondoIcono = EcoColores.RojoClaro,
                    tinteIcono = EcoColores.Rojo,
                    titulo = "Cerrar sesión",
                    colorTitulo = EcoColores.Rojo,
                    alPulsar = { confirmarSalida = true }
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Tendrás que volver a escribir tu usuario y contraseña para entrar.", style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    alCerrarSesion()
                }) {
                    Text("Cerrar sesión", style = MaterialTheme.typography.titleMedium, color = EcoColores.Rojo)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Cancelar", style = MaterialTheme.typography.titleMedium, color = EcoColores.TextoSuave)
                }
            },
            containerColor = EcoColores.Superficie
        )
    }

    if (verAcercaDe) {
        AlertDialog(
            onDismissRequest = { verAcercaDe = false },
            title = { Text("EcoVolt ⚡", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "Sistema de gestión y ahorro de energía para escuelas: lecturas del medidor con la cámara, " +
                        "reportes de desperdicio y consejos de ahorro.\n\nVersión 1.0",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { verAcercaDe = false }) {
                    Text("Cerrar", style = MaterialTheme.typography.titleMedium, color = EcoColores.Verde)
                }
            },
            containerColor = EcoColores.Superficie
        )
    }
}

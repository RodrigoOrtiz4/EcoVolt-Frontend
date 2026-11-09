package com.ecovolt.app.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.Rol
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.service.formatoKwh
import com.ecovolt.app.service.formatoNumero
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.Etiqueta
import com.ecovolt.app.ui.components.FilaInfo
import com.ecovolt.app.ui.components.GraficaBarras
import com.ecovolt.app.ui.components.Separador
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.components.TituloSeccion
import com.ecovolt.app.ui.historial.HistorialViewModel
import com.ecovolt.app.ui.historial.barrasDeConsumo
import com.ecovolt.app.ui.theme.EcoColores

// Inicio de alumnos y docentes: cuánto gastó la escuela, si hubo consumo fantasma
// y accesos rápidos para reportar y aprender a ahorrar.
@Composable
fun InicioScreen(
    usuario: Usuario,
    viewModel: HistorialViewModel,
    alReportar: () -> Unit,
    alVerConsejos: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    LaunchedEffect(Unit) { viewModel.cargar() }
    val barras = barrasDeConsumo(estado.visibles).takeLast(6)
    val flecha: @Composable () -> Unit = { Icon(EcoIconos.ChevronRight, contentDescription = null, tint = EcoColores.TextoSuave) }

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(
            titulo = "Hola, ${usuario.nombre.substringBefore(' ')}",
            subtitulo = usuario.rol.etiqueta
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Tarjeta verde con el consumo del último fin de semana
            Tarjeta(fondo = EcoColores.Verde, borde = EcoColores.Verde) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Consumo del último fin de semana",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(EcoIconos.ZapDelgado, contentDescription = null, tint = EcoColores.Amarillo, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.height(8.dp))
                val consumo = estado.consumoFinDeSemana
                Text(
                    consumo?.let { formatoKwh(it) } ?: "Sin datos aún",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (consumo == null) "Se calcula con el cierre del viernes y la apertura del lunes."
                    else "Energía usada con la escuela cerrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                if (estado.consumoFantasma) {
                    Spacer(Modifier.height(12.dp))
                    Etiqueta("Consumo fantasma", EcoColores.Amarillo, EcoColores.Ambar)
                }
            }

            if (estado.consumoFantasma && estado.consumoFinDeSemana != null) {
                Aviso(
                    TipoAviso.ADVERTENCIA,
                    "Algo se quedó encendido",
                    "Con la escuela cerrada se gastaron ${formatoNumero(estado.consumoFinDeSemana ?: 0.0)} kWh. " +
                        "Si ves equipos encendidos sin uso, repórtalos.",
                    icono = EcoIconos.TriangleAlert
                )
            }

            if (barras.isNotEmpty()) {
                Tarjeta(relleno = 16.dp) {
                    Text("Consumo por periodo (kWh)", style = MaterialTheme.typography.titleSmall, color = EcoColores.TextoSuave)
                    Spacer(Modifier.height(12.dp))
                    GraficaBarras(barras = barras, textoValor = { formatoNumero(it) })
                }
            }

            TituloSeccion("¿Qué quieres hacer?")
            Tarjeta(relleno = 12.dp) {
                FilaInfo(
                    icono = EcoIconos.CircleAlert,
                    fondoIcono = EcoColores.RojoClaro,
                    tinteIcono = EcoColores.Rojo,
                    titulo = "Reportar una situación",
                    subtitulo = "Luces o equipos encendidos, o algo descompuesto",
                    alPulsar = alReportar,
                    alFinal = flecha
                )
                Separador(Modifier.padding(vertical = 2.dp))
                FilaInfo(
                    icono = EcoIconos.Lightbulb,
                    fondoIcono = EcoColores.AmarilloClaro,
                    tinteIcono = EcoColores.Ambar,
                    titulo = "Consejos de ahorro",
                    subtitulo = "Para la escuela y para tu casa",
                    alPulsar = alVerConsejos,
                    alFinal = flecha
                )
            }

            Tarjeta(relleno = 12.dp) {
                FilaInfo(
                    icono = if (usuario.rol == Rol.ALUMNO) EcoIconos.GraduationCap else EcoIconos.School,
                    fondoIcono = EcoColores.VerdeClaro,
                    tinteIcono = EcoColores.Verde,
                    titulo = "Tu papel en EcoVolt",
                    subtitulo = usuario.rol.descripcion
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

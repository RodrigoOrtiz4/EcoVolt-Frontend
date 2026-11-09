package com.ecovolt.app.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.FilaInfo
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.theme.EcoColores

// Primera pantalla sin sesión: se elige si entra el personal de la escuela o un alumno.
// Los adultos usan la cuenta que les dio la dirección; los alumnos pueden crear la suya.
@Composable
fun BienvenidaScreen(alEntrarPersonal: () -> Unit, alEntrarAlumno: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(EcoColores.Fondo)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(EcoColores.Verde)
        ) {
            Icon(
                EcoIconos.SunDelgado,
                contentDescription = null,
                tint = EcoColores.Amarillo,
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 20.dp, top = 8.dp).size(84.dp)
            )
            Icon(
                EcoIconos.LeafDelgado,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.18f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 18.dp).size(96.dp)
            )
            Column(Modifier.statusBarsPadding().padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 36.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoEcoVolt(tamano = 52.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("EcoVolt", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                }
                Spacer(Modifier.height(36.dp))
                Text(
                    "Ahorrar energía empieza en tu escuela",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Registra el consumo del medidor, reporta desperdicios y aprende a cuidar la energía.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }
        }

        Column(Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 28.dp)) {
            Text("¿Cómo quieres entrar?", style = MaterialTheme.typography.headlineSmall, color = EcoColores.Texto)
            Spacer(Modifier.height(16.dp))
            Tarjeta(relleno = 12.dp) {
                FilaInfo(
                    icono = EcoIconos.School,
                    fondoIcono = EcoColores.VerdeClaro,
                    tinteIcono = EcoColores.Verde,
                    titulo = "Personal de la escuela",
                    subtitulo = "Responsable de lecturas, director o docente",
                    alPulsar = alEntrarPersonal,
                    alFinal = { Icon(EcoIconos.ChevronRight, null, tint = EcoColores.TextoSuave) }
                )
            }
            Spacer(Modifier.height(12.dp))
            Tarjeta(relleno = 12.dp) {
                FilaInfo(
                    icono = EcoIconos.GraduationCap,
                    fondoIcono = EcoColores.AzulClaro,
                    tinteIcono = EcoColores.Azul,
                    titulo = "Soy alumno",
                    subtitulo = "Inicia sesión o crea tu cuenta",
                    alPulsar = alEntrarAlumno,
                    alFinal = { Icon(EcoIconos.ChevronRight, null, tint = EcoColores.TextoSuave) }
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Las cuentas de responsable, director y docente las crea la dirección de la escuela.",
                style = MaterialTheme.typography.bodySmall,
                color = EcoColores.TextoSuave
            )
        }
    }
}

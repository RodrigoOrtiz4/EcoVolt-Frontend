package com.ecovolt.app.ui.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.Reporte
import com.ecovolt.app.ui.components.BotonPrincipal
import com.ecovolt.app.ui.components.BotonTexto
import com.ecovolt.app.ui.components.CabeceraResultado
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.FilaInfo
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.theme.EcoColores

// "¡Reporte enviado!" con el folio para darle seguimiento
@Composable
fun ReporteEnviadoScreen(reporte: Reporte, alVerReportes: () -> Unit, alVolverAlInicio: () -> Unit) {
    val (fondoEstatus, tintaEstatus) = coloresEstatus(reporte.estatus)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoColores.Fondo)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        CabeceraResultado(
            icono = EcoIconos.ClipboardCheck,
            titulo = "¡Reporte enviado!",
            texto = "Dirección recibió la situación y podrá actualizar su estatus."
        )
        Spacer(Modifier.height(20.dp))
        Tarjeta(fondo = EcoColores.VerdeClaro, borde = EcoColores.VerdeBorde, relleno = 16.dp) {
            FilaInfo(EcoIconos.Hash, EcoColores.VerdeClaro, EcoColores.Verde, "Folio", valor = reporte.folio)
            FilaInfo(EcoIconos.MapPin, EcoColores.AzulClaro, EcoColores.Azul, "Área", valor = reporte.area)
            FilaInfo(EcoIconos.Clock, fondoEstatus, tintaEstatus, "Estatus", valor = reporte.estatus.etiqueta)
        }
        Spacer(Modifier.height(24.dp))
        BotonPrincipal("Ver mis reportes", alPulsar = alVerReportes, icono = EcoIconos.ClipboardList)
        BotonTexto("Volver al inicio", alPulsar = alVolverAlInicio)
    }
}

package com.ecovolt.app.ui.consejos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecovolt.app.model.AmbitoConsejo
import com.ecovolt.app.model.Consejo
import com.ecovolt.app.service.formatoNumero
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.ChipOpcion
import com.ecovolt.app.ui.components.ColoresChip
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.Etiqueta
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.theme.EcoColores

// Consejos de ahorro para la escuela y el hogar (RFN-25).
// Puede abrirse como pestaña (sin botón de volver) o desde Perfil (con `alVolver`).
@Composable
fun ConsejosScreen(viewModel: ConsejosViewModel, alVolver: (() -> Unit)? = null) {
    val estado by viewModel.estado.collectAsState()
    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(
            titulo = "Consejos de ahorro",
            subtitulo = "Pequeños hábitos, gran diferencia",
            alVolver = alVolver,
            accion = if (estado.cargando) {
                { CircularProgressIndicator(color = EcoColores.Verde, strokeWidth = 3.dp, modifier = Modifier.size(28.dp)) }
            } else {
                null
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f).then(if (alVolver != null) Modifier.navigationBarsPadding() else Modifier),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChipOpcion(
                        texto = "Escuela",
                        seleccionado = estado.ambito == AmbitoConsejo.ESCUELA,
                        alPulsar = { viewModel.elegirAmbito(AmbitoConsejo.ESCUELA) },
                        colores = ColoresChip.Verde,
                        icono = EcoIconos.School
                    )
                    ChipOpcion(
                        texto = "Hogar",
                        seleccionado = estado.ambito == AmbitoConsejo.HOGAR,
                        alPulsar = { viewModel.elegirAmbito(AmbitoConsejo.HOGAR) },
                        colores = ColoresChip.Azul,
                        icono = EcoIconos.House
                    )
                }
            }

            // Recomendación personalizada con las lecturas del medidor
            val consumo = estado.consumoFinDeSemana
            if (estado.consumoFantasma && consumo != null && estado.ambito == AmbitoConsejo.ESCUELA) {
                item {
                    Aviso(
                        TipoAviso.ADVERTENCIA,
                        "Consumo fantasma el fin de semana",
                        "Con la escuela cerrada se gastaron ${formatoNumero(consumo)} kWh. " +
                            "Empieza por los consejos de equipos en espera.",
                        icono = EcoIconos.TriangleAlert
                    )
                }
            }
            estado.mensaje?.let { mensaje ->
                item { Aviso(TipoAviso.INFO, null, mensaje) }
            }

            items(estado.visibles, key = { it.id }) { consejo -> TarjetaConsejo(consejo) }
        }
    }
}

@Composable
private fun TarjetaConsejo(consejo: Consejo) {
    val escuela = consejo.ambito == AmbitoConsejo.ESCUELA
    Tarjeta(relleno = 16.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (escuela) EcoColores.VerdeClaro else EcoColores.AzulClaro),
                contentAlignment = Alignment.Center
            ) {
                Text(consejo.icono, fontSize = 26.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Etiqueta(
                    consejo.categoria,
                    if (escuela) EcoColores.VerdeClaro else EcoColores.AzulClaro,
                    if (escuela) EcoColores.Verde else EcoColores.Azul
                )
                Spacer(Modifier.height(6.dp))
                Text(consejo.titulo, style = MaterialTheme.typography.titleMedium, color = EcoColores.Texto)
                Spacer(Modifier.height(4.dp))
                Text(consejo.texto, style = MaterialTheme.typography.bodyMedium, color = EcoColores.TextoSuave)
            }
        }
    }
}

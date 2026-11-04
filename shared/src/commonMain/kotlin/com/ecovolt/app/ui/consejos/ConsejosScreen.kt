package com.ecovolt.app.ui.consejos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecovolt.app.model.AmbitoConsejo
import com.ecovolt.app.model.Consejo
import kotlin.math.round

// Consejos de ahorro para la escuela y el hogar (RFN-25)
@Composable
fun ConsejosScreen(viewModel: ConsejosViewModel, alVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val colores = MaterialTheme.colorScheme

    LaunchedEffect(Unit) { viewModel.cargar() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to colores.primary, 0.3f to colores.primary, 1f to colores.background))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().safeContentPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = alVolver,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        modifier = Modifier.align(Alignment.Start)
                    ) { Text("← Volver") }
                    Text(
                        text = "Consejos de ahorro",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pequeños cambios que ahorran energía",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(20.dp))

                    // Pestañas Escuela / Hogar
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
                    ) {
                        AmbitoConsejo.entries.forEach { ambito ->
                            OpcionAmbito(
                                texto = "${ambito.icono} ${ambito.etiqueta}",
                                activa = estado.ambito == ambito,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.elegirAmbito(ambito) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    // Recomendación según las lecturas del medidor (solo tiene sentido en la escuela)
                    val consumo = estado.consumoFinDeSemana
                    if (estado.ambito == AmbitoConsejo.ESCUELA && consumo != null) {
                        AvisoConsumo(consumoKwh = consumo, fantasma = estado.consumoFantasma)
                        Spacer(Modifier.height(12.dp))
                    }

                    if (estado.cargando && estado.consejos.isEmpty()) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(8.dp))
                    }
                    estado.mensaje?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            items(estado.visibles, key = { it.id }) { consejo ->
                ConsejoItem(consejo)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// Tarjeta destacada: avisa si el fin de semana hubo consumo fantasma o felicita si no
@Composable
private fun AvisoConsumo(consumoKwh: Double, fantasma: Boolean) {
    val colores = MaterialTheme.colorScheme
    val kwh = formatoKwh(consumoKwh)
    Surface(
        color = if (fantasma) colores.errorContainer else colores.primaryContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = if (fantasma) "⚠️ Consumo fantasma detectado" else "✅ Buen fin de semana",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (fantasma) colores.onErrorContainer else colores.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (fantasma) {
                    "El último fin de semana, con la escuela cerrada, se consumieron $kwh kWh. " +
                        "Revisa los consejos de equipos 🔌 y haz la revisión del viernes."
                } else {
                    "El último fin de semana la escuela consumió $kwh kWh. ¡Sigan apagando todo al salir!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (fantasma) colores.onErrorContainer else colores.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ConsejoItem(consejo: Consejo) {
    val colores = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp).padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(Modifier.padding(16.dp)) {
            // Círculo con el emoji del consejo
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(colores.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(consejo.icono, fontSize = 24.sp)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = consejo.categoria.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colores.primary
                )
                Text(consejo.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(consejo.texto, style = MaterialTheme.typography.bodyMedium, color = colores.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OpcionAmbito(texto: String, activa: Boolean, modifier: Modifier, alClic: () -> Unit) {
    if (activa) {
        Button(
            onClick = alClic,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
            modifier = modifier
        ) { Text(texto, fontWeight = FontWeight.Bold) }
    } else {
        OutlinedButton(
            onClick = alClic,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            modifier = modifier
        ) { Text(texto) }
    }
}

// 3.9000000000000909 → "3.9". Las restas con Double dejan decimales "basura"; se redondea a uno.
private fun formatoKwh(valor: Double): String = (round(valor * 10) / 10).toString()

package com.ecovolt.app.ui.captura

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.service.fechaHoy

// Pantalla para leer el medidor con la cámara. La foto y el OCR se hacen en el celular;
// al tocar "Guardar" solo se envía al backend el número que el usuario confirmó.
@Composable
fun CapturaScreen(viewModel: CapturaViewModel, alVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val colores = MaterialTheme.colorScheme
    val fecha = remember { fechaHoy() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to colores.primary, 0.3f to colores.primary, 1f to colores.background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(
                onClick = alVolver,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                modifier = Modifier.align(Alignment.Start)
            ) { Text("← Volver") }

            Text(
                text = "Leer medidor",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Toma una foto al contador y revisa el número",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    // Paso 1
                    Text("1. ¿Qué lectura es?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OpcionMomento("Viernes", estado.momento == MomentoLectura.VIERNES_CIERRE, Modifier.weight(1f)) {
                            viewModel.elegirMomento(MomentoLectura.VIERNES_CIERRE)
                        }
                        OpcionMomento("Lunes", estado.momento == MomentoLectura.LUNES_INICIO, Modifier.weight(1f)) {
                            viewModel.elegirMomento(MomentoLectura.LUNES_INICIO)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Viernes es el cierre de clases y lunes el inicio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onSurfaceVariant
                    )

                    // Paso 2
                    Spacer(Modifier.height(20.dp))
                    Text("2. Fotografía el medidor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::tomarFoto,
                        enabled = !estado.leyendo && !estado.guardando,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (estado.leyendo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colores.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.size(10.dp))
                            Text("Leyendo...")
                        } else {
                            Text("📷  Tomar foto", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (estado.textoOcr.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = colores.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Texto detectado", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = estado.textoOcr,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Paso 3
                    Spacer(Modifier.height(20.dp))
                    Text("3. Confirma el número", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    if (estado.candidatos.size > 1) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Otras opciones detectadas:",
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            estado.candidatos.forEach { candidato ->
                                OutlinedButton(
                                    onClick = { viewModel.usarCandidato(candidato) },
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text(candidato) }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = estado.valor,
                        onValueChange = viewModel::alCambiarValor,
                        label = { Text("Lectura (kWh)") },
                        singleLine = true,
                        isError = estado.errorValor != null,
                        supportingText = { estado.errorValor?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Fecha de la lectura: $fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onSurfaceVariant
                    )

                    estado.mensaje?.let { mensaje ->
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = colores.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colores.onPrimaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    // Envía la lectura al backend (POST /lecturas)
                    Button(
                        onClick = { viewModel.guardar(alVolver) },
                        enabled = !estado.leyendo && !estado.guardando,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (estado.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colores.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.size(10.dp))
                            Text("Guardando...")
                        } else {
                            Text("Guardar lectura", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OpcionMomento(texto: String, activa: Boolean, modifier: Modifier, alClic: () -> Unit) {
    if (activa) {
        Button(onClick = alClic, shape = RoundedCornerShape(12.dp), modifier = modifier) { Text(texto) }
    } else {
        OutlinedButton(onClick = alClic, shape = RoundedCornerShape(12.dp), modifier = modifier) { Text(texto) }
    }
}

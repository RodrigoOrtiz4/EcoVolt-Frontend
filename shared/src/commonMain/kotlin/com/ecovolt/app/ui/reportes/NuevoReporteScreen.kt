package com.ecovolt.app.ui.reportes

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.TipoReporte
import com.ecovolt.app.service.aImagen

// Áreas más comunes de la escuela: un toque y se escriben solas (luego se puede completar, ej. "Salón 3B")
private val AREAS_SUGERIDAS = listOf(
    "Salón", "Laboratorio", "Centro de cómputo", "Baños", "Oficinas", "Biblioteca", "Cafetería", "Patio"
)

@Composable
fun NuevoReporteScreen(viewModel: NuevoReporteViewModel, alVolver: () -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val colores = MaterialTheme.colorScheme

    // Remember solo lo repite cuando cambia la foto, no en cada redibujo
    val vistaPrevia = remember(estado.foto) { estado.foto?.bytes?.aImagen() }

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
            

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    // Paso 1: tipo
                    Text("1. ¿Qué quieres reportar?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        TipoReporte.entries.forEach { tipo ->
                            OpcionTipo(
                                texto = "${tipo.icono} ${tipo.etiqueta}",
                                activa = estado.tipo == tipo,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.elegirTipo(tipo) }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(estado.tipo.ayuda, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)

                    // Paso 2: área
                    Spacer(Modifier.height(20.dp))
                    Text("2. ¿Dónde está?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        AREAS_SUGERIDAS.forEach { area ->
                            OutlinedButton(
                                onClick = { viewModel.usarArea(area) },
                                enabled = !estado.enviando,
                                shape = RoundedCornerShape(10.dp)
                            ) { Text(area) }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = estado.area,
                        onValueChange = viewModel::alCambiarArea,
                        label = { Text("Área (ej. Salón 3B)") },
                        singleLine = true,
                        enabled = !estado.enviando,
                        isError = estado.errorArea != null,
                        supportingText = { estado.errorArea?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Paso 3: descripción
                    Spacer(Modifier.height(12.dp))
                    Text("3. Describe lo que viste", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = estado.descripcion,
                        onValueChange = viewModel::alCambiarDescripcion,
                        label = { Text("Descripción") },
                        placeholder = { Text("Ej. Las luces y el proyector siguen encendidos y no hay nadie.") },
                        minLines = 3,
                        enabled = !estado.enviando,
                        isError = estado.errorDescripcion != null,
                        // Muestra el error si hay; si no, cuántos caracteres van
                        supportingText = { Text(estado.errorDescripcion ?: "${estado.descripcion.length}/500") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Paso 4: foto
                    Spacer(Modifier.height(12.dp))
                    Text("4. Agrega una foto", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    if (vistaPrevia != null) {
                        Image(
                            bitmap = vistaPrevia,
                            contentDescription = "Foto del reporte",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    if (estado.procesandoFoto) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.size(10.dp))
                            Text("Preparando la foto...", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = viewModel::tomarFoto,
                                enabled = !estado.enviando,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text(if (estado.foto == null) "📷 Cámara" else "📷 Otra") }
                            OutlinedButton(
                                onClick = viewModel::elegirDeGaleria,
                                enabled = !estado.enviando,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("🖼️ Galería") }
                        }
                        if (estado.foto != null) {
                            TextButton(onClick = viewModel::quitarFoto, enabled = !estado.enviando) {
                                Text("Quitar foto", color = colores.error)
                            }
                        }
                    }
                    estado.errorFoto?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = colores.error)
                    }

                    // Avisos (error de conexión, cámara no disponible, etc.)
                    estado.mensaje?.let { mensaje ->
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = colores.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colores.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    // Envía el reporte al backend (POST /reportes)
                    Button(
                        onClick = { viewModel.enviar(alVolver) },
                        enabled = !estado.ocupado,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (estado.enviando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = colores.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.size(10.dp))
                            Text("Enviando...")
                        } else {
                            Text("Enviar reporte", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// Botón que se ve relleno cuando está elegido y con borde cuando no (igual que OpcionMomento)
@Composable
private fun OpcionTipo(texto: String, activa: Boolean, modifier: Modifier, alClic: () -> Unit) {
    if (activa) {
        Button(onClick = alClic, shape = RoundedCornerShape(12.dp), modifier = modifier) { Text(texto) }
    } else {
        OutlinedButton(onClick = alClic, shape = RoundedCornerShape(12.dp), modifier = modifier) { Text(texto) }
    }
}

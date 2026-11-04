package com.ecovolt.app.ui.reportes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.Reporte
import com.ecovolt.app.model.Rol
import com.ecovolt.app.model.TipoReporte
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.service.aImagen

// Lista de reportes. El director y el responsable ven todos; los demás, solo los que enviaron
// (eso lo decide el backend; aquí solo cambia el texto del encabezado).
@Composable
fun ReportesScreen(
    usuario: Usuario,
    viewModel: ReportesViewModel,
    alNuevoReporte: () -> Unit,
    alVolver: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val colores = MaterialTheme.colorScheme
    val veTodos = usuario.rol == Rol.DIRECTOR || usuario.rol == Rol.RESPONSABLE

    // Se ejecuta una vez cada vez que se entra a esta pantalla (también al volver del formulario)
    LaunchedEffect(Unit) { viewModel.cargar() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to colores.primary, 0.3f to colores.primary, 1f to colores.background))
    ) {
        // LazyColumn solo dibuja las tarjetas que se ven en pantalla (importante si hay muchas fotos)
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
                        text = if (veTodos) "Reportes recibidos" else "Mis reportes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (veTodos) "Desperdicios y fallas reportados en la escuela" else "Los reportes que has enviado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = alNuevoReporte,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colores.secondary, contentColor = colores.onSecondary),
                        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp).height(52.dp)
                    ) { Text("➕  Nuevo reporte", fontWeight = FontWeight.SemiBold) }
                    Spacer(Modifier.height(12.dp))

                    if (estado.cargando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(8.dp))
                    }
                    estado.mensaje?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (!estado.cargando && estado.reportes.isEmpty() && estado.mensaje == null) {
                        Text(
                            text = "Aún no hay reportes. Si ves una luz encendida sin nadie o algo descompuesto, repórtalo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }

            items(estado.reportes, key = { it.id }) { reporte ->
                ReporteItem(
                    reporte = reporte,
                    foto = estado.fotos[reporte.id],
                    cargandoFoto = reporte.id in estado.cargandoFotos,
                    mostrarAutor = veTodos,
                    alVerFoto = { viewModel.verFoto(reporte.id) }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ReporteItem(
    reporte: Reporte,
    foto: ByteArray?,
    cargandoFoto: Boolean,
    mostrarAutor: Boolean,
    alVerFoto: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val imagen = remember(foto) { foto?.aImagen() }
    // Desperdicio en amarillo "volt"; falla en el color de error para que resalte
    val colorTipo = if (reporte.tipo == TipoReporte.FALLA) colores.errorContainer else colores.secondary
    val textoTipo = if (reporte.tipo == TipoReporte.FALLA) colores.onErrorContainer else colores.onSecondary

    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp).padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = colorTipo) {
                    Text(
                        text = "${reporte.tipo.icono} ${reporte.tipo.etiqueta}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textoTipo,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.size(8.dp))
                Text(reporte.fecha, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text("📍 ${reporte.area}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(reporte.descripcion, style = MaterialTheme.typography.bodyMedium)
            if (mostrarAutor) {
                Text("Reportó: ${reporte.autor}", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))

            when {
                imagen != null -> Image(
                    bitmap = imagen,
                    contentDescription = "Foto del reporte en ${reporte.area}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                )
                cargandoFoto -> CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else -> TextButton(onClick = alVerFoto) { Text("🖼️ Ver foto") }
            }
        }
    }
}

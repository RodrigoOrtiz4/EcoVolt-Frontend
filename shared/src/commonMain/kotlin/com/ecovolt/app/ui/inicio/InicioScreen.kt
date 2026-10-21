package com.ecovolt.app.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecovolt.app.model.Rol
import com.ecovolt.app.model.Usuario

// Pantalla de bienvenida después del login. Muestra el rol con el que se entró.
@Composable
fun InicioScreen(
    usuario: Usuario,
    alVerLecturas: () -> Unit,
    alCapturar: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val puedeVerLecturas = usuario.rol == Rol.DIRECTOR || usuario.rol == Rol.RESPONSABLE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to colores.primary, 0.42f to colores.primary, 1f to colores.background))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(84.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = usuario.nombre.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = colores.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Hola, ${usuario.nombre}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Surface(shape = CircleShape, color = colores.secondary) {
                Text(
                    text = usuario.rol.etiqueta,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colores.onSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = "Sesión iniciada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = usuario.rol.descripcion, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(20.dp))

                    // Mismos roles que el backend deja guardar lecturas (DIRECTOR y RESPONSABLE)
                    if (puedeVerLecturas) {
                        Button(
                            onClick = alCapturar,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("📷  Leer medidor con la cámara", fontWeight = FontWeight.SemiBold) }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = alVerLecturas,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) { Text("Ver lecturas del medidor") }
                        Spacer(Modifier.height(12.dp))
                    }
                    OutlinedButton(
                        onClick = alCerrarSesion,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) { Text("Cerrar sesión") }
                }
            }
        }
    }
}

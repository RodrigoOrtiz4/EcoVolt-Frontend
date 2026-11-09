package com.ecovolt.app.ui.inicio

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ecovolt.app.ui.components.BarraProgreso
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.theme.EcoColores

// Logo de EcoVolt: cuadro amarillo redondeado con el rayo
@Composable
fun LogoEcoVolt(tamano: Dp, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(tamano * 0.3f)
    Box(
        modifier = modifier.size(tamano).shadow(tamano * 0.08f, forma).clip(forma).background(EcoColores.Amarillo),
        contentAlignment = Alignment.Center
    ) {
        Icon(EcoIconos.ZapDelgado, contentDescription = "EcoVolt", tint = EcoColores.Texto, modifier = Modifier.size(tamano * 0.55f))
    }
}

// Pantalla de carga (como en Figma). Mientras se llena la barra se decide a dónde ir:
// si ya hay una sesión guardada se entra directo; si no, a la bienvenida.
@Composable
fun SplashScreen(alTerminar: () -> Unit) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progreso.animateTo(1f, animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing))
        alTerminar()
    }

    Box(Modifier.fillMaxSize().background(EcoColores.Verde)) {
        Icon(
            EcoIconos.LeafDelgado,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.22f),
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 28.dp, top = 8.dp).size(76.dp)
        )
        Icon(
            EcoIconos.SunDelgado,
            contentDescription = null,
            tint = EcoColores.Amarillo,
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 24.dp, top = 4.dp).size(100.dp)
        )

        Column(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoEcoVolt(tamano = 150.dp)
            Spacer(Modifier.height(30.dp))
            Text("EcoVolt ⚡", style = MaterialTheme.typography.displaySmall, color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text(
                "Aprender a ahorrar energía\ntambién cambia el mundo.",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BarraProgreso(
                progreso = progreso.value,
                modifier = Modifier.width(200.dp),
                color = EcoColores.Amarillo,
                pista = Color.White.copy(alpha = 0.28f),
                alto = 10.dp
            )
            Spacer(Modifier.height(14.dp))
            Text("Cargando EcoVolt...", style = MaterialTheme.typography.titleSmall, color = Color.White)
        }
    }
}

package com.ecovolt.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ecovolt.app.resources.Res
import com.ecovolt.app.resources.nunito_black
import com.ecovolt.app.resources.nunito_bold
import com.ecovolt.app.resources.nunito_extrabold
import com.ecovolt.app.resources.nunito_regular
import com.ecovolt.app.resources.nunito_semibold
import org.jetbrains.compose.resources.Font

// Paleta tomada del diseño en Figma. Todas las pantallas usan estos colores,
// así un cambio aquí se ve en toda la app.
object EcoColores {
    // Marca
    val Verde = Color(0xFF167A44)            // botones, pestaña activa, splash
    val VerdeOscuro = Color(0xFF0F5A31)
    val VerdeBrillante = Color(0xFF2CCC72)   // barras de la gráfica y progreso
    val VerdeClaro = Color(0xFFE8F6EE)       // fondos suaves verdes
    val VerdeBorde = Color(0xFFA9D9BC)
    val Amarillo = Color(0xFFFFD93E)         // "volt": logo y barra destacada
    val AmarilloClaro = Color(0xFFFFF6CB)
    val AmarilloBorde = Color(0xFFE6CF77)
    val Ambar = Color(0xFF7A5B00)            // texto sobre amarillo, chip "Desperdicio"

    // Neutros
    val Fondo = Color(0xFFF6F8F6)
    val Superficie = Color.White
    val Borde = Color(0xFFE1E7E4)
    val Pista = Color(0xFFE3ECE7)            // fondo de las barras de progreso
    val Texto = Color(0xFF15302A)
    val TextoSuave = Color(0xFF5F6E68)
    val Gris = Color(0xFF8A9591)
    val GrisOscuro = Color(0xFF56665F)
    val GrisClaro = Color(0xFFE8EDEA)

    // Avisos
    val Azul = Color(0xFF1F63B0)
    val AzulClaro = Color(0xFFEAF4FE)
    val AzulBorde = Color(0xFFA9CBEE)
    val Rojo = Color(0xFFD63B3B)
    val RojoClaro = Color(0xFFFCECEC)
    val RojoBorde = Color(0xFFF2A9A9)
    val Morado = Color(0xFF8A5CD6)
    val MoradoClaro = Color(0xFFF0EBFF)

    // Medidor ilustrado
    val MedidorPanel = Color(0xFFD9E4DE)
    val MedidorCuerpo = Color(0xFFF8F6E9)
    val MedidorMarco = Color(0xFF78837D)
    val MedidorPantalla = Color(0xFF1E2F29)
    val MedidorNumero = Color(0xFFD6F9D1)
}

private val ColoresEcoVolt = lightColorScheme(
    primary = EcoColores.Verde,
    onPrimary = Color.White,
    primaryContainer = EcoColores.VerdeClaro,
    onPrimaryContainer = EcoColores.VerdeOscuro,
    secondary = EcoColores.Amarillo,
    onSecondary = EcoColores.Ambar,
    background = EcoColores.Fondo,
    onBackground = EcoColores.Texto,
    surface = EcoColores.Superficie,
    onSurface = EcoColores.Texto,
    surfaceVariant = EcoColores.GrisClaro,
    onSurfaceVariant = EcoColores.TextoSuave,
    outline = EcoColores.Borde,
    error = EcoColores.Rojo,
    errorContainer = EcoColores.RojoClaro,
    onErrorContainer = EcoColores.Rojo
)

// Nunito: la letra redondeada del diseño. Los archivos están en composeResources/font.
@Composable
private fun familiaNunito(): FontFamily = FontFamily(
    Font(Res.font.nunito_regular, FontWeight.Normal),
    Font(Res.font.nunito_semibold, FontWeight.SemiBold),
    Font(Res.font.nunito_bold, FontWeight.Bold),
    Font(Res.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(Res.font.nunito_black, FontWeight.Black)
)

@Composable
private fun tipografia(): Typography {
    val nunito = familiaNunito()
    fun estilo(tamano: Int, peso: FontWeight, alto: Int = tamano + 6) = TextStyle(
        fontFamily = nunito,
        fontWeight = peso,
        fontSize = tamano.sp,
        lineHeight = alto.sp
    )
    return Typography(
        displaySmall = estilo(40, FontWeight.Black, 46),       // "EcoVolt" del splash
        headlineLarge = estilo(30, FontWeight.ExtraBold, 36),  // "Bienvenido de nuevo"
        headlineMedium = estilo(24, FontWeight.ExtraBold, 30), // título del encabezado
        headlineSmall = estilo(21, FontWeight.ExtraBold, 27),  // "Antes de tomar la foto"
        titleLarge = estilo(19, FontWeight.ExtraBold, 24),     // texto de los botones
        titleMedium = estilo(17, FontWeight.ExtraBold, 22),    // títulos de filas y avisos
        titleSmall = estilo(15, FontWeight.ExtraBold, 20),     // etiquetas de los campos
        bodyLarge = estilo(17, FontWeight.Normal, 24),
        bodyMedium = estilo(15, FontWeight.Normal, 21),
        bodySmall = estilo(13, FontWeight.Normal, 18),
        labelLarge = estilo(16, FontWeight.ExtraBold, 20),     // chips
        labelMedium = estilo(13, FontWeight.Bold, 16),         // barra inferior
        labelSmall = estilo(12, FontWeight.ExtraBold, 16)      // "LECTURA ACTUAL"
    )
}

@Composable
fun EcoVoltTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColoresEcoVolt, typography = tipografia(), content = content)
}

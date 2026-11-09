package com.ecovolt.app.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.BotonPrincipal
import com.ecovolt.app.ui.components.BotonTexto
import com.ecovolt.app.ui.components.CabeceraResultado
import com.ecovolt.app.ui.components.CampoTexto
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.inicio.LogoEcoVolt
import com.ecovolt.app.ui.theme.EcoColores

@Composable
private fun Requisito(texto: String, cumplido: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (cumplido) EcoIconos.Check else EcoIconos.Circle,
            contentDescription = if (cumplido) "Cumplido" else "Pendiente",
            tint = if (cumplido) EcoColores.Verde else EcoColores.Gris,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            texto,
            style = MaterialTheme.typography.titleSmall,
            color = if (cumplido) EcoColores.Verde else EcoColores.TextoSuave
        )
    }
}

// "Crea una contraseña segura" (como en Figma)
@Composable
fun CambiarContrasenaScreen(
    viewModel: CambiarContrasenaViewModel,
    alVolver: () -> Unit,
    alCambiada: () -> Unit,
    alSesionVencida: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val guardar = { viewModel.guardar(alCambiada, alSesionVencida) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoColores.Fondo)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column {
            Text("Crea una contraseña segura", style = MaterialTheme.typography.headlineLarge, color = EcoColores.Texto)
            Spacer(Modifier.height(6.dp))
            Text(
                "Debe tener al menos 8 caracteres, una mayúscula y un número.",
                style = MaterialTheme.typography.bodyLarge,
                color = EcoColores.TextoSuave
            )
        }
        estado.mensaje?.let { Aviso(TipoAviso.ERROR, "No se cambió la contraseña", it) }

        CampoTexto(
            valor = estado.actual,
            alCambiar = viewModel::alCambiarActual,
            etiqueta = "Contraseña actual",
            error = estado.errorActual,
            esContrasena = true,
            mostrarContrasena = estado.mostrarActual,
            alAlternarContrasena = viewModel::alternarActual,
            teclado = KeyboardType.Password,
            habilitado = !estado.guardando
        )
        CampoTexto(
            valor = estado.nueva,
            alCambiar = viewModel::alCambiarNueva,
            etiqueta = "Nueva contraseña",
            error = estado.errorNueva,
            esContrasena = true,
            mostrarContrasena = estado.mostrarNueva,
            alAlternarContrasena = viewModel::alternarNueva,
            teclado = KeyboardType.Password,
            habilitado = !estado.guardando
        )
        CampoTexto(
            valor = estado.confirmacion,
            alCambiar = viewModel::alCambiarConfirmacion,
            etiqueta = "Confirmar contraseña",
            error = estado.errorConfirmacion,
            esContrasena = true,
            mostrarContrasena = estado.mostrarConfirmacion,
            alAlternarContrasena = viewModel::alternarConfirmacion,
            teclado = KeyboardType.Password,
            accionIme = ImeAction.Done,
            alAccionIme = guardar,
            habilitado = !estado.guardando
        )

        val todoBien = estado.tieneLongitud && estado.tieneMayusculaYNumero
        Tarjeta(
            fondo = if (todoBien) EcoColores.VerdeClaro else EcoColores.Superficie,
            borde = if (todoBien) EcoColores.VerdeBorde else EcoColores.Borde,
            relleno = 16.dp
        ) {
            Requisito("8 caracteres o más", estado.tieneLongitud)
            Spacer(Modifier.height(10.dp))
            Requisito("Incluye una mayúscula y un número", estado.tieneMayusculaYNumero)
        }

        BotonPrincipal(
            texto = "Guardar nueva contraseña",
            icono = EcoIconos.ShieldCheck,
            cargando = estado.guardando,
            alPulsar = guardar
        )
        BotonTexto("Volver", alPulsar = alVolver, habilitado = !estado.guardando)
    }
}

// "¡Contraseña actualizada!"
@Composable
fun ContrasenaActualizadaScreen(alIrALogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EcoColores.Fondo)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoEcoVolt(tamano = 44.dp)
            Spacer(Modifier.width(10.dp))
            Text("EcoVolt", style = MaterialTheme.typography.headlineMedium, color = EcoColores.Texto)
        }
        Spacer(Modifier.weight(1f))
        CabeceraResultado(
            icono = EcoIconos.BadgeCheck,
            titulo = "¡Contraseña actualizada!",
            texto = "Ya puedes ingresar con tu nueva contraseña. Tu sesión en otros dispositivos se cerró por seguridad."
        )
        Spacer(Modifier.weight(1f))
        BotonPrincipal("Ir a iniciar sesión", alPulsar = alIrALogin, icono = EcoIconos.LogIn)
    }
}

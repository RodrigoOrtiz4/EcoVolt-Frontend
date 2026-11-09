package com.ecovolt.app.ui.reportes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.EstatusReporte
import com.ecovolt.app.model.Reporte
import com.ecovolt.app.model.TipoReporte
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.service.aImagen
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.BotonCircular
import com.ecovolt.app.ui.components.BotonPrincipal
import com.ecovolt.app.ui.components.ChipOpcion
import com.ecovolt.app.ui.components.ColoresChip
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.Etiqueta
import com.ecovolt.app.ui.components.IconoEnCaja
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.theme.EcoColores

// Colores de cada estatus: fondo y texto
fun coloresEstatus(estatus: EstatusReporte): Pair<Color, Color> = when (estatus) {
    EstatusReporte.PENDIENTE -> EcoColores.AmarilloClaro to EcoColores.Ambar
    EstatusReporte.EN_PROCESO -> EcoColores.AzulClaro to EcoColores.Azul
    EstatusReporte.RESUELTO -> EcoColores.VerdeClaro to EcoColores.Verde
}

private fun coloresChipEstatus(estatus: EstatusReporte): ColoresChip = when (estatus) {
    EstatusReporte.PENDIENTE -> ColoresChip.Ambar
    EstatusReporte.EN_PROCESO -> ColoresChip.Azul
    EstatusReporte.RESUELTO -> ColoresChip.Verde
}

// Lista de reportes (pestaña "Reportar"). Director y responsable ven todos y cambian el estatus;
// los demás ven solo los suyos.
@Composable
fun ReportesScreen(
    usuario: Usuario,
    viewModel: ReportesViewModel,
    alNuevoReporte: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val atiende = usuario.rol.atiendeReportes
    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(
            titulo = if (atiende) "Reportes" else "Mis reportes",
            subtitulo = if (atiende) "De toda la escuela" else "Lo que has reportado",
            accion = {
                if (estado.cargando) {
                    CircularProgressIndicator(color = EcoColores.Verde, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
                } else {
                    BotonCircular(EcoIconos.RefreshCw, "Actualizar", viewModel::cargar)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BotonPrincipal(texto = "Reportar situación", icono = EcoIconos.CircleAlert, alPulsar = alNuevoReporte)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ChipOpcion(
                        texto = "Todos (${estado.reportes.size})",
                        seleccionado = estado.filtro == null,
                        alPulsar = { viewModel.filtrar(null) },
                        colores = ColoresChip.Gris
                    )
                    EstatusReporte.entries.forEach { estatus ->
                        ChipOpcion(
                            texto = "${estatus.etiqueta} (${estado.cuantos(estatus)})",
                            seleccionado = estado.filtro == estatus,
                            alPulsar = { viewModel.filtrar(estatus) },
                            colores = coloresChipEstatus(estatus)
                        )
                    }
                }
            }
            estado.mensaje?.let { mensaje ->
                item { Aviso(TipoAviso.ERROR, null, mensaje) }
            }
            if (estado.visibles.isEmpty() && !estado.cargando) {
                item {
                    Aviso(
                        TipoAviso.INFO,
                        "Sin reportes",
                        if (estado.filtro == null) {
                            "Cuando alguien reporte un desperdicio o una falla, aquí verás en qué va."
                        } else {
                            "No hay reportes con este estatus."
                        }
                    )
                }
            }
            items(estado.visibles, key = { it.id }) { reporte ->
                TarjetaReporte(
                    reporte = reporte,
                    atiende = atiende,
                    foto = estado.fotos[reporte.id],
                    cargandoFoto = reporte.id in estado.cargandoFotos,
                    actualizando = reporte.id in estado.actualizando,
                    alVerFoto = { viewModel.verFoto(reporte.id) },
                    alOcultarFoto = { viewModel.ocultarFoto(reporte.id) },
                    alCambiarEstatus = { viewModel.cambiarEstatus(reporte.id, it) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaReporte(
    reporte: Reporte,
    atiende: Boolean,
    foto: ByteArray?,
    cargandoFoto: Boolean,
    actualizando: Boolean,
    alVerFoto: () -> Unit,
    alOcultarFoto: () -> Unit,
    alCambiarEstatus: (EstatusReporte) -> Unit
) {
    val falla = reporte.tipo == TipoReporte.FALLA
    val (fondoEstatus, tintaEstatus) = coloresEstatus(reporte.estatus)
    val imagen = remember(foto) { foto?.aImagen() }

    Tarjeta(relleno = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoEnCaja(
                icono = if (falla) EcoIconos.Wrench else EcoIconos.Lightbulb,
                fondo = if (falla) EcoColores.RojoClaro else EcoColores.AmarilloClaro,
                tinte = if (falla) EcoColores.Rojo else EcoColores.Ambar,
                tamano = 48.dp,
                tamanoIcono = 24.dp,
                radio = 14.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(reporte.area, style = MaterialTheme.typography.titleMedium, color = EcoColores.Texto)
                Text(
                    "${reporte.folio} · ${reporte.tipo.etiqueta} · ${reporte.fecha}",
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoColores.TextoSuave
                )
            }
            Spacer(Modifier.width(8.dp))
            Etiqueta(reporte.estatus.etiqueta, fondoEstatus, tintaEstatus)
        }
        Spacer(Modifier.height(12.dp))
        Text(reporte.descripcion, style = MaterialTheme.typography.bodyLarge, color = EcoColores.Texto)
        if (atiende) {
            Spacer(Modifier.height(6.dp))
            Text("Reportó: ${reporte.autor}", style = MaterialTheme.typography.bodySmall, color = EcoColores.TextoSuave)
        }

        if (imagen != null) {
            Spacer(Modifier.height(12.dp))
            Image(
                bitmap = imagen,
                contentDescription = "Foto del reporte ${reporte.folio}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(18.dp))
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = if (imagen != null) alOcultarFoto else alVerFoto, enabled = !cargandoFoto) {
                if (cargandoFoto) {
                    CircularProgressIndicator(color = EcoColores.Verde, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                } else {
                    Icon(EcoIconos.Image, contentDescription = null, tint = EcoColores.Verde, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (imagen != null) "Ocultar foto" else "Ver foto",
                    style = MaterialTheme.typography.titleSmall,
                    color = EcoColores.Verde
                )
            }
        }

        // Dirección actualiza en qué va el reporte
        if (atiende && reporte.estatus != EstatusReporte.RESUELTO) {
            Spacer(Modifier.height(4.dp))
            if (actualizando) {
                CircularProgressIndicator(color = EcoColores.Verde, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (reporte.estatus == EstatusReporte.PENDIENTE) {
                        ChipOpcion(
                            texto = "Marcar en proceso",
                            seleccionado = false,
                            alPulsar = { alCambiarEstatus(EstatusReporte.EN_PROCESO) },
                            colores = ColoresChip.Azul,
                            icono = EcoIconos.Clock
                        )
                    }
                    ChipOpcion(
                        texto = "Resuelto",
                        seleccionado = false,
                        alPulsar = { alCambiarEstatus(EstatusReporte.RESUELTO) },
                        colores = ColoresChip.Verde,
                        icono = EcoIconos.Check
                    )
                }
            }
        }
    }
}

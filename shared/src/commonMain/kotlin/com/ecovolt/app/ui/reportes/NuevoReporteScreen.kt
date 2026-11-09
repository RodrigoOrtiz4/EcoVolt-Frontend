package com.ecovolt.app.ui.reportes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.Reporte
import com.ecovolt.app.model.TipoReporte
import com.ecovolt.app.service.aImagen
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.BotonPrincipal
import com.ecovolt.app.ui.components.BotonSecundario
import com.ecovolt.app.ui.components.CampoTexto
import com.ecovolt.app.ui.components.ChipOpcion
import com.ecovolt.app.ui.components.ColoresChip
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.FilaInfo
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.theme.EcoColores

// Botones rápidos para no teclear el área completa
private val AREAS_FRECUENTES = listOf("Salón", "Baños", "Aula de medios", "Laboratorio", "Dirección", "Cafetería", "Patio")

// Formulario "Reportar situación" (RFN-19): tipo, área, descripción y foto
@Composable
fun NuevoReporteScreen(viewModel: NuevoReporteViewModel, alVolver: () -> Unit, alEnviar: (Reporte) -> Unit) {
    val estado by viewModel.estado.collectAsState()
    var elegirFoto by remember { mutableStateOf(false) }
    val imagen = remember(estado.foto) { estado.foto?.bytes?.aImagen() }

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(titulo = "Reportar situación", subtitulo = "Falla o desperdicio", alVolver = alVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text("¿Qué encontraste?", style = MaterialTheme.typography.titleSmall, color = EcoColores.Texto)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChipOpcion(
                        texto = TipoReporte.DESPERDICIO.etiqueta,
                        seleccionado = estado.tipo == TipoReporte.DESPERDICIO,
                        alPulsar = { viewModel.elegirTipo(TipoReporte.DESPERDICIO) },
                        colores = ColoresChip.Ambar,
                        icono = EcoIconos.Lightbulb,
                        habilitado = !estado.ocupado
                    )
                    ChipOpcion(
                        texto = TipoReporte.FALLA.etiqueta,
                        seleccionado = estado.tipo == TipoReporte.FALLA,
                        alPulsar = { viewModel.elegirTipo(TipoReporte.FALLA) },
                        colores = ColoresChip.Rojo,
                        icono = EcoIconos.Wrench,
                        habilitado = !estado.ocupado
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(estado.tipo.ayuda, style = MaterialTheme.typography.bodyMedium, color = EcoColores.TextoSuave)
            }

            Column {
                CampoTexto(
                    valor = estado.area,
                    alCambiar = viewModel::alCambiarArea,
                    etiqueta = "Área de la escuela",
                    placeholder = "Ej. Salón 3B",
                    icono = EcoIconos.MapPin,
                    error = estado.errorArea,
                    capitalizacion = KeyboardCapitalization.Sentences,
                    habilitado = !estado.ocupado
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AREAS_FRECUENTES.forEach { area ->
                        ChipOpcion(
                            texto = area,
                            seleccionado = estado.area.trim() == area,
                            alPulsar = { viewModel.usarArea(area) },
                            colores = ColoresChip.Gris,
                            habilitado = !estado.ocupado
                        )
                    }
                }
            }

            CampoTexto(
                valor = estado.descripcion,
                alCambiar = viewModel::alCambiarDescripcion,
                etiqueta = "Descripción",
                placeholder = "¿Qué viste? Ej. El minisplit sigue encendido aunque el aula está vacía.",
                error = estado.errorDescripcion,
                ayuda = "${estado.descripcion.trim().length}/500 caracteres",
                capitalizacion = KeyboardCapitalization.Sentences,
                accionIme = ImeAction.Default,
                lineas = 3,
                habilitado = !estado.ocupado
            )

            // Foto: tarjeta azul para agregarla, o la vista previa si ya hay una
            Column {
                if (imagen == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(EcoColores.AzulClaro)
                            .border(
                                1.dp,
                                if (estado.errorFoto != null) EcoColores.Rojo else EcoColores.AzulBorde,
                                RoundedCornerShape(24.dp)
                            )
                            .clickable(enabled = !estado.ocupado) { elegirFoto = true }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (estado.procesandoFoto) {
                            CircularProgressIndicator(color = EcoColores.Azul, strokeWidth = 3.dp, modifier = Modifier.size(34.dp))
                        } else {
                            Icon(EcoIconos.Camera, contentDescription = null, tint = EcoColores.Azul, modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.width(18.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (estado.procesandoFoto) "Preparando la foto..." else "Agrega una foto",
                                style = MaterialTheme.typography.titleMedium,
                                color = EcoColores.Texto
                            )
                            Text(
                                "Ayuda a localizar y resolver más rápido.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EcoColores.TextoSuave
                            )
                        }
                        Icon(EcoIconos.ChevronRight, contentDescription = null, tint = EcoColores.Azul)
                    }
                } else {
                    Image(
                        bitmap = imagen,
                        contentDescription = "Foto del reporte",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(24.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        BotonSecundario(
                            texto = "Cambiar foto",
                            icono = EcoIconos.Camera,
                            habilitado = !estado.ocupado,
                            alPulsar = { elegirFoto = true },
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::quitarFoto, enabled = !estado.ocupado) {
                            Icon(EcoIconos.Trash2, contentDescription = null, tint = EcoColores.Rojo, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Quitar", style = MaterialTheme.typography.titleMedium, color = EcoColores.Rojo)
                        }
                    }
                }
                estado.errorFoto?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EcoColores.Rojo)
                }
            }

            Aviso(
                TipoAviso.ADVERTENCIA,
                "No incluyas personas",
                "Fotografía únicamente el equipo o el área. Evita rostros y datos personales.",
                icono = EcoIconos.ShieldAlert
            )
            estado.mensaje?.let { Aviso(TipoAviso.ERROR, "No se envió el reporte", it) }

            BotonPrincipal(
                texto = "Enviar reporte",
                icono = EcoIconos.Send,
                cargando = estado.enviando,
                habilitado = !estado.procesandoFoto,
                alPulsar = { viewModel.enviar(alEnviar) }
            )
        }
    }

    if (elegirFoto) {
        AlertDialog(
            onDismissRequest = { elegirFoto = false },
            confirmButton = {
                TextButton(onClick = { elegirFoto = false }) {
                    Text("Cancelar", style = MaterialTheme.typography.titleMedium, color = EcoColores.TextoSuave)
                }
            },
            title = { Text("Agregar foto", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column {
                    FilaInfo(
                        icono = EcoIconos.Camera,
                        fondoIcono = EcoColores.VerdeClaro,
                        tinteIcono = EcoColores.Verde,
                        titulo = "Tomar foto",
                        subtitulo = "Con la cámara del teléfono",
                        alPulsar = {
                            elegirFoto = false
                            viewModel.tomarFoto()
                        }
                    )
                    FilaInfo(
                        icono = EcoIconos.Image,
                        fondoIcono = EcoColores.AzulClaro,
                        tinteIcono = EcoColores.Azul,
                        titulo = "Elegir de la galería",
                        subtitulo = "Una foto que ya tengas",
                        alPulsar = {
                            elegirFoto = false
                            viewModel.elegirDeGaleria()
                        }
                    )
                }
            },
            containerColor = EcoColores.Superficie
        )
    }
}

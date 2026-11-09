package com.ecovolt.app.ui.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.model.OrigenLectura
import com.ecovolt.app.service.Fechas
import com.ecovolt.app.service.formatoKwh
import com.ecovolt.app.service.formatoNumero
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.BarraGrafica
import com.ecovolt.app.ui.components.BotonCircular
import com.ecovolt.app.ui.components.ChipOpcion
import com.ecovolt.app.ui.components.ColoresChip
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.FilaInfo
import com.ecovolt.app.ui.components.GraficaBarras
import com.ecovolt.app.ui.components.Separador
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.components.TituloSeccion
import com.ecovolt.app.ui.theme.EcoColores

// Barras de la gráfica: el consumo de cada periodo (la más reciente, en amarillo).
// Máximo 8 para que se lean bien en el teléfono.
fun barrasDeConsumo(registros: List<RegistroLectura>): List<BarraGrafica> {
    val conConsumo = registros.filter { (it.consumo ?: -1.0) >= 0.0 }.take(8).reversed()
    return conConsumo.mapIndexed { i, registro ->
        val fecha = registro.lectura.fecha
        BarraGrafica(
            etiqueta = if (i == 0) Fechas.diaMes(fecha) else Fechas.soloDia(fecha),
            valor = registro.consumo ?: 0.0,
            destacada = i == conConsumo.lastIndex
        )
    }
}

@Composable
fun HistorialScreen(viewModel: HistorialViewModel, subtitulo: String) {
    val estado by viewModel.estado.collectAsState()
    LaunchedEffect(Unit) { viewModel.cargar() }
    val visibles = estado.visibles
    val barras = barrasDeConsumo(visibles)

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(
            titulo = "Historial de lecturas",
            subtitulo = subtitulo,
            accion = { BotonCircular(EcoIconos.Download, "Exportar", viewModel::exportar) }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ChipOpcion("30 días", estado.solo30Dias, viewModel::alternar30Dias, colores = ColoresChip.Gris)
                ChipOpcion(
                    "Apertura",
                    estado.momento == MomentoLectura.LUNES_INICIO,
                    { viewModel.filtrarMomento(MomentoLectura.LUNES_INICIO) },
                    colores = ColoresChip.Azul
                )
                ChipOpcion(
                    "Cierre",
                    estado.momento == MomentoLectura.VIERNES_CIERRE,
                    { viewModel.filtrarMomento(MomentoLectura.VIERNES_CIERRE) },
                    colores = ColoresChip.Ambar
                )
                if (estado.cargando) {
                    Spacer(Modifier.weight(1f))
                    CircularProgressIndicator(color = EcoColores.Verde, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                }
            }

            estado.mensaje?.let { Aviso(TipoAviso.ADVERTENCIA, null, it) }

            Tarjeta(relleno = 16.dp) {
                Text("Consumo por periodo (kWh)", style = MaterialTheme.typography.titleSmall, color = EcoColores.TextoSuave)
                Spacer(Modifier.height(12.dp))
                if (barras.isEmpty()) {
                    Text(
                        "Con dos lecturas o más aquí verás cuánto se consumió entre una y otra.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EcoColores.TextoSuave,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    )
                } else {
                    GraficaBarras(barras = barras, textoValor = { formatoNumero(it) })
                }
            }

            TituloSeccion(
                "Registros recientes",
                accion = {
                    TextButton(onClick = viewModel::exportar) {
                        Text("Exportar", style = MaterialTheme.typography.titleMedium, color = EcoColores.Verde)
                    }
                }
            )

            if (visibles.isEmpty()) {
                Aviso(
                    TipoAviso.INFO,
                    "Sin lecturas",
                    if (estado.solo30Dias) "No hay lecturas en los últimos 30 días. Quita el filtro para ver todas." else "Aún no hay lecturas registradas."
                )
            } else {
                Tarjeta(relleno = 14.dp) {
                    visibles.forEachIndexed { indice, (lectura, consumo) ->
                        if (indice > 0) Separador(Modifier.padding(vertical = 2.dp))
                        val apertura = lectura.momento == MomentoLectura.LUNES_INICIO
                        val manual = lectura.origen == OrigenLectura.MANUAL
                        val detalle = buildList {
                            add(lectura.momento.etiqueta)
                            lectura.origen?.let { add(it.etiqueta) }
                            consumo?.let { add("${formatoNumero(it, conSigno = true)} kWh") }
                        }.joinToString(" · ")
                        FilaInfo(
                            icono = if (manual) EcoIconos.Keyboard else EcoIconos.CalendarCheck,
                            fondoIcono = when {
                                manual -> EcoColores.AmarilloClaro
                                apertura -> EcoColores.AzulClaro
                                else -> EcoColores.VerdeClaro
                            },
                            tinteIcono = when {
                                manual -> EcoColores.Ambar
                                apertura -> EcoColores.Azul
                                else -> EcoColores.Verde
                            },
                            titulo = Fechas.corta(lectura.fecha) + (lectura.hora?.let { " · $it" } ?: ""),
                            subtitulo = detalle,
                            valor = formatoKwh(lectura.valorKwh)
                        )
                        lectura.nota?.let { nota ->
                            Text(
                                "Nota: $nota",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EcoColores.Ambar,
                                modifier = Modifier.fillMaxWidth().padding(start = 72.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

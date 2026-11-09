package com.ecovolt.app.ui.lectura

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.service.CicloLectura
import com.ecovolt.app.service.Fechas
import com.ecovolt.app.service.formatoKwh
import com.ecovolt.app.service.formatoNumero
import com.ecovolt.app.service.numeroDeTexto
import com.ecovolt.app.ui.components.Aviso
import com.ecovolt.app.ui.components.BarraProgreso
import com.ecovolt.app.ui.components.BotonCircular
import com.ecovolt.app.ui.components.BotonPrincipal
import com.ecovolt.app.ui.components.BotonSecundario
import com.ecovolt.app.ui.components.BotonTexto
import com.ecovolt.app.ui.components.CabeceraResultado
import com.ecovolt.app.ui.components.CampoTexto
import com.ecovolt.app.ui.components.ChipOpcion
import com.ecovolt.app.ui.components.ColoresChip
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.EncabezadoPantalla
import com.ecovolt.app.ui.components.FilaInfo
import com.ecovolt.app.ui.components.IconoEnCaja
import com.ecovolt.app.ui.components.Separador
import com.ecovolt.app.ui.components.Tarjeta
import com.ecovolt.app.ui.components.TipoAviso
import com.ecovolt.app.ui.components.TituloSeccion
import com.ecovolt.app.ui.components.VistaMedidor
import com.ecovolt.app.ui.theme.EcoColores

// Una lámpara LED de 30 W gasta 0.03 kWh por hora; con 8 lámparas, 0.24 kWh por hora
private const val KWH_POR_HORA_8_LAMPARAS = 8 * 0.03

// Contenido desplazable común a las pantallas de lectura: márgenes, espacio entre bloques y
// lugar para el teclado (imePadding) y la barra de navegación del teléfono.
@Composable
private fun ColumnScope.Contenido(conBarraInferior: Boolean = false, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .then(if (conBarraInferior) Modifier else Modifier.navigationBarsPadding())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = contenido
    )
}

// "8742.6" → "8,742.6" respetando los decimales que se escribieron
private fun valorParaMostrar(texto: String): String {
    val numero = numeroDeTexto(texto) ?: return texto.ifBlank { "—" }
    val decimales = numero.toString().substringAfter('.', "").let { if (it == "0") 0 else it.length.coerceAtMost(3) }
    return formatoNumero(numero, decimales)
}

private class TextoCiclo(val tipo: TipoAviso, val titulo: String, val texto: String, val icono: ImageVector)

private fun textoCiclo(ciclo: CicloLectura): TextoCiclo {
    val dia = Fechas.corta(ciclo.fecha)
    val apertura = ciclo.momento == MomentoLectura.LUNES_INICIO
    return when {
        ciclo.completo -> TextoCiclo(
            TipoAviso.EXITO, "Ciclo semanal completo",
            "Ya están el cierre y la apertura. La próxima lectura es el cierre del $dia.", EcoIconos.CircleCheck
        )
        ciclo.atrasada && apertura -> TextoCiclo(
            TipoAviso.ADVERTENCIA, "Lectura de apertura atrasada",
            "No se registró la apertura del $dia. Tómala cuanto antes para estimar el consumo del fin de semana.",
            EcoIconos.Clock
        )
        ciclo.atrasada -> TextoCiclo(
            TipoAviso.ADVERTENCIA, "Lectura de cierre atrasada",
            "No se registró el cierre del $dia. Tómala cuanto antes.", EcoIconos.Clock
        )
        apertura && ciclo.esHoy -> TextoCiclo(
            TipoAviso.INFO, "Lectura de apertura pendiente",
            "Tómala antes de las 9:00 h para comparar el consumo del fin de semana.", EcoIconos.Sunrise
        )
        apertura -> TextoCiclo(
            TipoAviso.INFO, "Próxima lectura: apertura",
            "El $dia antes de las 9:00 h, para comparar el consumo del fin de semana.", EcoIconos.Sunrise
        )
        ciclo.esHoy -> TextoCiclo(
            TipoAviso.INFO, "Lectura de cierre pendiente",
            "Tómala hoy al terminar las clases, antes de cerrar la escuela.", EcoIconos.Clock
        )
        else -> TextoCiclo(
            TipoAviso.INFO, "Próxima lectura: cierre",
            "El $dia al terminar las clases.", EcoIconos.Clock
        )
    }
}

// ================================================================ 1. Inicio de lectura (pestaña)

@Composable
fun LecturaInicioScreen(viewModel: LecturaViewModel, usuario: Usuario, alPaso: (PasoLectura) -> Unit) {
    val estado by viewModel.estado.collectAsState()
    var verRecordatorio by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(
            titulo = "Lectura del medidor",
            subtitulo = usuario.rol.etiqueta,
            accion = { BotonCircular(EcoIconos.Bell, "Recordatorios", { verRecordatorio = true }) }
        )
        Contenido(conBarraInferior = true) {
            estado.mensaje?.let { Aviso(TipoAviso.ERROR, "No se pudo leer el medidor", it) }

            estado.ciclo?.let { ciclo ->
                val texto = textoCiclo(ciclo)
                Aviso(texto.tipo, texto.titulo, texto.texto, icono = texto.icono)
            }

            TarjetaCiclo(
                ciclo = estado.ciclo,
                momento = estado.momento,
                alElegirMomento = viewModel::elegirMomento
            )

            TituloSeccion("Antes de tomar la foto")
            Tarjeta(relleno = 12.dp) {
                val consejos = listOf(
                    Triple(EcoIconos.CircleCheck, "Limpia la pantalla", "Sin polvo ni reflejos"),
                    Triple(EcoIconos.Sun, "Busca buena luz", "Evita usar flash de frente"),
                    Triple(EcoIconos.Scan, "Encuadra los números", "Que se vean completos y derechos")
                )
                val colores = listOf(
                    EcoColores.VerdeClaro to EcoColores.Verde,
                    EcoColores.AmarilloClaro to EcoColores.Ambar,
                    EcoColores.AzulClaro to EcoColores.Azul
                )
                consejos.forEachIndexed { indice, (icono, titulo, subtitulo) ->
                    val revisado = indice in estado.revisados
                    FilaInfo(
                        icono = icono,
                        fondoIcono = colores[indice].first,
                        tinteIcono = colores[indice].second,
                        titulo = titulo,
                        subtitulo = subtitulo,
                        alPulsar = { viewModel.alternarRevisado(indice) },
                        alFinal = {
                            Icon(
                                if (revisado) EcoIconos.CircleCheck else EcoIconos.Circle,
                                contentDescription = if (revisado) "Listo" else "Pendiente",
                                tint = if (revisado) EcoColores.Verde else EcoColores.Borde,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    )
                }
            }

            BotonPrincipal(
                texto = if (estado.leyendo) "Leyendo el medidor..." else "Tomar lectura",
                icono = EcoIconos.Camera,
                cargando = estado.leyendo,
                alPulsar = { viewModel.tomarFoto(alPaso) }
            )
            BotonSecundario(
                texto = "Captura manual",
                icono = EcoIconos.Keyboard,
                habilitado = !estado.leyendo,
                alPulsar = {
                    viewModel.iniciarManual()
                    alPaso(PasoLectura.MANUAL)
                }
            )

            estado.anterior?.let { ultima ->
                Tarjeta(relleno = 12.dp) {
                    FilaInfo(
                        icono = EcoIconos.History,
                        fondoIcono = EcoColores.GrisClaro,
                        tinteIcono = EcoColores.GrisOscuro,
                        titulo = "Última lectura",
                        subtitulo = "${Fechas.corta(ultima.fecha)} · ${ultima.momento.etiqueta}",
                        valor = formatoKwh(ultima.valorKwh)
                    )
                }
            }
        }
    }

    if (verRecordatorio) {
        AlertDialog(
            onDismissRequest = { verRecordatorio = false },
            confirmButton = {
                TextButton(onClick = { verRecordatorio = false }) {
                    Text("Entendido", style = MaterialTheme.typography.titleMedium, color = EcoColores.Verde)
                }
            },
            icon = { Icon(EcoIconos.Bell, contentDescription = null, tint = EcoColores.Verde) },
            title = { Text("Horarios de lectura", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MomentoLectura.entries.forEach { momento ->
                        Text(
                            "${momento.etiqueta}: ${momento.dia.lowercase()} de ${momento.horario}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Text(
                        "La diferencia entre el cierre del viernes y la apertura del lunes es el consumo del fin de semana, " +
                            "cuando la escuela está cerrada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EcoColores.TextoSuave
                    )
                }
            },
            containerColor = EcoColores.Superficie
        )
    }
}

@Composable
private fun TarjetaCiclo(ciclo: CicloLectura?, momento: MomentoLectura, alElegirMomento: (MomentoLectura) -> Unit) {
    Tarjeta(fondo = EcoColores.VerdeClaro, borde = EcoColores.VerdeBorde) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (ciclo != null && ciclo.momento == momento) Fechas.corta(ciclo.fecha) else "Lectura de ${momento.dia.lowercase()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EcoColores.Texto
                )
                Text(
                    "${momento.etiqueta} · ${momento.horario}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EcoColores.TextoSuave
                )
            }
            Icon(EcoIconos.Gauge, contentDescription = null, tint = EcoColores.Verde, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ciclo semanal", style = MaterialTheme.typography.titleMedium, color = EcoColores.Texto, modifier = Modifier.weight(1f))
            Text(
                "${((ciclo?.progreso ?: 0f) * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = EcoColores.Verde
            )
        }
        Spacer(Modifier.height(10.dp))
        BarraProgreso(ciclo?.progreso ?: 0f)
        Spacer(Modifier.height(16.dp))
        Separador(color = EcoColores.VerdeBorde)
        Spacer(Modifier.height(14.dp))
        Text("¿Qué lectura vas a tomar?", style = MaterialTheme.typography.titleSmall, color = EcoColores.TextoSuave)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ChipOpcion(
                texto = "Apertura · lunes",
                seleccionado = momento == MomentoLectura.LUNES_INICIO,
                alPulsar = { alElegirMomento(MomentoLectura.LUNES_INICIO) },
                colores = ColoresChip.Azul
            )
            ChipOpcion(
                texto = "Cierre · viernes",
                seleccionado = momento == MomentoLectura.VIERNES_CIERRE,
                alPulsar = { alElegirMomento(MomentoLectura.VIERNES_CIERRE) },
                colores = ColoresChip.Ambar
            )
        }
    }
}

// Línea "+128.4 kWh desde la lectura anterior"
@Composable
private fun TextoDiferencia(estado: LecturaUiState) {
    val diferencia = estado.diferencia
    when {
        estado.anterior == null -> Text(
            "Es la primera lectura registrada",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = EcoColores.TextoSuave
        )
        diferencia == null -> Unit
        diferencia < 0 -> Text(
            "${formatoKwh(diferencia)}: es menor que la lectura anterior",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = EcoColores.Rojo
        )
        else -> Text(
            "${formatoKwh(diferencia, conSigno = true)} desde la lectura anterior",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = EcoColores.Verde
        )
    }
}

// ================================================================ 2. Confirmar lectura (OCR)

@Composable
fun ConfirmarLecturaScreen(viewModel: LecturaViewModel, alVolver: () -> Unit, alPaso: (PasoLectura) -> Unit) {
    val estado by viewModel.estado.collectAsState()

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(
            titulo = "Confirmar lectura",
            subtitulo = "${estado.momento.dia} · ${estado.momento.etiqueta}",
            alVolver = alVolver
        )
        Contenido {
            VistaMedidor(foto = estado.foto, numero = valorParaMostrar(estado.valor))
            Aviso(
                TipoAviso.INFO,
                "OCR detectó esta lectura",
                "Compara el número con la foto. Puedes corregirlo antes de guardar.",
                icono = EcoIconos.ScanText
            )
            estado.mensaje?.let { Aviso(TipoAviso.ERROR, "No se guardó la lectura", it) }

            Tarjeta {
                Text("LECTURA ACTUAL", style = MaterialTheme.typography.labelSmall, color = EcoColores.TextoSuave)
                Spacer(Modifier.height(8.dp))
                if (estado.editandoValor) {
                    CampoTexto(
                        valor = estado.valor,
                        alCambiar = viewModel::alCambiarValor,
                        etiqueta = null,
                        placeholder = "Ej. 8,742.6",
                        icono = EcoIconos.Gauge,
                        error = estado.errorValor,
                        teclado = KeyboardType.Decimal,
                        accionIme = ImeAction.Done,
                        alAccionIme = viewModel::alternarEdicion
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            valorParaMostrar(estado.valor),
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 36.sp,
                            lineHeight = 42.sp,
                            color = EcoColores.Texto
                        )
                        Text(" kWh", style = MaterialTheme.typography.titleLarge, color = EcoColores.Texto)
                        Spacer(Modifier.weight(1f))
                        ChipOpcion(
                            texto = "Editar",
                            seleccionado = false,
                            alPulsar = viewModel::alternarEdicion,
                            colores = ColoresChip.Azul,
                            icono = EcoIconos.Pencil
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                TextoDiferencia(estado)

                val otros = estado.candidatos.filter { it != estado.valor }
                if (otros.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text("¿Es otro número? Toca para usarlo", style = MaterialTheme.typography.bodyMedium, color = EcoColores.TextoSuave)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        otros.forEach { candidato ->
                            ChipOpcion(
                                texto = valorParaMostrar(candidato),
                                seleccionado = false,
                                alPulsar = { viewModel.usarCandidato(candidato) },
                                colores = ColoresChip.Gris
                            )
                        }
                    }
                }
            }

            BotonPrincipal(
                texto = "Confirmar y guardar",
                icono = EcoIconos.CircleCheck,
                cargando = estado.guardando,
                alPulsar = { viewModel.guardar(alPaso) }
            )
            BotonSecundario(
                texto = "Corregir número",
                icono = EcoIconos.Pencil,
                habilitado = !estado.guardando,
                alPulsar = {
                    viewModel.corregirNumero()
                    alPaso(PasoLectura.MANUAL)
                }
            )
        }
    }
}

// ================================================================ 3. No pudimos leer la foto

@Composable
fun LecturaIlegibleScreen(viewModel: LecturaViewModel, alVolver: () -> Unit, alPaso: (PasoLectura) -> Unit) {
    val estado by viewModel.estado.collectAsState()

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(titulo = "No pudimos leer la foto", alVolver = alVolver)
        Contenido {
            VistaMedidor(foto = estado.foto, numero = "")
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                IconoEnCaja(EcoIconos.CircleX, EcoColores.RojoClaro, EcoColores.Rojo, tamano = 96.dp, tamanoIcono = 48.dp, radio = 28.dp)
                Spacer(Modifier.height(20.dp))
                Text(
                    "Los números no se ven claros",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EcoColores.Texto,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Acércate un poco, evita reflejos y mantén el teléfono recto.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EcoColores.TextoSuave,
                    textAlign = TextAlign.Center
                )
            }
            estado.mensaje?.let { Aviso(TipoAviso.ERROR, null, it) }
            Spacer(Modifier.height(12.dp))
            BotonPrincipal(
                texto = "Repetir",
                icono = EcoIconos.Camera,
                cargando = estado.leyendo,
                alPulsar = { viewModel.tomarFoto(alPaso) }
            )
            BotonSecundario(
                texto = "Captura manual",
                icono = EcoIconos.Keyboard,
                habilitado = !estado.leyendo,
                alPulsar = {
                    viewModel.iniciarManual()
                    alPaso(PasoLectura.MANUAL)
                }
            )
        }
    }
}

// ================================================================ 4. Captura manual

@Composable
fun CapturaManualScreen(viewModel: LecturaViewModel, alVolver: () -> Unit, alPaso: (PasoLectura) -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val anterior = estado.anterior

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(
            titulo = "Captura manual",
            subtitulo = "${estado.momento.dia} · ${estado.momento.etiqueta}",
            alVolver = alVolver
        )
        Contenido {
            Aviso(
                TipoAviso.INFO,
                "Escribe todos los dígitos",
                "Incluye el decimal tal como aparece en el medidor. La unidad es kWh."
            )
            if (estado.foto != null) VistaMedidor(foto = estado.foto, numero = "")

            CampoTexto(
                valor = estado.valor,
                alCambiar = viewModel::alCambiarValor,
                etiqueta = "Lectura actual (kWh)",
                placeholder = "Ej. 8,742.6",
                icono = EcoIconos.Gauge,
                error = estado.errorValor,
                teclado = KeyboardType.Decimal,
                accionIme = ImeAction.Done,
                alAccionIme = { viewModel.guardar(alPaso) },
                habilitado = !estado.guardando
            )

            Tarjeta(relleno = 14.dp) {
                FilaInfo(
                    icono = EcoIconos.History,
                    fondoIcono = EcoColores.VerdeClaro,
                    tinteIcono = EcoColores.Verde,
                    titulo = "Lectura anterior",
                    subtitulo = anterior?.let { "${Fechas.corta(it.fecha)} · ${it.momento.etiqueta}" } ?: "Aún no hay lecturas",
                    valor = anterior?.let { formatoKwh(it.valorKwh) } ?: "—"
                )
                FilaInfo(
                    icono = EcoIconos.Calculator,
                    fondoIcono = EcoColores.AzulClaro,
                    tinteIcono = EcoColores.Azul,
                    titulo = "Diferencia calculada",
                    subtitulo = if (estado.esMenorQueAnterior) "Es menor que la anterior" else "Consumo del periodo",
                    valor = estado.diferencia?.let { formatoKwh(it) } ?: "—",
                    colorTitulo = if (estado.esMenorQueAnterior) EcoColores.Rojo else EcoColores.Texto
                )
            }

            Aviso(
                TipoAviso.ADVERTENCIA,
                "Revisa antes de guardar",
                "Una lectura equivocada cambia las alertas y los reportes de la escuela."
            )
            estado.mensaje?.let { Aviso(TipoAviso.ERROR, "No se guardó la lectura", it) }

            BotonPrincipal(
                texto = "Guardar lectura",
                icono = EcoIconos.Save,
                cargando = estado.guardando,
                alPulsar = { viewModel.guardar(alPaso) }
            )
            BotonTexto("Cancelar", alPulsar = alVolver, habilitado = !estado.guardando)
        }
    }
}

// ================================================================ 5. Revisar lectura (menor que la anterior)

@Composable
fun RevisarLecturaScreen(
    viewModel: LecturaViewModel,
    alVolver: () -> Unit,
    alCorregir: () -> Unit,
    alPaso: (PasoLectura) -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val capturada = numeroDeTexto(estado.valor)
    val anterior = estado.anterior

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        EncabezadoPantalla(titulo = "Revisar lectura", alVolver = alVolver)
        Contenido {
            IconoEnCaja(EcoIconos.TriangleAlert, EcoColores.AmarilloClaro, EcoColores.Ambar, tamano = 96.dp, tamanoIcono = 46.dp, radio = 28.dp)
            Aviso(
                TipoAviso.ERROR,
                "La lectura es menor a la anterior",
                if (capturada != null && anterior != null) {
                    "Capturaste ${formatoKwh(capturada)}, pero la lectura anterior fue ${formatoKwh(anterior.valorKwh)}."
                } else {
                    "El número capturado es menor que la lectura anterior."
                },
                icono = EcoIconos.TriangleAlert
            )
            estado.mensaje?.let { Aviso(TipoAviso.ERROR, null, it) }

            Tarjeta {
                Text("¿Se cambió o reinició el medidor?", style = MaterialTheme.typography.titleLarge, color = EcoColores.Texto)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Si no hubo cambio, corrige el número. Si sí hubo cambio, deja una nota para dirección.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EcoColores.TextoSuave
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChipOpcion(
                        texto = "No",
                        seleccionado = estado.cambioMedidor == false,
                        alPulsar = { viewModel.elegirCambioMedidor(false) },
                        colores = ColoresChip.Verde
                    )
                    ChipOpcion(
                        texto = "Sí",
                        seleccionado = estado.cambioMedidor == true,
                        alPulsar = { viewModel.elegirCambioMedidor(true) },
                        colores = ColoresChip.Azul
                    )
                }
            }

            CampoTexto(
                valor = estado.nota,
                alCambiar = viewModel::alCambiarNota,
                etiqueta = "Nota para dirección",
                placeholder = "Describe el cambio de medidor...",
                error = estado.errorNota,
                ayuda = if (estado.cambioMedidor == true) "Mínimo 10 caracteres." else "Elige \"Sí\" para escribir la nota.",
                capitalizacion = KeyboardCapitalization.Sentences,
                accionIme = ImeAction.Default,
                lineas = 3,
                habilitado = estado.cambioMedidor == true && !estado.guardando
            )

            BotonPrincipal(
                texto = "Corregir lectura",
                icono = EcoIconos.Pencil,
                habilitado = !estado.guardando,
                alPulsar = alCorregir
            )
            if (estado.guardando) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = EcoColores.Verde, modifier = Modifier.size(32.dp))
                }
            } else {
                BotonSecundario(
                    texto = "Guardar con nota",
                    icono = EcoIconos.FileWarning,
                    habilitado = estado.cambioMedidor == true,
                    alPulsar = { viewModel.guardarConNota(alPaso) }
                )
            }
        }
    }
}

// ================================================================ 6. ¡Lectura guardada! (con barra inferior)

@Composable
fun LecturaGuardadaScreen(viewModel: LecturaViewModel, alVerHistorial: () -> Unit, alOtraLectura: () -> Unit) {
    val estado by viewModel.estado.collectAsState()
    val guardada = estado.guardada

    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (guardada == null) {
                CabeceraResultado(EcoIconos.CircleCheck, "Lectura guardada", null)
                BotonPrincipal("Ver historial", alPulsar = alVerHistorial, icono = EcoIconos.History)
            } else {
                Spacer(Modifier.height(12.dp))
                CabeceraResultado(
                    icono = EcoIconos.CircleCheck,
                    titulo = "¡Lectura guardada!",
                    texto = "${Fechas.corta(guardada.fecha)}${guardada.hora?.let { " · $it h" } ?: ""}"
                )
                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(EcoColores.VerdeClaro)
                        .border(1.dp, EcoColores.VerdeBorde, RoundedCornerShape(28.dp))
                        .padding(14.dp)
                ) {
                    Tarjeta {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("LECTURA", style = MaterialTheme.typography.labelSmall, color = EcoColores.TextoSuave, modifier = Modifier.weight(1f))
                            IconoEnCaja(EcoIconos.Gauge, EcoColores.VerdeClaro, EcoColores.Verde, tamano = 40.dp, tamanoIcono = 22.dp, radio = 12.dp)
                        }
                        Text(formatoKwh(guardada.valorKwh), style = MaterialTheme.typography.headlineLarge, color = EcoColores.Texto)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${guardada.momento.etiqueta} · ${guardada.origen?.etiqueta ?: "Registro"} · sincronizado",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcoColores.Verde
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        textoEquivalencia(estado.consumoGuardada),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EcoColores.Texto,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                guardada.nota?.let { Aviso(TipoAviso.INFO, "Nota para dirección", it, icono = EcoIconos.FileWarning) }

                Spacer(Modifier.height(8.dp))
                BotonPrincipal("Ver historial", alPulsar = alVerHistorial, icono = EcoIconos.History)
                BotonTexto("Tomar otra lectura", alPulsar = alOtraLectura)
            }
        }
    }
}

private fun textoEquivalencia(consumo: Double?): String = when {
    consumo == null -> "Es la primera lectura registrada. Con la siguiente verás el consumo del periodo."
    consumo <= 0.0 -> "Se guardó con una nota para dirección. El consumo de este periodo no se puede calcular."
    else -> {
        val horas = consumo / KWH_POR_HORA_8_LAMPARAS
        "Los ${formatoNumero(consumo)} kWh de este periodo equivalen a usar 8 lámparas LED de 30 W " +
            "durante unas ${formatoNumero(horas, 0)} horas."
    }
}

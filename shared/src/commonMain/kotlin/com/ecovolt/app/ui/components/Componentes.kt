package com.ecovolt.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.ecovolt.app.service.aImagen
import com.ecovolt.app.ui.theme.EcoColores

// Piezas de interfaz que se repiten en varias pantallas. Así todas se ven igual que en Figma
// y un cambio de estilo se hace en un solo lugar.

private val FormaTarjeta = RoundedCornerShape(24.dp)
private val FormaBoton = RoundedCornerShape(16.dp)

// ---------------------------------------------------------------- Encabezado

// Barra blanca superior: botón para volver, título, subtítulo y una acción opcional a la derecha.
// Se pinta detrás de la barra de estado del teléfono (por eso statusBarsPadding).
@Composable
fun EncabezadoPantalla(
    titulo: String,
    subtitulo: String? = null,
    alVolver: (() -> Unit)? = null,
    accion: (@Composable () -> Unit)? = null
) {
    Column(Modifier.fillMaxWidth().background(EcoColores.Superficie).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (alVolver != null) {
                BotonCircular(icono = EcoIconos.ArrowLeft, descripcion = "Volver", alPulsar = alVolver)
                Spacer(Modifier.width(14.dp))
            } else {
                Spacer(Modifier.width(4.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = EcoColores.Texto,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitulo != null) {
                    Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = EcoColores.TextoSuave)
                }
            }
            if (accion != null) {
                Spacer(Modifier.width(8.dp))
                accion()
            }
        }
        Separador()
    }
}

@Composable
fun Separador(modifier: Modifier = Modifier, color: Color = EcoColores.Borde) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

// Botón redondo con un ícono (volver, campana, descargar...)
@Composable
fun BotonCircular(
    icono: ImageVector,
    descripcion: String?,
    alPulsar: () -> Unit,
    fondo: Color = EcoColores.VerdeClaro,
    tinte: Color = EcoColores.Texto,
    tamano: Dp = 48.dp
) {
    Box(
        modifier = Modifier.size(tamano).clip(CircleShape).background(fondo).clickable(onClick = alPulsar),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = descripcion, tint = tinte, modifier = Modifier.size(tamano * 0.46f))
    }
}

// ---------------------------------------------------------------- Botones

@Composable
fun BotonPrincipal(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    color: Color = EcoColores.Verde
) {
    Button(
        onClick = alPulsar,
        enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().height(58.dp),
        shape = FormaBoton,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = color.copy(alpha = 0.45f),
            disabledContentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 3.dp)
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
            }
            Text(texto, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true,
    color: Color = EcoColores.Verde
) {
    OutlinedButton(
        onClick = alPulsar,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().height(58.dp),
        shape = FormaBoton,
        border = BorderStroke(2.dp, if (habilitado) color else color.copy(alpha = 0.4f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = EcoColores.Superficie,
            contentColor = EcoColores.Texto,
            disabledContainerColor = EcoColores.Superficie,
            disabledContentColor = EcoColores.Gris
        )
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(texto, style = MaterialTheme.typography.titleLarge)
    }
}

// "Volver", "Cancelar": texto en negritas sin fondo
@Composable
fun BotonTexto(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = EcoColores.Texto,
    habilitado: Boolean = true
) {
    TextButton(onClick = alPulsar, enabled = habilitado, modifier = modifier.fillMaxWidth().height(52.dp)) {
        Text(texto, style = MaterialTheme.typography.titleLarge, color = color)
    }
}

// ---------------------------------------------------------------- Avisos

enum class TipoAviso { INFO, EXITO, ADVERTENCIA, ERROR }

private class ColoresAviso(val fondo: Color, val borde: Color, val titulo: Color, val icono: ImageVector)

private fun coloresDe(tipo: TipoAviso): ColoresAviso = when (tipo) {
    TipoAviso.INFO -> ColoresAviso(EcoColores.AzulClaro, EcoColores.AzulBorde, EcoColores.Azul, EcoIconos.Info)
    TipoAviso.EXITO -> ColoresAviso(EcoColores.VerdeClaro, EcoColores.VerdeBorde, EcoColores.Verde, EcoIconos.CircleCheck)
    TipoAviso.ADVERTENCIA -> ColoresAviso(EcoColores.AmarilloClaro, EcoColores.AmarilloBorde, EcoColores.Ambar, EcoIconos.Info)
    TipoAviso.ERROR -> ColoresAviso(EcoColores.RojoClaro, EcoColores.RojoBorde, EcoColores.Rojo, EcoIconos.CircleX)
}

// Recuadro de color con ícono, título y texto ("OCR detectó esta lectura", "Revisa antes de guardar"...)
@Composable
fun Aviso(
    tipo: TipoAviso,
    titulo: String?,
    texto: String,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    val colores = coloresDe(tipo)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(colores.fondo)
            .border(1.dp, colores.borde, FormaTarjeta)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(EcoColores.Superficie),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono ?: colores.icono, contentDescription = null, tint = colores.titulo, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            if (titulo != null) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = colores.titulo)
                Spacer(Modifier.height(2.dp))
            }
            Text(texto, style = MaterialTheme.typography.bodyMedium, color = EcoColores.Texto)
        }
    }
}

// ---------------------------------------------------------------- Tarjetas y filas

@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    fondo: Color = EcoColores.Superficie,
    borde: Color = EcoColores.Borde,
    relleno: Dp = 20.dp,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaTarjeta)
            .background(fondo)
            .border(1.dp, borde, FormaTarjeta)
            .padding(relleno),
        content = contenido
    )
}

// Cuadro redondeado de color con un ícono al centro
@Composable
fun IconoEnCaja(
    icono: ImageVector,
    fondo: Color,
    tinte: Color,
    modifier: Modifier = Modifier,
    tamano: Dp = 56.dp,
    tamanoIcono: Dp = 26.dp,
    radio: Dp = 16.dp
) {
    Box(
        modifier = modifier.size(tamano).clip(RoundedCornerShape(radio)).background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(tamanoIcono))
    }
}

// Fila con ícono, título, subtítulo y algo a la derecha (un valor, una flecha...)
@Composable
fun FilaInfo(
    icono: ImageVector,
    fondoIcono: Color,
    tinteIcono: Color,
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    valor: String? = null,
    colorTitulo: Color = EcoColores.Texto,
    alPulsar: (() -> Unit)? = null,
    alFinal: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (alPulsar != null) Modifier.clickable(onClick = alPulsar) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoEnCaja(icono, fondoIcono, tinteIcono)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = colorTitulo)
            if (subtitulo != null) {
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = EcoColores.TextoSuave)
            }
        }
        if (valor != null) {
            Spacer(Modifier.width(8.dp))
            Text(valor, style = MaterialTheme.typography.titleMedium, color = EcoColores.Texto, textAlign = TextAlign.End)
        }
        if (alFinal != null) {
            Spacer(Modifier.width(8.dp))
            alFinal()
        }
    }
}

// Título de sección ("Antes de tomar la foto", "Registros recientes")
@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier, accion: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, style = MaterialTheme.typography.headlineSmall, color = EcoColores.Texto, modifier = Modifier.weight(1f))
        if (accion != null) accion()
    }
}

// ---------------------------------------------------------------- Chips

// Colores de un chip: lleno cuando está elegido, suave cuando no
class ColoresChip(val fondoElegido: Color, val textoElegido: Color, val fondo: Color, val texto: Color) {
    companion object {
        val Verde = ColoresChip(EcoColores.Verde, Color.White, EcoColores.VerdeClaro, EcoColores.Verde)
        val Azul = ColoresChip(EcoColores.Azul, Color.White, EcoColores.AzulClaro, EcoColores.Azul)
        val Morado = ColoresChip(EcoColores.Morado, Color.White, EcoColores.MoradoClaro, EcoColores.Morado)
        val Ambar = ColoresChip(EcoColores.Ambar, Color.White, EcoColores.AmarilloClaro, EcoColores.Ambar)
        val Rojo = ColoresChip(EcoColores.Rojo, Color.White, EcoColores.RojoClaro, EcoColores.Rojo)
        val Gris = ColoresChip(EcoColores.GrisOscuro, Color.White, EcoColores.GrisClaro, EcoColores.GrisOscuro)
    }
}

@Composable
fun ChipOpcion(
    texto: String,
    seleccionado: Boolean,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    colores: ColoresChip = ColoresChip.Verde,
    icono: ImageVector? = null,
    habilitado: Boolean = true
) {
    val fondo = if (seleccionado) colores.fondoElegido else colores.fondo
    val tinta = if (seleccionado) colores.textoElegido else colores.texto
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .clickable(enabled = habilitado, onClick = alPulsar)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, tint = tinta, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge, color = tinta)
    }
}

// Etiqueta pequeña que no se puede pulsar (estatus de un reporte, rol del usuario)
@Composable
fun Etiqueta(texto: String, fondo: Color, tinta: Color, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = tinta,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

// ---------------------------------------------------------------- Campos de texto

// Campo con etiqueta arriba, ícono a la izquierda y mensaje de error abajo (como en Figma)
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String?,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    icono: ImageVector? = null,
    error: String? = null,
    ayuda: String? = null,
    esContrasena: Boolean = false,
    mostrarContrasena: Boolean = false,
    alAlternarContrasena: (() -> Unit)? = null,
    teclado: KeyboardType = KeyboardType.Text,
    capitalizacion: KeyboardCapitalization = KeyboardCapitalization.None,
    accionIme: ImeAction = ImeAction.Next,
    alAccionIme: (() -> Unit)? = null,
    lineas: Int = 1,
    habilitado: Boolean = true
) {
    Column(modifier.fillMaxWidth()) {
        if (etiqueta != null) {
            Text(etiqueta, style = MaterialTheme.typography.titleSmall, color = EcoColores.Texto)
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            modifier = Modifier.fillMaxWidth(),
            enabled = habilitado,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = EcoColores.Texto),
            placeholder = if (placeholder != null) {
                { Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = EcoColores.TextoSuave) }
            } else {
                null
            },
            leadingIcon = if (icono != null) {
                { Icon(icono, contentDescription = null, modifier = Modifier.size(22.dp)) }
            } else {
                null
            },
            trailingIcon = if (esContrasena && alAlternarContrasena != null) {
                {
                    IconButton(onClick = alAlternarContrasena) {
                        Icon(
                            if (mostrarContrasena) EcoIconos.EyeOff else EcoIconos.Eye,
                            contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                null
            },
            isError = error != null,
            visualTransformation = if (esContrasena && !mostrarContrasena) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                capitalization = capitalizacion,
                keyboardType = teclado,
                imeAction = accionIme
            ),
            keyboardActions = if (alAccionIme != null) {
                KeyboardActions(onDone = { alAccionIme() }, onGo = { alAccionIme() }, onSend = { alAccionIme() })
            } else {
                KeyboardActions.Default
            },
            singleLine = lineas == 1,
            minLines = lineas,
            maxLines = if (lineas == 1) 1 else lineas + 3,
            shape = FormaBoton,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = EcoColores.Superficie,
                unfocusedContainerColor = EcoColores.Superficie,
                disabledContainerColor = EcoColores.GrisClaro,
                errorContainerColor = EcoColores.Superficie,
                focusedBorderColor = EcoColores.Verde,
                unfocusedBorderColor = EcoColores.Borde,
                errorBorderColor = EcoColores.Rojo,
                cursorColor = EcoColores.Verde,
                focusedLeadingIconColor = EcoColores.Verde,
                unfocusedLeadingIconColor = EcoColores.TextoSuave,
                errorLeadingIconColor = EcoColores.TextoSuave,
                focusedTrailingIconColor = EcoColores.TextoSuave,
                unfocusedTrailingIconColor = EcoColores.TextoSuave,
                errorTrailingIconColor = EcoColores.TextoSuave
            )
        )
        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EcoColores.Rojo)
        } else if (ayuda != null) {
            Spacer(Modifier.height(6.dp))
            Text(ayuda, style = MaterialTheme.typography.bodySmall, color = EcoColores.TextoSuave)
        }
    }
}

// ---------------------------------------------------------------- Barra inferior

class ItemBarra(val etiqueta: String, val icono: ImageVector)

// Menú inferior con las secciones principales. La pestaña activa lleva una píldora verde clara.
@Composable
fun BarraInferior(items: List<ItemBarra>, seleccionado: Int, alElegir: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().background(EcoColores.Superficie)) {
        Separador()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items.forEachIndexed { indice, item ->
                val activo = indice == seleccionado
                val color = if (activo) EcoColores.Verde else EcoColores.Gris
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { alElegir(indice) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (activo) EcoColores.VerdeClaro else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icono, contentDescription = item.etiqueta, tint = color, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (activo) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = color,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Progreso y carga

// Barra de progreso redondeada (ciclo semanal, splash)
@Composable
fun BarraProgreso(
    progreso: Float,
    modifier: Modifier = Modifier,
    color: Color = EcoColores.VerdeBrillante,
    pista: Color = EcoColores.Pista,
    alto: Dp = 12.dp
) {
    Box(modifier.fillMaxWidth().height(alto).clip(RoundedCornerShape(50)).background(pista)) {
        Box(
            Modifier
                .fillMaxWidth(progreso.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
fun CargandoPantalla(texto: String = "Cargando...") {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = EcoColores.Verde)
        Spacer(Modifier.height(16.dp))
        Text(texto, style = MaterialTheme.typography.bodyLarge, color = EcoColores.TextoSuave)
    }
}

// ---------------------------------------------------------------- Pantallas de resultado

// Ícono grande en un cuadro verde suave, título y texto centrados ("¡Lectura guardada!")
@Composable
fun CabeceraResultado(
    icono: ImageVector,
    titulo: String,
    texto: String?,
    fondoIcono: Color = EcoColores.VerdeClaro,
    tinteIcono: Color = EcoColores.Verde
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        IconoEnCaja(icono, fondoIcono, tinteIcono, tamano = 104.dp, tamanoIcono = 52.dp, radio = 32.dp)
        Spacer(Modifier.height(28.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.headlineLarge,
            color = EcoColores.Texto,
            textAlign = TextAlign.Center
        )
        if (texto != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                texto,
                style = MaterialTheme.typography.bodyLarge,
                color = EcoColores.TextoSuave,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------- Medidor

// Vista del medidor: la foto real si la hay, o un medidor dibujado con el número leído
@Composable
fun VistaMedidor(foto: ByteArray?, numero: String, modifier: Modifier = Modifier) {
    val imagen = remember(foto) { foto?.aImagen() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(EcoColores.MedidorPanel)
            .padding(if (imagen != null) 0.dp else 28.dp),
        contentAlignment = Alignment.Center
    ) {
        if (imagen != null) {
            Image(
                bitmap = imagen,
                contentDescription = "Foto del medidor",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(EcoColores.MedidorCuerpo)
                    .border(3.dp, EcoColores.MedidorMarco, RoundedCornerShape(20.dp))
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EcoColores.MedidorPantalla)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        numero.ifBlank { "-----" },
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 32.sp,
                        color = EcoColores.MedidorNumero,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "MEDIDOR DIGITAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = EcoColores.GrisOscuro,
                        modifier = Modifier.weight(1f)
                    )
                    Box(Modifier.size(14.dp).clip(CircleShape).background(EcoColores.Rojo))
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Gráfica de barras

class BarraGrafica(val etiqueta: String, val valor: Double, val destacada: Boolean)

// Gráfica de barras sencilla, sin librerías: cada barra es una caja con la altura proporcional
@Composable
fun GraficaBarras(
    barras: List<BarraGrafica>,
    modifier: Modifier = Modifier,
    alto: Dp = 150.dp,
    textoValor: (Double) -> String = { it.toString() }
) {
    val maximo = barras.maxOfOrNull { it.valor }?.takeIf { it > 0.0 } ?: 1.0
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(EcoColores.Fondo)
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        barras.forEach { barra ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (barra.destacada) {
                    Text(
                        textoValor(barra.valor),
                        style = MaterialTheme.typography.labelSmall,
                        color = EcoColores.Ambar,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                }
                val fraccion = (barra.valor / maximo).toFloat().coerceIn(0f, 1f)
                Box(
                    Modifier
                        .padding(horizontal = 6.dp)
                        .fillMaxWidth()
                        .height((alto * fraccion).coerceAtLeast(8.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (barra.destacada) EcoColores.Amarillo else EcoColores.VerdeBrillante)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    barra.etiqueta,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = EcoColores.TextoSuave,
                    maxLines = 1
                )
            }
        }
    }
}

package com.ecovolt.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecovolt.app.data.repository.AuthRepository
import com.ecovolt.app.data.repository.ConsejoRepository
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.data.repository.ReporteRepository
import com.ecovolt.app.model.Rol
import com.ecovolt.app.model.Usuario
import com.ecovolt.app.service.Compartidor
import com.ecovolt.app.service.LectorMedidor
import com.ecovolt.app.service.SelectorFoto
import com.ecovolt.app.ui.components.BarraInferior
import com.ecovolt.app.ui.components.EcoIconos
import com.ecovolt.app.ui.components.ItemBarra
import com.ecovolt.app.ui.consejos.ConsejosScreen
import com.ecovolt.app.ui.consejos.ConsejosViewModel
import com.ecovolt.app.ui.historial.HistorialScreen
import com.ecovolt.app.ui.historial.HistorialViewModel
import com.ecovolt.app.ui.inicio.BienvenidaScreen
import com.ecovolt.app.ui.inicio.InicioScreen
import com.ecovolt.app.ui.inicio.SplashScreen
import com.ecovolt.app.ui.lectura.CapturaManualScreen
import com.ecovolt.app.ui.lectura.ConfirmarLecturaScreen
import com.ecovolt.app.ui.lectura.LecturaGuardadaScreen
import com.ecovolt.app.ui.lectura.LecturaIlegibleScreen
import com.ecovolt.app.ui.lectura.LecturaInicioScreen
import com.ecovolt.app.ui.lectura.LecturaViewModel
import com.ecovolt.app.ui.lectura.PasoLectura
import com.ecovolt.app.ui.lectura.RevisarLecturaScreen
import com.ecovolt.app.ui.login.LoginScreen
import com.ecovolt.app.ui.login.LoginViewModel
import com.ecovolt.app.ui.perfil.CambiarContrasenaScreen
import com.ecovolt.app.ui.perfil.CambiarContrasenaViewModel
import com.ecovolt.app.ui.perfil.ContrasenaActualizadaScreen
import com.ecovolt.app.ui.perfil.PerfilScreen
import com.ecovolt.app.ui.registro.RegistroScreen
import com.ecovolt.app.ui.registro.RegistroViewModel
import com.ecovolt.app.ui.reportes.NuevoReporteScreen
import com.ecovolt.app.ui.reportes.NuevoReporteViewModel
import com.ecovolt.app.ui.reportes.ReporteEnviadoScreen
import com.ecovolt.app.ui.reportes.ReportesScreen
import com.ecovolt.app.ui.reportes.ReportesViewModel
import com.ecovolt.app.ui.theme.EcoColores
import kotlinx.coroutines.flow.StateFlow

// Todo lo que las pantallas necesitan de fuera (repositorios y servicios de la plataforma)
class Dependencias(
    val lecturas: LecturaRepository,
    val auth: AuthRepository,
    val reportes: ReporteRepository,
    val consejos: ConsejoRepository,
    val lector: LectorMedidor,
    val selectorFoto: SelectorFoto,
    val compartidor: Compartidor
)

private const val AVISO_SESION = "Tu sesión expiró o se cerró desde otro dispositivo."

// ViewModels. Llevan el id del usuario en su `key`: viven mientras la app está abierta y así,
// si otra persona inicia sesión en el mismo teléfono, no ve los datos de la anterior.
@Composable
private fun lecturaViewModel(u: Usuario, d: Dependencias): LecturaViewModel =
    viewModel(key = "lectura-${u.id}") { LecturaViewModel(d.lector, d.lecturas) }

@Composable
private fun historialViewModel(u: Usuario, d: Dependencias): HistorialViewModel =
    viewModel(key = "historial-${u.id}") { HistorialViewModel(d.lecturas, d.compartidor) }

@Composable
private fun reportesViewModel(u: Usuario, d: Dependencias): ReportesViewModel =
    viewModel(key = "reportes-${u.id}") { ReportesViewModel(d.reportes) }

@Composable
private fun nuevoReporteViewModel(u: Usuario, d: Dependencias): NuevoReporteViewModel =
    viewModel(key = "nuevo-reporte-${u.id}") { NuevoReporteViewModel(d.selectorFoto, d.reportes) }

@Composable
private fun consejosViewModel(d: Dependencias): ConsejosViewModel =
    viewModel(key = "consejos") { ConsejosViewModel(d.consejos, d.lecturas) }

private fun itemDe(pestana: Pestana, rol: Rol): ItemBarra = when (pestana) {
    Pestana.LECTURA -> ItemBarra("Lectura", EcoIconos.ClipboardCheck)
    Pestana.HISTORIAL -> ItemBarra("Historial", EcoIconos.History)
    Pestana.INICIO -> ItemBarra("Inicio", EcoIconos.House)
    Pestana.REPORTES -> ItemBarra(if (rol.atiendeReportes) "Reportes" else "Reportar", EcoIconos.CircleAlert)
    Pestana.CONSEJOS -> ItemBarra("Consejos", EcoIconos.Lightbulb)
    Pestana.PERFIL -> ItemBarra("Perfil", EcoIconos.User)
}

// Contenido + menú inferior
@Composable
private fun ConBarraInferior(
    usuario: Usuario,
    pestana: Pestana,
    alElegir: (Pestana) -> Unit,
    contenido: @Composable () -> Unit
) {
    val pestanas = pestanasDe(usuario.rol)
    Column(Modifier.fillMaxSize().background(EcoColores.Fondo)) {
        Box(Modifier.weight(1f)) { contenido() }
        BarraInferior(
            items = pestanas.map { itemDe(it, usuario.rol) },
            seleccionado = pestanas.indexOf(pestana).coerceAtLeast(0),
            alElegir = { indice -> alElegir(pestanas[indice]) }
        )
    }
}

// Navegación de toda la app: decide qué pantalla se ve según la pila y si hay sesión.
@Composable
fun AppNavHost(
    d: Dependencias,
    sesionVencida: StateFlow<Boolean>,
    alAtenderSesionVencida: () -> Unit
) {
    val nav = remember { Navegador(Screen.Splash) }
    var usuario by remember { mutableStateOf(d.auth.sesionActual()) }
    var aviso by remember { mutableStateOf<String?>(null) }

    fun principal(u: Usuario, pestana: Pestana? = null) = Screen.Principal(pestana ?: pestanasDe(u.rol).first())

    fun cerrarSesion(mensaje: String?) {
        val personal = usuario?.rol != Rol.ALUMNO
        d.auth.cerrarSesion()
        usuario = null
        aviso = mensaje
        if (mensaje == null) nav.reiniciar(Screen.Bienvenida) else nav.reiniciar(Screen.Bienvenida, Screen.Login(personal))
    }

    // El servidor rechazó el token (venció o se cambió la contraseña en otro dispositivo)
    val vencida by sesionVencida.collectAsState()
    LaunchedEffect(vencida) {
        if (vencida) {
            alAtenderSesionVencida()
            if (usuario != null) cerrarSesion(AVISO_SESION)
        }
    }

    // Botón "atrás": regresa en la pila; en una pestaña que no es la primera, vuelve a la primera
    val actual = nav.actual
    val enPestanaSecundaria = actual is Screen.Principal &&
        usuario?.let { actual.pestana != pestanasDe(it.rol).first() } == true
    ManejadorAtras(habilitado = nav.puedeRetroceder || enPestanaSecundaria) {
        val u = usuario
        if (nav.puedeRetroceder) nav.atras() else if (u != null) nav.reemplazar(principal(u))
    }

    // Pasos de la toma de lectura: los que se repiten se reemplazan para que "atrás" tenga sentido
    val irAPaso: (PasoLectura) -> Unit = { paso ->
        val arriba = nav.actual
        when (paso) {
            PasoLectura.CONFIRMAR, PasoLectura.ILEGIBLE -> {
                val destino = if (paso == PasoLectura.CONFIRMAR) Screen.ConfirmarLectura else Screen.LecturaIlegible
                if (arriba == Screen.ConfirmarLectura || arriba == Screen.LecturaIlegible) nav.reemplazar(destino) else nav.ir(destino)
            }
            PasoLectura.MANUAL ->
                if (arriba == Screen.LecturaIlegible || arriba == Screen.RevisarLectura) nav.reemplazar(Screen.CapturaManual)
                else if (arriba != Screen.CapturaManual) nav.ir(Screen.CapturaManual)
            PasoLectura.REVISAR -> if (arriba != Screen.RevisarLectura) nav.ir(Screen.RevisarLectura)
            PasoLectura.GUARDADA -> nav.reiniciar(Screen.Principal(Pestana.LECTURA), Screen.LecturaGuardada)
        }
    }

    // ---------------------------------------------------------------- Pantallas sin sesión
    when (actual) {
        Screen.Splash -> {
            SplashScreen {
                val u = usuario
                if (u == null) nav.reiniciar(Screen.Bienvenida) else nav.reiniciar(principal(u))
            }
            return
        }
        Screen.Bienvenida -> {
            BienvenidaScreen(
                alEntrarPersonal = { nav.ir(Screen.Login(personal = true)) },
                alEntrarAlumno = { nav.ir(Screen.Login(personal = false)) }
            )
            return
        }
        is Screen.Login -> {
            val vm = viewModel(key = "login") { LoginViewModel(d.auth) }
            LoginScreen(
                viewModel = vm,
                personal = actual.personal,
                aviso = aviso,
                alVolver = {
                    vm.limpiar()
                    aviso = null
                    if (nav.puedeRetroceder) nav.atras() else nav.reiniciar(Screen.Bienvenida)
                },
                alIrARegistro = { nav.ir(Screen.Registro) },
                alEntrar = { u ->
                    usuario = u
                    aviso = null
                    nav.reiniciar(principal(u))
                }
            )
            return
        }
        Screen.Registro -> {
            val vm = viewModel(key = "registro") { RegistroViewModel(d.auth) }
            RegistroScreen(
                viewModel = vm,
                alVolverALogin = {
                    vm.limpiar()
                    nav.atras()
                }
            ) { u ->
                vm.limpiar()
                usuario = u
                nav.reiniciar(principal(u))
            }
            return
        }
        is Screen.ContrasenaActualizada -> {
            ContrasenaActualizadaScreen {
                nav.reiniciar(Screen.Bienvenida, Screen.Login(actual.personal))
            }
            return
        }
        else -> Unit
    }

    // ---------------------------------------------------------------- Pantallas con sesión
    val u = usuario
    if (u == null) {
        LaunchedEffect(Unit) { nav.reiniciar(Screen.Bienvenida) }
        return
    }

    when (actual) {
        is Screen.Principal -> ConBarraInferior(u, actual.pestana, alElegir = { nav.reemplazar(Screen.Principal(it)) }) {
            when (actual.pestana) {
                Pestana.LECTURA -> LecturaInicioScreen(lecturaViewModel(u, d), u, irAPaso)
                Pestana.HISTORIAL -> HistorialScreen(historialViewModel(u, d), subtitulo = u.rol.etiqueta)
                Pestana.INICIO -> InicioScreen(
                    usuario = u,
                    viewModel = historialViewModel(u, d),
                    alReportar = { nav.ir(Screen.NuevoReporte) },
                    alVerConsejos = { nav.reemplazar(Screen.Principal(Pestana.CONSEJOS)) }
                )
                Pestana.REPORTES -> ReportesScreen(u, reportesViewModel(u, d), alNuevoReporte = { nav.ir(Screen.NuevoReporte) })
                Pestana.CONSEJOS -> ConsejosScreen(consejosViewModel(d))
                Pestana.PERFIL -> PerfilScreen(
                    usuario = u,
                    alCambiarContrasena = { nav.ir(Screen.CambiarContrasena) },
                    alVerConsejos = if (Pestana.CONSEJOS in pestanasDe(u.rol)) null else { { nav.ir(Screen.Consejos) } },
                    alCerrarSesion = { cerrarSesion(null) }
                )
            }
        }

        Screen.ConfirmarLectura -> ConfirmarLecturaScreen(lecturaViewModel(u, d), alVolver = nav::atras, alPaso = irAPaso)
        Screen.LecturaIlegible -> LecturaIlegibleScreen(lecturaViewModel(u, d), alVolver = nav::atras, alPaso = irAPaso)
        Screen.CapturaManual -> CapturaManualScreen(lecturaViewModel(u, d), alVolver = nav::atras, alPaso = irAPaso)
        Screen.RevisarLectura -> RevisarLecturaScreen(
            viewModel = lecturaViewModel(u, d),
            alVolver = nav::atras,
            alCorregir = { irAPaso(PasoLectura.MANUAL) },
            alPaso = irAPaso
        )
        Screen.LecturaGuardada -> ConBarraInferior(u, Pestana.LECTURA, alElegir = { nav.reiniciar(Screen.Principal(it)) }) {
            LecturaGuardadaScreen(
                viewModel = lecturaViewModel(u, d),
                alVerHistorial = { nav.reiniciar(Screen.Principal(Pestana.HISTORIAL)) },
                alOtraLectura = { nav.reiniciar(Screen.Principal(Pestana.LECTURA)) }
            )
        }

        Screen.NuevoReporte -> NuevoReporteScreen(
            viewModel = nuevoReporteViewModel(u, d),
            alVolver = nav::atras,
            alEnviar = { reporte -> nav.reemplazar(Screen.ReporteEnviado(reporte)) }
        )
        is Screen.ReporteEnviado -> ReporteEnviadoScreen(
            reporte = actual.reporte,
            alVerReportes = { nav.reiniciar(Screen.Principal(Pestana.REPORTES)) },
            alVolverAlInicio = { nav.reiniciar(principal(u)) }
        )

        Screen.Consejos -> ConsejosScreen(consejosViewModel(d), alVolver = nav::atras)

        Screen.CambiarContrasena -> {
            val vm = viewModel(key = "contrasena-${u.id}") { CambiarContrasenaViewModel(d.auth) }
            CambiarContrasenaScreen(
                viewModel = vm,
                alVolver = {
                    vm.limpiar()
                    nav.atras()
                },
                alCambiada = {
                    // El servidor ya cerró todas las sesiones y el repositorio borró la de este teléfono
                    val personal = u.rol != Rol.ALUMNO
                    usuario = null
                    nav.reiniciar(Screen.Bienvenida, Screen.ContrasenaActualizada(personal))
                },
                alSesionVencida = { cerrarSesion(AVISO_SESION) }
            )
        }

        // Las pantallas sin sesión ya se atendieron arriba
        Screen.Splash, Screen.Bienvenida, Screen.Registro, is Screen.Login, is Screen.ContrasenaActualizada -> Unit
    }
}

package com.ecovolt.app.navigation

import androidx.compose.runtime.mutableStateListOf
import com.ecovolt.app.model.Reporte
import com.ecovolt.app.model.Rol

// Secciones del menú inferior
enum class Pestana { LECTURA, HISTORIAL, INICIO, REPORTES, CONSEJOS, PERFIL }

// Quien toma lecturas (director y responsable) ve Lectura e Historial;
// alumnos y docentes ven un Inicio con el resumen y los Consejos.
fun pestanasDe(rol: Rol): List<Pestana> =
    if (rol.capturaLecturas) {
        listOf(Pestana.LECTURA, Pestana.HISTORIAL, Pestana.REPORTES, Pestana.PERFIL)
    } else {
        listOf(Pestana.INICIO, Pestana.REPORTES, Pestana.CONSEJOS, Pestana.PERFIL)
    }

// Pantallas de la app. Para agregar una nueva solo se suma un objeto aquí y su rama en AppNavHost.
sealed interface Screen {
    // Sin sesión
    data object Splash : Screen
    data object Bienvenida : Screen
    data class Login(val personal: Boolean) : Screen          // personal = acceso de adultos
    data object Registro : Screen
    data class ContrasenaActualizada(val personal: Boolean) : Screen

    // Con sesión: las secciones con menú inferior
    data class Principal(val pestana: Pestana) : Screen

    // Toma de lectura
    data object ConfirmarLectura : Screen
    data object LecturaIlegible : Screen
    data object CapturaManual : Screen
    data object RevisarLectura : Screen
    data object LecturaGuardada : Screen

    // Reportes (RFN-19)
    data object NuevoReporte : Screen
    data class ReporteEnviado(val reporte: Reporte) : Screen

    // Otras
    data object Consejos : Screen        // RFN-25, cuando no es una pestaña
    data object CambiarContrasena : Screen
}

// Pila de pantallas: la última es la que se ve. "Atrás" quita la última.
class Navegador(inicial: Screen) {
    val pila = mutableStateListOf<Screen>(inicial)

    val actual: Screen get() = pila.last()
    val puedeRetroceder: Boolean get() = pila.size > 1

    fun ir(pantalla: Screen) {
        pila.add(pantalla)
    }

    fun reemplazar(pantalla: Screen) {
        pila[pila.lastIndex] = pantalla
    }

    // Borra el historial y deja solo estas pantallas (por ejemplo, al iniciar o cerrar sesión)
    fun reiniciar(vararg pantallas: Screen) {
        pila.clear()
        pila.addAll(pantallas.toList())
    }

    fun atras() {
        if (pila.size > 1) pila.removeAt(pila.lastIndex)
    }
}

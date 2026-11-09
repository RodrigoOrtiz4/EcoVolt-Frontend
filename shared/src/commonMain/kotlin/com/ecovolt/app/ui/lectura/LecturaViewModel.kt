package com.ecovolt.app.ui.lectura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecovolt.app.data.remote.SesionExpiradaException
import com.ecovolt.app.data.remote.ServidorRechazoException
import com.ecovolt.app.data.repository.LecturaRepository
import com.ecovolt.app.model.Lectura
import com.ecovolt.app.model.MomentoLectura
import com.ecovolt.app.model.OrigenLectura
import com.ecovolt.app.service.CicloLectura
import com.ecovolt.app.service.CicloSemanal
import com.ecovolt.app.service.ExtractorLectura
import com.ecovolt.app.service.LectorMedidor
import com.ecovolt.app.service.ResultadoCaptura
import com.ecovolt.app.service.Validador
import com.ecovolt.app.service.fechaHoy
import com.ecovolt.app.service.numeroDeTexto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Pasos de la toma de lectura. El ViewModel decide el siguiente y la navegación lo muestra.
enum class PasoLectura { CONFIRMAR, ILEGIBLE, MANUAL, REVISAR, GUARDADA }

// Todo lo que las pantallas de lectura necesitan mostrar. Un solo estado para todo el flujo:
// así el número leído, la foto y la nota sobreviven al ir y venir entre pantallas.
class LecturaUiState(
    val lecturas: List<Lectura> = emptyList(),      // en orden cronológico
    val ciclo: CicloLectura? = null,
    val sincronizando: Boolean = false,
    val momento: MomentoLectura = MomentoLectura.VIERNES_CIERRE,
    val revisados: Set<Int> = emptySet(),           // consejos de "Antes de tomar la foto" ya marcados
    val leyendo: Boolean = false,                   // la cámara o el OCR están trabajando
    val foto: ByteArray? = null,                    // foto del medidor (solo para mostrarla)
    val candidatos: List<String> = emptyList(),     // números que encontró el OCR
    val origen: OrigenLectura = OrigenLectura.MANUAL,
    val valor: String = "",
    val editandoValor: Boolean = false,             // "Editar" en la pantalla de confirmar
    val errorValor: String? = null,
    val cambioMedidor: Boolean? = null,             // respuesta a "¿Se cambió o reinició el medidor?"
    val nota: String = "",
    val errorNota: String? = null,
    val guardando: Boolean = false,
    val mensaje: String? = null,                    // error para mostrar en la pantalla actual
    val guardada: Lectura? = null,                  // la última lectura guardada (pantalla de éxito)
    val consumoGuardada: Double? = null             // diferencia contra la lectura anterior a esa
) {
    val anterior: Lectura? get() = lecturas.lastOrNull()

    val valorNumero: Double? get() = numeroDeTexto(valor)?.takeIf { it > 0.0 && it < 10_000_000.0 }

    // Diferencia contra la lectura anterior mientras se escribe ("Diferencia calculada")
    val diferencia: Double? get() {
        val numero = valorNumero ?: return null
        val previa = anterior ?: return null
        return numero - previa.valorKwh
    }

    val esMenorQueAnterior: Boolean get() = (diferencia ?: 0.0) < 0.0

    fun copy(
        lecturas: List<Lectura> = this.lecturas,
        ciclo: CicloLectura? = this.ciclo,
        sincronizando: Boolean = this.sincronizando,
        momento: MomentoLectura = this.momento,
        revisados: Set<Int> = this.revisados,
        leyendo: Boolean = this.leyendo,
        foto: ByteArray? = this.foto,
        candidatos: List<String> = this.candidatos,
        origen: OrigenLectura = this.origen,
        valor: String = this.valor,
        editandoValor: Boolean = this.editandoValor,
        errorValor: String? = this.errorValor,
        cambioMedidor: Boolean? = this.cambioMedidor,
        nota: String = this.nota,
        errorNota: String? = this.errorNota,
        guardando: Boolean = this.guardando,
        mensaje: String? = this.mensaje,
        guardada: Lectura? = this.guardada,
        consumoGuardada: Double? = this.consumoGuardada
    ) = LecturaUiState(
        lecturas, ciclo, sincronizando, momento, revisados, leyendo, foto, candidatos, origen, valor,
        editandoValor, errorValor, cambioMedidor, nota, errorNota, guardando, mensaje, guardada, consumoGuardada
    )
}

class LecturaViewModel(
    private val lector: LectorMedidor,
    private val repository: LecturaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(LecturaUiState())
    val estado: StateFlow<LecturaUiState> = _estado.asStateFlow()

    init {
        // Primero lo guardado en el teléfono (sirve sin internet) y luego lo del servidor
        aplicarLecturas(repository.lecturasGuardadas(), elegirMomento = true)
        sincronizar()
    }

    fun sincronizar() {
        if (_estado.value.sincronizando) return
        viewModelScope.launch {
            _estado.update { it.copy(sincronizando = true) }
            repository.sincronizar()
                .onSuccess { lecturas -> aplicarLecturas(lecturas, elegirMomento = true) }
            _estado.update { it.copy(sincronizando = false) }
        }
    }

    // Recalcula el ciclo semanal. Si `elegirMomento`, propone la lectura que toca (cierre o apertura).
    private fun aplicarLecturas(lecturas: List<Lectura>, elegirMomento: Boolean) {
        val ciclo = CicloSemanal.calcular(lecturas, fechaHoy())
        _estado.update {
            it.copy(
                lecturas = lecturas,
                ciclo = ciclo,
                momento = if (elegirMomento && ciclo != null) ciclo.momento else it.momento
            )
        }
    }

    fun elegirMomento(momento: MomentoLectura) {
        _estado.update { it.copy(momento = momento) }
    }

    fun alternarRevisado(indice: Int) {
        _estado.update {
            it.copy(revisados = if (indice in it.revisados) it.revisados - indice else it.revisados + indice)
        }
    }

    fun cerrarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    // Limpia lo de la lectura anterior antes de empezar otra
    private fun reiniciarCaptura(origen: OrigenLectura) {
        _estado.update {
            it.copy(
                foto = null, candidatos = emptyList(), origen = origen, valor = "", editandoValor = false,
                errorValor = null, cambioMedidor = null, nota = "", errorNota = null, mensaje = null
            )
        }
    }

    // 1) Foto  2) OCR  3) Si se encontró un número → confirmar; si no → "No pudimos leer la foto"
    fun tomarFoto(irA: (PasoLectura) -> Unit) {
        val actual = _estado.value
        if (actual.leyendo || actual.guardando) return
        reiniciarCaptura(OrigenLectura.OCR)
        _estado.update { it.copy(leyendo = true) }

        lector.capturar { resultado ->
            when (resultado) {
                is ResultadoCaptura.Texto -> {
                    val candidatos = ExtractorLectura.candidatos(resultado.texto)
                    _estado.update {
                        it.copy(
                            leyendo = false,
                            foto = resultado.foto,
                            candidatos = candidatos,
                            valor = candidatos.firstOrNull() ?: "",
                            origen = OrigenLectura.OCR
                        )
                    }
                    irA(if (candidatos.isEmpty()) PasoLectura.ILEGIBLE else PasoLectura.CONFIRMAR)
                }
                ResultadoCaptura.Cancelado -> _estado.update { it.copy(leyendo = false) }
                is ResultadoCaptura.Error -> _estado.update { it.copy(leyendo = false, mensaje = resultado.mensaje) }
            }
        }
    }

    // "Captura manual" desde el inicio o desde "No pudimos leer la foto"
    fun iniciarManual() {
        val conservarFoto = _estado.value.foto
        reiniciarCaptura(OrigenLectura.MANUAL)
        _estado.update { it.copy(foto = conservarFoto) }
    }

    // "Corregir número": se escribe a mano, pero la lectura sigue viniendo de una foto
    fun corregirNumero() {
        _estado.update { it.copy(editandoValor = false, errorValor = null, mensaje = null) }
    }

    fun alternarEdicion() {
        _estado.update { it.copy(editandoValor = !it.editandoValor) }
    }

    fun alCambiarValor(valor: String) {
        if (valor.length > 12 || !valor.all { it.isDigit() || it == '.' || it == ',' }) return
        _estado.update { it.copy(valor = valor, errorValor = null, mensaje = null) }
    }

    fun usarCandidato(candidato: String) {
        _estado.update { it.copy(valor = candidato, errorValor = null, editandoValor = false) }
    }

    fun elegirCambioMedidor(cambio: Boolean) {
        _estado.update { it.copy(cambioMedidor = cambio, errorNota = null) }
    }

    fun alCambiarNota(nota: String) {
        if (nota.length > 300) return
        _estado.update { it.copy(nota = nota, errorNota = null) }
    }

    // "Confirmar y guardar" / "Guardar lectura". Si el número es menor que el anterior,
    // primero se pasa por "Revisar lectura" para corregirlo o explicar el cambio de medidor.
    fun guardar(irA: (PasoLectura) -> Unit) {
        val actual = _estado.value
        if (actual.leyendo || actual.guardando) return

        val error = errorDeValor(actual.valor)
        val valor = actual.valorNumero
        if (error != null || valor == null) {
            _estado.update { it.copy(errorValor = error ?: "Revisa el número", editandoValor = true) }
            return
        }
        if (actual.esMenorQueAnterior) {
            _estado.update { it.copy(cambioMedidor = false, nota = "", errorNota = null, mensaje = null) }
            irA(PasoLectura.REVISAR)
            return
        }
        enviar(valor, nota = null, irA = irA)
    }

    // "Guardar con nota" (en Revisar lectura): solo si se cambió el medidor y se explicó
    fun guardarConNota(irA: (PasoLectura) -> Unit) {
        val actual = _estado.value
        if (actual.guardando) return
        val valor = actual.valorNumero ?: return
        if (actual.cambioMedidor != true) {
            _estado.update { it.copy(errorNota = "Si el medidor no cambió, corrige el número de la lectura.") }
            return
        }
        val nota = actual.nota.trim()
        if (nota.length < 10) {
            _estado.update { it.copy(errorNota = "Explica el cambio con al menos 10 caracteres.") }
            return
        }
        enviar(valor, nota, irA)
    }

    private fun errorDeValor(valor: String): String? {
        if (valor.isBlank()) return "Escribe o elige el número del medidor"
        val numero = numeroDeTexto(valor) ?: return "Escribe solo números (puedes usar un punto decimal)"
        return Validador.validarLectura(numero.toString())
    }

    private fun enviar(valor: Double, nota: String?, irA: (PasoLectura) -> Unit) {
        val actual = _estado.value
        val anterior = actual.anterior
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, mensaje = null) }
            repository.agregarLectura(actual.momento, valor, fechaHoy(), actual.origen, nota)
                .onSuccess { guardada ->
                    aplicarLecturas(repository.lecturasGuardadas(), elegirMomento = true)
                    _estado.update {
                        it.copy(
                            guardando = false,
                            guardada = guardada,
                            consumoGuardada = anterior?.let { previa -> guardada.valorKwh - previa.valorKwh },
                            foto = null,
                            candidatos = emptyList(),
                            valor = "",
                            nota = "",
                            cambioMedidor = null,
                            revisados = emptySet()
                        )
                    }
                    irA(PasoLectura.GUARDADA)
                }
                .onFailure { causa ->
                    // Se conserva el número para reintentar sin volver a tomar la foto
                    if (causa is ServidorRechazoException && causa.codigo == 409) {
                        // El servidor tiene una lectura más reciente y mayor: hay que revisar
                        _estado.update { it.copy(guardando = false, mensaje = causa.message, cambioMedidor = false) }
                        sincronizar()
                        irA(PasoLectura.REVISAR)
                        return@onFailure
                    }
                    val mensaje = when (causa) {
                        is SesionExpiradaException -> "Tu sesión expiró. Vuelve a iniciar sesión para guardar."
                        is ServidorRechazoException -> causa.message ?: "El servidor rechazó la lectura."
                        else -> "No se pudo conectar con el servidor. Revisa tu conexión e intenta de nuevo."
                    }
                    _estado.update { it.copy(guardando = false, mensaje = mensaje) }
                }
        }
    }
}

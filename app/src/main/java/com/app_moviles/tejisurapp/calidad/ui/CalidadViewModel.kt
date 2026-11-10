package com.app_moviles.tejisurapp.calidad.ui

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.calidad.model.AnalisisInspeccion
import com.app_moviles.tejisurapp.calidad.model.CatalogoCalidad
import com.app_moviles.tejisurapp.calidad.model.CausaDefecto
import com.app_moviles.tejisurapp.calidad.model.DatosDemoCalidad
import com.app_moviles.tejisurapp.calidad.model.Defecto
import com.app_moviles.tejisurapp.calidad.model.DisposicionDefecto
import com.app_moviles.tejisurapp.calidad.model.EstadoIncidencia
import com.app_moviles.tejisurapp.calidad.model.InspeccionCalidad
import com.app_moviles.tejisurapp.calidad.model.TipoDefecto
import com.app_moviles.tejisurapp.produccion.ui.Evento
import java.time.LocalDateTime
import java.util.Locale

/**
 * Controlador de EP02 - Control de Calidad (HU04 / HU05 / HU06).
 *
 * Mantiene los catálogos de Calidad, el estado de los formularios, los errores
 * de validación con @StringRes, las listas observables (LiveData) de
 * inspecciones y defectos registrados en memoria, y el generador secuencial de IDs.
 */
class CalidadViewModel : ViewModel() {

    // ---------------------------------------------------------------------
    // Estado del formulario de Inspección (HU04)
    // ---------------------------------------------------------------------

    data class InspeccionFormState(
        val otIndex: Int = SIN_SELECCION,
        val loteIndex: Int = SIN_SELECCION,
        val operarioIndex: Int = SIN_SELECCION,
        val inspeccionadas: String = "",
        val aprobadas: String = "",
        val rechazadas: String = "",
        val enReproceso: String = ""
    )

    data class InspeccionErrores(
        @StringRes val ot: Int? = null,
        @StringRes val lote: Int? = null,
        @StringRes val operario: Int? = null,
        @StringRes val inspeccionadas: Int? = null,
        @StringRes val aprobadas: Int? = null,
        @StringRes val rechazadas: Int? = null,
        @StringRes val enReproceso: Int? = null,
        @StringRes val suma: Int? = null
    ) {
        val hayErrores: Boolean
            get() = listOf(ot, lote, operario, inspeccionadas, aprobadas, rechazadas, enReproceso, suma).any { it != null }
    }

    // ---------------------------------------------------------------------
    // Catálogos
    // ---------------------------------------------------------------------

    val ordenesTrabajo: List<String> = CatalogoCalidad.ordenesTrabajo

    val operarios: List<String> = CatalogoCalidad.operarios

    val tiposDefecto: List<TipoDefecto> = TipoDefecto.entries

    val causasDefecto: List<CausaDefecto> = CausaDefecto.entries

    val disposiciones: List<DisposicionDefecto> = DisposicionDefecto.entries

    val estadosIncidencia: List<EstadoIncidencia> = EstadoIncidencia.entries

    fun lotesDe(otIndex: Int): List<String> = CatalogoCalidad.lotesDe(otIndex)

    fun lotesDe(ot: String): List<String> = CatalogoCalidad.lotesDe(ot)

    // ---------------------------------------------------------------------
    // Registros en memoria (LiveData)
    // ---------------------------------------------------------------------

    private val inicioSesion = LocalDateTime.now()

    private val _inspecciones = MutableLiveData<List<InspeccionCalidad>>(
        if (CARGAR_DATOS_DEMO) DatosDemoCalidad.inspecciones(inicioSesion) else emptyList()
    )
    val inspecciones: LiveData<List<InspeccionCalidad>> = _inspecciones

    private val _defectos = MutableLiveData<List<Defecto>>(
        if (CARGAR_DATOS_DEMO) DatosDemoCalidad.defectos(inicioSesion) else emptyList()
    )
    val defectos: LiveData<List<Defecto>> = _defectos

    // ---------------------------------------------------------------------
    // Generador de IDs
    // ---------------------------------------------------------------------

    private var siguienteInspeccionId: Long =
        if (CARGAR_DATOS_DEMO) DatosDemoCalidad.SIGUIENTE_INSPECCION_ID else 1L

    private var siguienteDefectoId: Long =
        if (CARGAR_DATOS_DEMO) DatosDemoCalidad.SIGUIENTE_DEFECTO_ID else 1L

    fun generarIdInspeccion(): String =
        String.format(Locale.ROOT, "INS-%04d", siguienteInspeccionId++)

    fun generarIdDefecto(): String =
        String.format(Locale.ROOT, "DEF-%04d", siguienteDefectoId++)

    // =====================================================================
    // HU04 - Gestión del Formulario de Inspección de Calidad
    // =====================================================================

    var inspeccionForm = InspeccionFormState()
        private set

    private val _erroresInspeccion = MutableLiveData(InspeccionErrores())
    val erroresInspeccion: LiveData<InspeccionErrores> = _erroresInspeccion

    /** Indicador en vivo "Suma: X de Y" para el balance de control. */
    private val _indicadorSuma = MutableLiveData("Suma: 0 de 0")
    val indicadorSuma: LiveData<String> = _indicadorSuma

    /** Insignia en vivo de resultado: "Sin rechazo ni reproceso", "Con rechazo o reproceso" o null. */
    private val _resultadoEnVivo = MutableLiveData<String?>(null)
    val resultadoEnVivo: LiveData<String?> = _resultadoEnVivo

    /** Evento emitido cuando se registra una inspección con rechazo o reproceso > 0 (HU05). */
    private val _solicitudRegistroCausa = MutableLiveData<Evento<InspeccionCalidad>>()
    val solicitudRegistroCausa: LiveData<Evento<InspeccionCalidad>> = _solicitudRegistroCausa

    fun onInspeccionOtSeleccionada(index: Int) {
        if (index == inspeccionForm.otIndex) return
        inspeccionForm = inspeccionForm.copy(otIndex = index, loteIndex = SIN_SELECCION)
        _erroresInspeccion.value = erroresInspeccionActuales().copy(ot = null, lote = null)
    }

    fun onInspeccionLoteSeleccionado(index: Int) {
        if (index == inspeccionForm.loteIndex) return
        inspeccionForm = inspeccionForm.copy(loteIndex = index)
        _erroresInspeccion.value = erroresInspeccionActuales().copy(lote = null)
    }

    fun onInspeccionOperarioSeleccionado(index: Int) {
        if (index == inspeccionForm.operarioIndex) return
        inspeccionForm = inspeccionForm.copy(operarioIndex = index)
        _erroresInspeccion.value = erroresInspeccionActuales().copy(operario = null)
    }

    fun onInspeccionInspeccionadasCambiada(texto: String) {
        if (texto == inspeccionForm.inspeccionadas) return
        inspeccionForm = inspeccionForm.copy(inspeccionadas = texto)
        _erroresInspeccion.value = erroresInspeccionActuales().copy(inspeccionadas = null, suma = null)
        actualizarBalanceEnVivo()
    }

    fun onInspeccionAprobadasCambiada(texto: String) {
        if (texto == inspeccionForm.aprobadas) return
        inspeccionForm = inspeccionForm.copy(aprobadas = texto)
        _erroresInspeccion.value = erroresInspeccionActuales().copy(aprobadas = null, suma = null)
        actualizarBalanceEnVivo()
    }

    fun onInspeccionRechazadasCambiada(texto: String) {
        if (texto == inspeccionForm.rechazadas) return
        inspeccionForm = inspeccionForm.copy(rechazadas = texto)
        _erroresInspeccion.value = erroresInspeccionActuales().copy(rechazadas = null, suma = null)
        actualizarBalanceEnVivo()
    }

    fun onInspeccionReprocesoCambiada(texto: String) {
        if (texto == inspeccionForm.enReproceso) return
        inspeccionForm = inspeccionForm.copy(enReproceso = texto)
        _erroresInspeccion.value = erroresInspeccionActuales().copy(enReproceso = null, suma = null)
        actualizarBalanceEnVivo()
    }

    private fun actualizarBalanceEnVivo() {
        val insp = AnalisisInspeccion.parsearEntero(inspeccionForm.inspeccionadas) ?: 0
        val apr = AnalisisInspeccion.parsearEntero(inspeccionForm.aprobadas) ?: 0
        val rech = AnalisisInspeccion.parsearEntero(inspeccionForm.rechazadas) ?: 0
        val repr = AnalisisInspeccion.parsearEntero(inspeccionForm.enReproceso) ?: 0
        val suma = apr + rech + repr

        _indicadorSuma.value = "Suma: $suma de $insp"

        _resultadoEnVivo.value = if (insp > 0 && suma == insp) {
            AnalisisInspeccion.calcularResultado(apr, rech, repr)
        } else {
            null
        }
    }

    private fun erroresInspeccionActuales() = _erroresInspeccion.value ?: InspeccionErrores()

    /**
     * Valida y registra una nueva inspección de calidad (HU04-T04).
     * Devuelve la entidad creada si el formulario es válido, o null si contiene errores.
     */
    fun registrarInspeccion(): InspeccionCalidad? {
        val form = inspeccionForm
        val ot = ordenesTrabajo.getOrNull(form.otIndex)
        val lote = lotesDe(form.otIndex).getOrNull(form.loteIndex)
        val operario = operarios.getOrNull(form.operarioIndex)

        val validacion = AnalisisInspeccion.validar(
            ot = ot,
            lote = lote,
            operario = operario,
            inspeccionadasStr = form.inspeccionadas,
            aprobadasStr = form.aprobadas,
            rechazadasStr = form.rechazadas,
            enReprocesoStr = form.enReproceso
        )

        val errorOtRes = R.string.error_ot_requerida.takeIf { validacion.errores.ot != null }
        val errorLoteRes = R.string.error_lote_requerido.takeIf { validacion.errores.lote != null }
        val errorOperarioRes = R.string.error_operario_requerido.takeIf { validacion.errores.operario != null }

        val errorInspRes = when (validacion.errores.inspeccionadas) {
            AnalisisInspeccion.TipoError.CAMPO_REQUERIDO -> R.string.error_inspeccionadas_requeridas
            AnalisisInspeccion.TipoError.DEBE_SER_MAYOR_A_CERO -> R.string.error_inspeccionadas_mayor_cero
            AnalisisInspeccion.TipoError.NUMERO_INVALIDO -> R.string.error_cantidad_entera_invalida
            else -> null
        }

        val errorAprRes = when (validacion.errores.aprobadas) {
            AnalisisInspeccion.TipoError.CAMPO_REQUERIDO -> R.string.error_aprobadas_requeridas
            AnalisisInspeccion.TipoError.NUMERO_INVALIDO -> R.string.error_cantidad_entera_invalida
            else -> null
        }

        val errorRechRes = when (validacion.errores.rechazadas) {
            AnalisisInspeccion.TipoError.CAMPO_REQUERIDO -> R.string.error_rechazadas_requeridas
            AnalisisInspeccion.TipoError.NUMERO_INVALIDO -> R.string.error_cantidad_entera_invalida
            else -> null
        }

        val errorReprRes = when (validacion.errores.enReproceso) {
            AnalisisInspeccion.TipoError.CAMPO_REQUERIDO -> R.string.error_reproceso_requerido
            AnalisisInspeccion.TipoError.NUMERO_INVALIDO -> R.string.error_cantidad_entera_invalida
            else -> null
        }

        val errorSumaRes = R.string.error_suma_inspeccion.takeIf { validacion.errores.suma != null }

        val errores = InspeccionErrores(
            ot = errorOtRes,
            lote = errorLoteRes,
            operario = errorOperarioRes,
            inspeccionadas = errorInspRes,
            aprobadas = errorAprRes,
            rechazadas = errorRechRes,
            enReproceso = errorReprRes,
            suma = errorSumaRes
        )

        _erroresInspeccion.value = errores

        if (!validacion.esValida || ot == null || lote == null || operario == null) {
            return null
        }

        val inspeccionadas = AnalisisInspeccion.parsearEntero(form.inspeccionadas) ?: return null
        val aprobadas = AnalisisInspeccion.parsearEntero(form.aprobadas) ?: return null
        val rechazadas = AnalisisInspeccion.parsearEntero(form.rechazadas) ?: return null
        val enReproceso = AnalisisInspeccion.parsearEntero(form.enReproceso) ?: return null

        val nuevaInspeccion = InspeccionCalidad(
            id = generarIdInspeccion(),
            ordenTrabajo = ot,
            lote = lote,
            operario = operario,
            inspeccionadas = inspeccionadas,
            aprobadas = aprobadas,
            rechazadas = rechazadas,
            enReproceso = enReproceso,
            fechaHora = LocalDateTime.now(),
            supervisor = SUPERVISOR_SESION
        )

        _inspecciones.value = _inspecciones.value.orEmpty() + nuevaInspeccion

        if (rechazadas + enReproceso > 0) {
            _solicitudRegistroCausa.value = Evento(nuevaInspeccion)
        }

        limpiarFormularioInspeccion()
        return nuevaInspeccion
    }

    fun limpiarFormularioInspeccion() {
        inspeccionForm = InspeccionFormState()
        _erroresInspeccion.value = InspeccionErrores()
        actualizarBalanceEnVivo()
    }

    // ---------------------------------------------------------------------
    // Operaciones directas sobre registros
    // ---------------------------------------------------------------------

    fun registrarInspeccion(inspeccion: InspeccionCalidad) {
        _inspecciones.value = _inspecciones.value.orEmpty() + inspeccion
    }

    fun registrarDefecto(defecto: Defecto) {
        _defectos.value = _defectos.value.orEmpty() + defecto
    }

    fun defectosDeLote(ot: String, lote: String): List<Defecto> =
        _defectos.value.orEmpty().filter { it.ordenTrabajo == ot && it.lote == lote }

    fun defectosDeInspeccion(inspeccionId: String): List<Defecto> =
        _defectos.value.orEmpty().filter { it.inspeccionId == inspeccionId }

    companion object {
        const val SIN_SELECCION = -1

        /** Supervisor asignado para el control de calidad en el turno actual. */
        const val SUPERVISOR_SESION = "Jorge Paredes"

        /** Operario por defecto para la sesión de calidad. */
        const val OPERARIO_SESION = "Juan Pérez"

        /** Carga datos de ejemplo para la demo de EP02. */
        const val CARGAR_DATOS_DEMO = true
    }
}

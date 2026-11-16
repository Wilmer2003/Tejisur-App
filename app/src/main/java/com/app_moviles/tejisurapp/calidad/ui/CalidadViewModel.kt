package com.app_moviles.tejisurapp.calidad.ui

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.calidad.model.AlertaReincidencia
import com.app_moviles.tejisurapp.calidad.model.AnalisisInspeccion
import com.app_moviles.tejisurapp.calidad.model.CatalogoCalidad
import com.app_moviles.tejisurapp.calidad.model.CausaDefecto
import com.app_moviles.tejisurapp.calidad.model.DatosDemoCalidad
import com.app_moviles.tejisurapp.calidad.model.Defecto
import com.app_moviles.tejisurapp.calidad.model.DisposicionDefecto
import com.app_moviles.tejisurapp.calidad.model.EstadoIncidencia
import com.app_moviles.tejisurapp.calidad.model.InspeccionCalidad
import com.app_moviles.tejisurapp.calidad.model.RastreadorReincidencias
import com.app_moviles.tejisurapp.calidad.model.TipoDefecto
import com.app_moviles.tejisurapp.produccion.model.ConfiguracionPlanta
import com.app_moviles.tejisurapp.produccion.ui.Evento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
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

    data class DefectoForm(
        val tipo: TipoDefecto? = null,
        val causa: CausaDefecto? = null,
        val disposicion: DisposicionDefecto? = null,
        val cantidad: String = "",
        val esGrupoAprobado: Boolean = false,
        val idGrupo: Long,
        val foto: FotoDefecto? = null
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

    var causasInspeccion: List<DefectoForm> = emptyList()
        private set

    private val _errorCausas = MutableLiveData<Int?>(null)
    val errorCausas: LiveData<Int?> = _errorCausas

    val umbralReincidencia = ConfiguracionPlanta().umbralReincidencia
    private val rastreador = RastreadorReincidencias(umbralReincidencia)

    private val _alertasReincidencia = MutableLiveData<List<AlertaReincidencia>>(emptyList())
    val alertasReincidencia: LiveData<List<AlertaReincidencia>> = _alertasReincidencia

    private val _nuevasAlertas = MutableLiveData<Evento<List<AlertaReincidencia>>>()
    val nuevasAlertas: LiveData<Evento<List<AlertaReincidencia>>> = _nuevasAlertas

    private data class SolicitudFoto(val idGrupo: Long, val foto: FotoDefecto)

    private var siguienteGrupoId = 1L
    private var solicitudFoto: SolicitudFoto? = null
    private val fotosRegistradas = mutableListOf<FotoDefecto>()

    private val _fotoOcupada = MutableLiveData(false)
    val fotoOcupada: LiveData<Boolean> = _fotoOcupada

    private val _resultadoFoto = MutableLiveData<Evento<Boolean>>()
    val resultadoFoto: LiveData<Evento<Boolean>> = _resultadoFoto

    fun iniciarSolicitudFoto(idGrupo: Long, foto: FotoDefecto) {
        solicitudFoto = SolicitudFoto(idGrupo, foto)
        _fotoOcupada.value = true
    }

    fun cancelarSolicitudFoto() {
        solicitudFoto?.foto?.archivo?.delete()
        solicitudFoto = null
        _fotoOcupada.value = false
    }

    fun recibirFoto(contexto: Context, seleccion: Uri? = null) {
        val solicitud = solicitudFoto ?: return
        viewModelScope.launch {
            val foto = solicitud.foto
            try {
                val valida = withContext(Dispatchers.IO) {
                    if (seleccion != null) foto.importar(contexto, seleccion)
                    decodificarFoto(contexto, foto.uri, 128) != null
                }
                val indice = causasInspeccion.indexOfFirst { it.idGrupo == solicitud.idGrupo }
                if (valida && indice >= 0) {
                    val grupo = causasInspeccion[indice]
                    actualizarCausa(indice, grupo.copy(foto = foto))
                    grupo.foto?.archivo?.delete()
                    solicitudFoto = null
                    _resultadoFoto.value = Evento(true)
                } else if (!valida) {
                    _resultadoFoto.value = Evento(false)
                }
            } catch (_: IOException) {
                _resultadoFoto.value = Evento(false)
            } catch (_: SecurityException) {
                _resultadoFoto.value = Evento(false)
            } finally {
                cancelarSolicitudFoto()
            }
        }
    }

    fun quitarFoto(indice: Int) {
        val grupo = causasInspeccion.getOrNull(indice) ?: return
        grupo.foto?.archivo?.delete()
        actualizarCausa(indice, grupo.copy(foto = null))
    }

    fun agregarCausa(esGrupoAprobado: Boolean = false) {
        causasInspeccion = causasInspeccion + DefectoForm(
            esGrupoAprobado = esGrupoAprobado, idGrupo = siguienteGrupoId++
        )
        _errorCausas.value = null
    }

    fun actualizarCausa(indice: Int, causa: DefectoForm) {
        if (causasInspeccion.getOrNull(indice) == causa) return
        causasInspeccion = causasInspeccion.toMutableList().apply { set(indice, causa) }
        _errorCausas.value = null
    }

    fun quitarCausa(indice: Int) {
        causasInspeccion.getOrNull(indice)?.foto?.archivo?.delete()
        causasInspeccion = causasInspeccion.toMutableList().apply { removeAt(indice) }
        _errorCausas.value = null
    }

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
        val suma = apr.toLong() + rech + repr

        _indicadorSuma.value = "Suma: $suma de $insp"

        _resultadoEnVivo.value = if (insp > 0 && suma == insp.toLong()) {
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

        val tieneDefectos = rechazadas > 0 || enReproceso > 0
        val grupos = if (tieneDefectos) causasInspeccion else emptyList()
        val cantidades = grupos.map { AnalisisInspeccion.parsearEntero(it.cantidad) ?: 0 }
        if (grupos.zip(cantidades).any { (grupo, cantidad) ->
                cantidad <= 0 || (!grupo.esGrupoAprobado &&
                    (grupo.tipo == null || grupo.causa == null || grupo.disposicion == null))
            }
        ) {
            _errorCausas.value = R.string.error_grupo_incompleto
            return null
        }

        if (tieneDefectos) {
            val aprobadasEnGrupos = grupos.indices.filter { grupos[it].esGrupoAprobado }
                .sumOf { cantidades[it].toLong() }
            val rechazadasEnGrupos = grupos.indices
                .filter { grupos[it].disposicion == DisposicionDefecto.RECHAZO }
                .sumOf { cantidades[it].toLong() }
            val enReprocesoEnGrupos = cantidades.sumOf { it.toLong() } - aprobadasEnGrupos - rechazadasEnGrupos
            if (aprobadasEnGrupos != aprobadas.toLong() || rechazadasEnGrupos != rechazadas.toLong() ||
                enReprocesoEnGrupos != enReproceso.toLong()
            ) {
                _errorCausas.value = R.string.error_cantidad_grupos
                return null
            }
        }

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

        val nuevosDefectos = grupos.mapIndexedNotNull { indice, causa ->
            if (causa.esGrupoAprobado) return@mapIndexedNotNull null
            Defecto(
                id = generarIdDefecto(),
                inspeccionId = nuevaInspeccion.id,
                ordenTrabajo = ot,
                lote = lote,
                tipo = checkNotNull(causa.tipo),
                causa = checkNotNull(causa.causa),
                disposicion = checkNotNull(causa.disposicion),
                prendasAfectadas = cantidades[indice],
                estado = EstadoIncidencia.PENDIENTE,
                fotoUri = causa.foto?.uri?.toString(),
                fechaHora = nuevaInspeccion.fechaHora
            )
        }
        fotosRegistradas += grupos.mapNotNull { it.foto }
        if (!tieneDefectos) causasInspeccion.forEach { it.foto?.archivo?.delete() }
        _inspecciones.value = _inspecciones.value.orEmpty() + nuevaInspeccion
        if (nuevosDefectos.isNotEmpty()) {
            _defectos.value = _defectos.value.orEmpty() + nuevosDefectos
        }

        val secuencia = if (tieneDefectos) {
            grupos.mapIndexed { indice, grupo -> RastreadorReincidencias.Grupo(grupo.tipo, cantidades[indice]) }
        } else {
            listOf(RastreadorReincidencias.Grupo(null, aprobadas))
        }
        val alertas = rastreador.registrar(nuevaInspeccion, secuencia)
        if (alertas.isNotEmpty()) {
            _alertasReincidencia.value = alertas + _alertasReincidencia.value.orEmpty()
            _nuevasAlertas.value = Evento(alertas)
        }

        limpiarFormularioInspeccion()
        return nuevaInspeccion
    }

    fun limpiarFormularioInspeccion() {
        inspeccionForm = InspeccionFormState()
        causasInspeccion = emptyList()
        _errorCausas.value = null
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

    override fun onCleared() {
        solicitudFoto?.foto?.archivo?.delete()
        causasInspeccion.forEach { it.foto?.archivo?.delete() }
        fotosRegistradas.forEach { it.archivo.delete() }
        super.onCleared()
    }

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

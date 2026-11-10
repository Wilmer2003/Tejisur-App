package com.app_moviles.tejisurapp.calidad.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app_moviles.tejisurapp.calidad.model.CatalogoCalidad
import com.app_moviles.tejisurapp.calidad.model.CausaDefecto
import com.app_moviles.tejisurapp.calidad.model.DatosDemoCalidad
import com.app_moviles.tejisurapp.calidad.model.Defecto
import com.app_moviles.tejisurapp.calidad.model.DisposicionDefecto
import com.app_moviles.tejisurapp.calidad.model.EstadoIncidencia
import com.app_moviles.tejisurapp.calidad.model.InspeccionCalidad
import com.app_moviles.tejisurapp.calidad.model.TipoDefecto
import java.time.LocalDateTime
import java.util.Locale

/**
 * Controlador de EP02 - Control de Calidad.
 *
 * Mantiene los catálogos de Calidad, las listas observables (LiveData) de
 * inspecciones y defectos registrados en memoria, y el generador secuencial de IDs.
 */
class CalidadViewModel : ViewModel() {

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

    // ---------------------------------------------------------------------
    // Operaciones sobre registros
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

        /** Operario por defecto para la sesión de calidad. */
        const val OPERARIO_SESION = "Juan Pérez"

        /** Carga datos de ejemplo para la demo de EP02. */
        const val CARGAR_DATOS_DEMO = true
    }
}


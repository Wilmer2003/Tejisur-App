package com.app_moviles.tejisurapp.calidad

import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.calidad.model.AlertaReincidencia
import com.app_moviles.tejisurapp.calidad.model.CatalogoCalidad
import com.app_moviles.tejisurapp.calidad.model.CausaDefecto
import com.app_moviles.tejisurapp.calidad.model.Defecto
import com.app_moviles.tejisurapp.calidad.model.DisposicionDefecto
import com.app_moviles.tejisurapp.calidad.model.EstadoIncidencia
import com.app_moviles.tejisurapp.calidad.model.InspeccionCalidad
import com.app_moviles.tejisurapp.calidad.model.TipoDefecto
import com.app_moviles.tejisurapp.calidad.ui.CalidadViewModel
import com.app_moviles.tejisurapp.produccion.model.ConfiguracionPlanta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class CalidadBaseTest {

    @Test
    fun configuracionPlanta_umbralReincidenciaPorDefectoYValidaciones() {
        val config = ConfiguracionPlanta()
        assertEquals(5.0, config.umbralMermaPorcentaje, 0.001)
        assertEquals(3, config.umbralReincidencia)

        val configPersonalizada = ConfiguracionPlanta(umbralMermaPorcentaje = 6.0, umbralReincidencia = 4)
        assertEquals(4, configPersonalizada.umbralReincidencia)

        var errorLanzado = false
        try {
            ConfiguracionPlanta(umbralReincidencia = 1)
        } catch (e: IllegalArgumentException) {
            errorLanzado = true
        }
        assertTrue("Debe lanzar excepción si el umbral de reincidencia es < 2", errorLanzado)
    }

    @Test
    fun enumsCalidad_contienenValoresYEtiquetasCorrectas() {
        assertEquals(2, DisposicionDefecto.entries.size)
        assertEquals(R.string.disposicion_rechazo, DisposicionDefecto.RECHAZO.etiquetaRes)
        assertEquals(R.string.disposicion_reproceso, DisposicionDefecto.REPROCESO.etiquetaRes)

        assertEquals(2, EstadoIncidencia.entries.size)
        assertEquals(R.string.estado_incidencia_pendiente, EstadoIncidencia.PENDIENTE.etiquetaRes)
        assertEquals(R.string.estado_incidencia_atendida, EstadoIncidencia.ATENDIDA.etiquetaRes)

        assertEquals(5, TipoDefecto.entries.size)
        assertEquals(R.string.tipo_defecto_costura_abierta, TipoDefecto.COSTURA_ABIERTA.etiquetaRes)
        assertEquals(R.string.tipo_defecto_mancha_suciedad, TipoDefecto.MANCHA_SUCIEDAD.etiquetaRes)
        assertEquals(R.string.tipo_defecto_medida_fuera_tolerancia, TipoDefecto.MEDIDA_FUERA_TOLERANCIA.etiquetaRes)
        assertEquals(R.string.tipo_defecto_tela_fallada, TipoDefecto.TELA_FALLADA.etiquetaRes)
        assertEquals(R.string.tipo_defecto_hilo_suelto, TipoDefecto.HILO_SUELTO.etiquetaRes)

        assertEquals(6, CausaDefecto.entries.size)
        assertEquals(R.string.causa_defecto_maquina_descalibrada, CausaDefecto.MAQUINA_DESCALIBRADA.etiquetaRes)
        assertEquals(R.string.causa_defecto_aguja_desgastada, CausaDefecto.AGUJA_DESGASTADA.etiquetaRes)
        assertEquals(R.string.causa_defecto_tension_hilo_incorrecta, CausaDefecto.TENSION_HILO_INCORRECTA.etiquetaRes)
        assertEquals(R.string.causa_defecto_error_operario, CausaDefecto.ERROR_OPERARIO.etiquetaRes)
        assertEquals(R.string.causa_defecto_material_defectuoso, CausaDefecto.MATERIAL_DEFECTUOSO.etiquetaRes)
        assertEquals(R.string.causa_defecto_otra_causa, CausaDefecto.OTRA_CAUSA.etiquetaRes)
    }

    @Test
    fun catalogoCalidad_coincideConProduccion() {
        assertEquals(listOf("OT-2026-001", "OT-2026-002", "OT-2026-003"), CatalogoCalidad.ordenesTrabajo)
        assertEquals(listOf("L-0101", "L-0102", "L-0103"), CatalogoCalidad.lotesDe("OT-2026-001"))
        assertEquals(listOf("L-0201", "L-0202"), CatalogoCalidad.lotesDe("OT-2026-002"))
        assertEquals(listOf("L-0301", "L-0302", "L-0303", "L-0304"), CatalogoCalidad.lotesDe("OT-2026-003"))
        assertEquals(listOf("Juan Pérez", "María Quispe", "Luis Rojas"), CatalogoCalidad.operarios)
    }

    @Test
    fun dataClasses_validanInvariantes() {
        val ahora = LocalDateTime.now()
        val inspeccion = InspeccionCalidad(
            id = "INS-0001",
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            operario = "Juan Pérez",
            inspeccionadas = 10,
            aprobadas = 8,
            rechazadas = 1,
            enReproceso = 1,
            fechaHora = ahora,
            supervisor = "Carlos Morales"
        )
        assertEquals(10, inspeccion.inspeccionadas)

        val defecto = Defecto(
            id = "DEF-0001",
            inspeccionId = "INS-0001",
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            tipo = TipoDefecto.COSTURA_ABIERTA,
            causa = CausaDefecto.TENSION_HILO_INCORRECTA,
            disposicion = DisposicionDefecto.REPROCESO,
            prendasAfectadas = 1,
            estado = EstadoIncidencia.PENDIENTE,
            fotoUri = null,
            fechaHora = ahora
        )
        assertEquals(1, defecto.prendasAfectadas)

        val alerta = AlertaReincidencia(
            tipoDefecto = TipoDefecto.COSTURA_ABIERTA,
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            repeticiones = 3,
            umbral = 3,
            fechaHora = ahora
        )
        assertEquals(3L, alerta.repeticiones)
    }

    @Test
    fun calidadViewModel_cargaDatosDemoYGeneraIds() {
        val vm = CalidadViewModel()
        assertTrue(CalidadViewModel.CARGAR_DATOS_DEMO)

        val defectos = vm.defectos.value
        assertNotNull(defectos)
        assertEquals(3, defectos?.size)

        val d1 = defectos?.firstOrNull { it.inspeccionId == "INS-0007" }
        assertNotNull(d1)
        assertEquals(TipoDefecto.COSTURA_ABIERTA, d1?.tipo)
        assertEquals(DisposicionDefecto.REPROCESO, d1?.disposicion)
        assertEquals(EstadoIncidencia.PENDIENTE, d1?.estado)
        assertEquals(4, d1?.prendasAfectadas)

        val d2 = defectos?.firstOrNull { it.inspeccionId == "INS-0006" }
        assertNotNull(d2)
        assertEquals(TipoDefecto.MANCHA_SUCIEDAD, d2?.tipo)
        assertEquals(DisposicionDefecto.REPROCESO, d2?.disposicion)
        assertEquals(EstadoIncidencia.ATENDIDA, d2?.estado)
        assertEquals(2, d2?.prendasAfectadas)

        val d3 = defectos?.firstOrNull { it.inspeccionId == "INS-0004" }
        assertNotNull(d3)
        assertEquals(TipoDefecto.TELA_FALLADA, d3?.tipo)
        assertEquals(DisposicionDefecto.RECHAZO, d3?.disposicion)
        assertEquals(EstadoIncidencia.PENDIENTE, d3?.estado)
        assertEquals(1, d3?.prendasAfectadas)

        val idIns = vm.generarIdInspeccion()
        assertEquals("INS-0008", idIns)

        val idDef = vm.generarIdDefecto()
        assertEquals("DEF-0004", idDef)
    }
}


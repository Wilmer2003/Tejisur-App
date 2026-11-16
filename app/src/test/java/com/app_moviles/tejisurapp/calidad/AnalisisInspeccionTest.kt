package com.app_moviles.tejisurapp.calidad

import com.app_moviles.tejisurapp.calidad.model.AnalisisInspeccion
import com.app_moviles.tejisurapp.calidad.model.AnalisisInspeccion.TipoError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalisisInspeccionTest {

    @Test
    fun validar_camposVacios_detectaErroresRequeridos() {
        val resultado = AnalisisInspeccion.validar(
            ot = "",
            lote = null,
            operario = "   ",
            inspeccionadas = null,
            aprobadas = null,
            rechazadas = null,
            enReproceso = null
        )

        assertFalse(resultado.esValida)
        assertTrue(resultado.errores.hayErrores)
        assertEquals(TipoError.CAMPO_REQUERIDO, resultado.errores.ot)
        assertEquals(TipoError.CAMPO_REQUERIDO, resultado.errores.lote)
        assertEquals(TipoError.CAMPO_REQUERIDO, resultado.errores.operario)
        assertEquals(TipoError.CAMPO_REQUERIDO, resultado.errores.inspeccionadas)
        assertEquals(TipoError.CAMPO_REQUERIDO, resultado.errores.aprobadas)
        assertEquals(TipoError.CAMPO_REQUERIDO, resultado.errores.rechazadas)
        assertEquals(TipoError.CAMPO_REQUERIDO, resultado.errores.enReproceso)
        assertNull(resultado.resultado)
    }

    @Test
    fun validar_sumaQueNoCuadra_detectaErrorDeSuma() {
        val resultado = AnalisisInspeccion.validar(
            ot = "OT-2026-001",
            lote = "L-0101",
            operario = "Juan Pérez",
            inspeccionadas = 100,
            aprobadas = 90,
            rechazadas = 5,
            enReproceso = 2 // 90 + 5 + 2 = 97 != 100
        )

        assertFalse(resultado.esValida)
        assertTrue(resultado.errores.hayErrores)
        assertNull(resultado.errores.ot)
        assertNull(resultado.errores.lote)
        assertNull(resultado.errores.operario)
        assertNull(resultado.errores.inspeccionadas)
        assertNull(resultado.errores.aprobadas)
        assertNull(resultado.errores.rechazadas)
        assertNull(resultado.errores.enReproceso)
        assertEquals(TipoError.SUMA_NO_COINCIDE, resultado.errores.suma)
        assertNull(resultado.resultado)
    }

    @Test
    fun validar_inspeccionadasCero_detectaDebeSerMayorACero() {
        val resultado = AnalisisInspeccion.validar(
            ot = "OT-2026-001",
            lote = "L-0101",
            operario = "Juan Pérez",
            inspeccionadas = 0,
            aprobadas = 0,
            rechazadas = 0,
            enReproceso = 0
        )

        assertFalse(resultado.esValida)
        assertEquals(TipoError.DEBE_SER_MAYOR_A_CERO, resultado.errores.inspeccionadas)
        assertNull(resultado.resultado)
    }

    @Test
    fun validar_inspeccionAprobadaSinRechazo_calculaResultadoSinDefectos() {
        val resultado = AnalisisInspeccion.validar(
            ot = "OT-2026-001",
            lote = "L-0101",
            operario = "Juan Pérez",
            inspeccionadas = 50,
            aprobadas = 50,
            rechazadas = 0,
            enReproceso = 0
        )

        assertTrue(resultado.esValida)
        assertFalse(resultado.errores.hayErrores)
        assertNull(resultado.errores.suma)
        assertEquals(AnalisisInspeccion.RESULTADO_SIN_DEFECTOS, resultado.resultado)
        assertEquals("Sin rechazo ni reproceso", resultado.resultado)
    }

    @Test
    fun validar_inspeccionConRechazoYConReproceso_calculaResultadoConDefectos() {
        val resultado = AnalisisInspeccion.validar(
            ot = "OT-2026-001",
            lote = "L-0101",
            operario = "Juan Pérez",
            inspeccionadas = 50,
            aprobadas = 44,
            rechazadas = 4,
            enReproceso = 2
        )

        assertTrue(resultado.esValida)
        assertFalse(resultado.errores.hayErrores)
        assertNull(resultado.errores.suma)
        assertEquals(AnalisisInspeccion.RESULTADO_CON_DEFECTOS, resultado.resultado)
        assertEquals("Con rechazo o reproceso", resultado.resultado)
    }

    @Test
    fun validar_sobrecargaCadenas_funcionaIgual() {
        val resultado = AnalisisInspeccion.validar(
            ot = "OT-2026-001",
            lote = "L-0101",
            operario = "Juan Pérez",
            inspeccionadasStr = "60",
            aprobadasStr = "56",
            rechazadasStr = "0",
            enReprocesoStr = "4"
        )

        assertTrue(resultado.esValida)
        assertEquals(AnalisisInspeccion.RESULTADO_CON_DEFECTOS, resultado.resultado)
    }
}


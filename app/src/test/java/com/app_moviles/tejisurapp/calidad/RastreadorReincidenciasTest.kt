package com.app_moviles.tejisurapp.calidad

import com.app_moviles.tejisurapp.calidad.model.InspeccionCalidad
import com.app_moviles.tejisurapp.calidad.model.RastreadorReincidencias
import com.app_moviles.tejisurapp.calidad.model.RastreadorReincidencias.Grupo
import com.app_moviles.tejisurapp.calidad.model.TipoDefecto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class RastreadorReincidenciasTest {

    private val rastreador = RastreadorReincidencias(umbral = 3)
    private val costura = TipoDefecto.COSTURA_ABIERTA
    private val mancha = TipoDefecto.MANCHA_SUCIEDAD

    @Test
    fun detectaCruceEntreInspeccionesYEvitaDuplicarLaMismaRacha() {
        assertTrue(registrar(Grupo(costura, 2)).isEmpty())
        val alerta = registrar(Grupo(costura, 2)).single()
        assertEquals(costura, alerta.tipoDefecto)
        assertEquals(4L, alerta.repeticiones)
        assertEquals("L-0101", alerta.lote)
        assertTrue(registrar(Grupo(costura, 1)).isEmpty())
    }

    @Test
    fun unaPrendaAprobadaInterrumpeLaRachaYPermiteAlertarDeNuevo() {
        assertTrue(registrar(Grupo(costura, 2), Grupo(null, 1), Grupo(costura, 1)).isEmpty())
        assertEquals(3L, registrar(Grupo(costura, 2)).single().repeticiones)
        registrar(Grupo(null, 1))
        assertEquals(3L, registrar(Grupo(costura, 3)).single().repeticiones)
    }

    @Test
    fun separaTiposYLotes() {
        registrar(Grupo(costura, 2))
        assertTrue(registrar(Grupo(costura, 1), lote = "L-0102").isEmpty())
        assertTrue(registrar(Grupo(mancha, 1), Grupo(costura, 2)).isEmpty())
        val alertas = registrar(Grupo(costura, 1), Grupo(mancha, 3))
        assertEquals(listOf(costura, mancha), alertas.map { it.tipoDefecto })
    }

    private fun registrar(vararg grupos: Grupo, lote: String = "L-0101") =
        rastreador.registrar(
            InspeccionCalidad(
                id = "INS-0001",
                ordenTrabajo = "OT-2026-001",
                lote = lote,
                operario = "Operario",
                inspeccionadas = grupos.sumOf { it.cantidad },
                aprobadas = grupos.filter { it.tipo == null }.sumOf { it.cantidad },
                rechazadas = grupos.filter { it.tipo != null }.sumOf { it.cantidad },
                enReproceso = 0,
                fechaHora = LocalDateTime.of(2026, 11, 16, 12, 0),
                supervisor = "Supervisor"
            ),
            grupos.toList()
        )
}

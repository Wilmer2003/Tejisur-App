package com.app_moviles.tejisurapp.calidad.model

import java.time.LocalDateTime

/**
 * Registros de ejemplo para la demo de EP02 (Control de Calidad).
 *
 * Demo para L-0101 en OT-2026-001:
 * - INS-0007: Costura abierta, Reproceso, Pendiente, 4 prendas
 * - INS-0006: Mancha o suciedad, Reproceso, Atendida, 2 prendas
 * - INS-0004: Tela fallada, Rechazo, Pendiente, 1 prenda
 */
internal object DatosDemoCalidad {

    private const val SUPERVISOR_DEMO = "Carlos Morales"

    fun inspecciones(ahora: LocalDateTime): List<InspeccionCalidad> = listOf(
        InspeccionCalidad(
            id = "INS-0004",
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            operario = "Juan Pérez",
            inspeccionadas = 50,
            aprobadas = 49,
            rechazadas = 1,
            enReproceso = 0,
            fechaHora = ahora.minusDays(1),
            supervisor = SUPERVISOR_DEMO
        ),
        InspeccionCalidad(
            id = "INS-0006",
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            operario = "María Quispe",
            inspeccionadas = 40,
            aprobadas = 38,
            rechazadas = 0,
            enReproceso = 2,
            fechaHora = ahora.minusHours(5),
            supervisor = SUPERVISOR_DEMO
        ),
        InspeccionCalidad(
            id = "INS-0007",
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            operario = "Luis Rojas",
            inspeccionadas = 60,
            aprobadas = 56,
            rechazadas = 0,
            enReproceso = 4,
            fechaHora = ahora.minusHours(2),
            supervisor = SUPERVISOR_DEMO
        )
    )

    fun defectos(ahora: LocalDateTime): List<Defecto> = listOf(
        Defecto(
            id = "DEF-0001",
            inspeccionId = "INS-0007",
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            tipo = TipoDefecto.COSTURA_ABIERTA,
            causa = CausaDefecto.TENSION_HILO_INCORRECTA,
            disposicion = DisposicionDefecto.REPROCESO,
            prendasAfectadas = 4,
            estado = EstadoIncidencia.PENDIENTE,
            fotoUri = null,
            fechaHora = ahora.minusHours(2)
        ),
        Defecto(
            id = "DEF-0002",
            inspeccionId = "INS-0006",
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            tipo = TipoDefecto.MANCHA_SUCIEDAD,
            causa = CausaDefecto.ERROR_OPERARIO,
            disposicion = DisposicionDefecto.REPROCESO,
            prendasAfectadas = 2,
            estado = EstadoIncidencia.ATENDIDA,
            fotoUri = null,
            fechaHora = ahora.minusHours(5)
        ),
        Defecto(
            id = "DEF-0003",
            inspeccionId = "INS-0004",
            ordenTrabajo = "OT-2026-001",
            lote = "L-0101",
            tipo = TipoDefecto.TELA_FALLADA,
            causa = CausaDefecto.MATERIAL_DEFECTUOSO,
            disposicion = DisposicionDefecto.RECHAZO,
            prendasAfectadas = 1,
            estado = EstadoIncidencia.PENDIENTE,
            fotoUri = null,
            fechaHora = ahora.minusDays(1)
        )
    )

    /** Primer id secuencial libre para inspecciones generadas por formularios. */
    const val SIGUIENTE_INSPECCION_ID = 8L

    /** Primer id secuencial libre para defectos generados por formularios. */
    const val SIGUIENTE_DEFECTO_ID = 4L
}


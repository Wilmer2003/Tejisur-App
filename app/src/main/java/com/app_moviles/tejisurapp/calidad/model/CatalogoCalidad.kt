package com.app_moviles.tejisurapp.calidad.model

/**
 * Catálogo maestro para el módulo de Control de Calidad (EP02).
 *
 * Mantiene las mismas Órdenes de Trabajo y lotes que Producción, además del
 * personal operativo para las inspecciones de calidad.
 *
 * TODO (Sprint 3): Unificar este catálogo con ProduccionViewModel y el repositorio central
 * para evitar duplicidad de fuentes de datos.
 */
object CatalogoCalidad {

    val lotesPorOt: Map<String, List<String>> = linkedMapOf(
        "OT-2026-001" to listOf("L-0101", "L-0102", "L-0103"),
        "OT-2026-002" to listOf("L-0201", "L-0202"),
        "OT-2026-003" to listOf("L-0301", "L-0302", "L-0303", "L-0304")
    )

    val ordenesTrabajo: List<String> = lotesPorOt.keys.toList()

    val operarios: List<String> = listOf(
        "Juan Pérez",
        "María Quispe",
        "Luis Rojas"
    )

    fun lotesDe(otIndex: Int): List<String> =
        ordenesTrabajo.getOrNull(otIndex)?.let { lotesPorOt[it] }.orEmpty()

    fun lotesDe(ot: String): List<String> =
        lotesPorOt[ot].orEmpty()
}


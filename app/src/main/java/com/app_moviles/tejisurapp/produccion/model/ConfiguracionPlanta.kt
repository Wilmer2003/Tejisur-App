package com.app_moviles.tejisurapp.produccion.model

/**
 * HU16 - Parámetros que define el área de Planta.
 *
 * El umbral se centraliza aquí para que la regla de alerta, la leyenda de la
 * consulta (HU03) y el color de cada lote usen exactamente el mismo valor.
 * En Sprints futuros podrá leerse de un repositorio o de la configuración remota.
 */
data class ConfiguracionPlanta(
    /** Porcentaje de merma tolerable por lote (merma / consumo × 100). */
    val umbralMermaPorcentaje: Double = UMBRAL_MERMA_POR_DEFECTO,
    /**
     * Umbral de repeticiones para alertar reincidencia de un defecto en EP02.
     * Parámetro PROVISIONAL para el Sprint 2.
     */
    val umbralReincidencia: Int = UMBRAL_REINCIDENCIA_POR_DEFECTO
) {
    init {
        require(umbralMermaPorcentaje > 0.0 && umbralMermaPorcentaje < 100.0) {
            "El umbral de merma debe estar entre 0 y 100 %"
        }
        require(umbralReincidencia >= 2) {
            "El umbral de reincidencia debe ser al menos 2"
        }
    }

    companion object {
        const val UMBRAL_MERMA_POR_DEFECTO = 5.0
        /** Umbral provisional de reincidencias de defecto por lote (EP02). */
        const val UMBRAL_REINCIDENCIA_POR_DEFECTO = 3
    }
}

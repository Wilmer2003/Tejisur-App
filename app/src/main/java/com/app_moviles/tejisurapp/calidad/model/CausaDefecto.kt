package com.app_moviles.tejisurapp.calidad.model

import androidx.annotation.StringRes
import com.app_moviles.tejisurapp.R

/**
 * Causa raíz atribuible a la generación de un defecto (EP02).
 *
 * NOTA: Este catálogo es PROVISIONAL para el Sprint 2 y queda sujeto a
 * validación definitiva por parte del área de Calidad y Planta.
 */
enum class CausaDefecto(@StringRes val etiquetaRes: Int) {
    MAQUINA_DESCALIBRADA(R.string.causa_defecto_maquina_descalibrada),
    AGUJA_DESGASTADA(R.string.causa_defecto_aguja_desgastada),
    TENSION_HILO_INCORRECTA(R.string.causa_defecto_tension_hilo_incorrecta),
    ERROR_OPERARIO(R.string.causa_defecto_error_operario),
    MATERIAL_DEFECTUOSO(R.string.causa_defecto_material_defectuoso),
    OTRA_CAUSA(R.string.causa_defecto_otra_causa)
}


package com.app_moviles.tejisurapp.calidad.model

import androidx.annotation.StringRes
import com.app_moviles.tejisurapp.R

/**
 * Tipos de defecto identificados durante el control de calidad (EP02).
 *
 * NOTA: Este catálogo es PROVISIONAL para el Sprint 2 y queda sujeto a
 * validación definitiva por parte del área de Calidad y Planta.
 */
enum class TipoDefecto(@StringRes val etiquetaRes: Int) {
    COSTURA_ABIERTA(R.string.tipo_defecto_costura_abierta),
    MANCHA_SUCIEDAD(R.string.tipo_defecto_mancha_suciedad),
    MEDIDA_FUERA_TOLERANCIA(R.string.tipo_defecto_medida_fuera_tolerancia),
    TELA_FALLADA(R.string.tipo_defecto_tela_fallada),
    HILO_SUELTO(R.string.tipo_defecto_hilo_suelto)
}


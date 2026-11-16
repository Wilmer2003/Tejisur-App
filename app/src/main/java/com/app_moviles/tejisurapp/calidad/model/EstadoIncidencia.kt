package com.app_moviles.tejisurapp.calidad.model

import androidx.annotation.StringRes
import com.app_moviles.tejisurapp.R

/**
 * Estado de seguimiento de la incidencia o defecto detectado (EP02).
 */
enum class EstadoIncidencia(@StringRes val etiquetaRes: Int) {
    PENDIENTE(R.string.estado_incidencia_pendiente),
    ATENDIDA(R.string.estado_incidencia_atendida)
}


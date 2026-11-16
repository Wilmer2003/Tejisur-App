package com.app_moviles.tejisurapp.calidad.model

import androidx.annotation.StringRes
import com.app_moviles.tejisurapp.R

/**
 * Disposición final o tratamiento asignado a un defecto detectado (EP02).
 */
enum class DisposicionDefecto(@StringRes val etiquetaRes: Int) {
    RECHAZO(R.string.disposicion_rechazo),
    REPROCESO(R.string.disposicion_reproceso)
}


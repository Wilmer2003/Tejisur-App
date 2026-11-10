package com.app_moviles.tejisurapp.calidad.model

import java.time.LocalDateTime

/**
 * EP02 - Registro de un defecto identificado durante una inspección de calidad.
 */
data class Defecto(
    val id: String,
    val inspeccionId: String,
    val ordenTrabajo: String,
    val lote: String,
    val tipo: TipoDefecto,
    val causa: CausaDefecto,
    val disposicion: DisposicionDefecto,
    val prendasAfectadas: Int,
    val estado: EstadoIncidencia,
    val fotoUri: String? = null,
    val fechaHora: LocalDateTime
) {
    init {
        require(prendasAfectadas > 0) { "Las prendas afectadas deben ser mayor a 0" }
    }
}


package com.app_moviles.tejisurapp.calidad.model

import java.time.LocalDateTime

/**
 * EP02 - Registro de inspección de calidad sobre un lote y orden de trabajo.
 * Entidad inmutable generada al completar una sesión de control de calidad.
 */
data class InspeccionCalidad(
    val id: String,
    val ordenTrabajo: String,
    val lote: String,
    val operario: String,
    val inspeccionadas: Int,
    val aprobadas: Int,
    val rechazadas: Int,
    val enReproceso: Int,
    val fechaHora: LocalDateTime,
    val supervisor: String
) {
    init {
        require(inspeccionadas > 0) { "La cantidad inspeccionada debe ser mayor a 0" }
        require(aprobadas >= 0) { "La cantidad aprobada debe ser mayor o igual a 0" }
        require(rechazadas >= 0) { "La cantidad rechazada debe ser mayor o igual a 0" }
        require(enReproceso >= 0) { "La cantidad en reproceso debe ser mayor o igual a 0" }
        require(aprobadas + rechazadas + enReproceso == inspeccionadas) {
            "La suma de aprobadas, rechazadas y en reproceso debe ser igual a la cantidad inspeccionada"
        }
    }
}

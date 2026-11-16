package com.app_moviles.tejisurapp.calidad.model

import java.time.LocalDateTime

/**
 * EP02 - Alerta generada cuando un mismo tipo de defecto alcanza o supera el umbral
 * de repeticiones establecido por Planta dentro de un mismo lote y orden de trabajo.
 */
data class AlertaReincidencia(
    val tipoDefecto: TipoDefecto,
    val ordenTrabajo: String,
    val lote: String,
    val repeticiones: Long,
    val umbral: Int,
    val fechaHora: LocalDateTime
) {
    init {
        require(repeticiones >= umbral) { "Las repeticiones deben alcanzar o superar el umbral" }
        require(umbral >= 2) { "El umbral debe ser al menos 2" }
    }
}

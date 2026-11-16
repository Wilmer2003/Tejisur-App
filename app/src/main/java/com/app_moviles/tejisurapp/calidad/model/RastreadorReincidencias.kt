package com.app_moviles.tejisurapp.calidad.model

internal class RastreadorReincidencias(private val umbral: Int) {

    data class Grupo(val tipo: TipoDefecto?, val cantidad: Int)
    private data class Racha(val tipo: TipoDefecto, val cantidad: Int)

    private val rachasPorLote = mutableMapOf<Pair<String, String>, Racha>()

    fun registrar(inspeccion: InspeccionCalidad, grupos: List<Grupo>): List<AlertaReincidencia> {
        val clave = inspeccion.ordenTrabajo to inspeccion.lote
        var racha = rachasPorLote[clave]
        val alertas = buildList {
            for ((tipo, cantidad) in grupos) {
                if (tipo == null) {
                    racha = null
                    continue
                }

                val anteriores = racha?.takeIf { it.tipo == tipo }?.cantidad ?: 0
                val total = anteriores.toLong() + cantidad
                if (anteriores < umbral && total >= umbral) {
                    add(
                        AlertaReincidencia(
                            tipo, inspeccion.ordenTrabajo, inspeccion.lote,
                            total, umbral, inspeccion.fechaHora
                        )
                    )
                }
                racha = Racha(tipo, total.coerceAtMost(umbral.toLong()).toInt())
            }
        }

        val ultimaRacha = racha
        if (ultimaRacha == null) rachasPorLote.remove(clave)
        else rachasPorLote[clave] = ultimaRacha
        return alertas
    }
}

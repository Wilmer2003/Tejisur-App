package com.app_moviles.tejisurapp.calidad.model

/**
 * HU04-T04 - Reglas de negocio y validación de inspección de calidad.
 *
 * Lógica pura en Kotlin (sin dependencias del framework Android) para permitir
 * su ejecución en pruebas unitarias locales y su uso desde el ViewModel.
 */
object AnalisisInspeccion {

    const val RESULTADO_SIN_DEFECTOS = "Sin rechazo ni reproceso"
    const val RESULTADO_CON_DEFECTOS = "Con rechazo o reproceso"

    enum class TipoError {
        CAMPO_REQUERIDO,
        NUMERO_INVALIDO,
        DEBE_SER_MAYOR_A_CERO,
        SUMA_NO_COINCIDE
    }

    data class ErroresCampos(
        val ot: TipoError? = null,
        val lote: TipoError? = null,
        val operario: TipoError? = null,
        val inspeccionadas: TipoError? = null,
        val aprobadas: TipoError? = null,
        val rechazadas: TipoError? = null,
        val enReproceso: TipoError? = null,
        val suma: TipoError? = null
    ) {
        val hayErrores: Boolean
            get() = listOf(ot, lote, operario, inspeccionadas, aprobadas, rechazadas, enReproceso, suma).any { it != null }
    }

    data class ResultadoValidacion(
        val errores: ErroresCampos,
        val resultado: String?
    ) {
        val esValida: Boolean get() = !errores.hayErrores
    }

    /**
     * Valida los datos de una inspección con valores enteros y devuelve los errores por campo
     * y el resultado de la inspección.
     */
    fun validar(
        ot: String?,
        lote: String?,
        operario: String?,
        inspeccionadas: Int?,
        aprobadas: Int?,
        rechazadas: Int?,
        enReproceso: Int?
    ): ResultadoValidacion {
        val errorOt = if (ot.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else null
        val errorLote = if (lote.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else null
        val errorOperario = if (operario.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else null

        val errorInspeccionadas = when {
            inspeccionadas == null -> TipoError.CAMPO_REQUERIDO
            inspeccionadas < 0 -> TipoError.NUMERO_INVALIDO
            inspeccionadas == 0 -> TipoError.DEBE_SER_MAYOR_A_CERO
            else -> null
        }

        val errorAprobadas = when {
            aprobadas == null -> TipoError.CAMPO_REQUERIDO
            aprobadas < 0 -> TipoError.NUMERO_INVALIDO
            else -> null
        }

        val errorRechazadas = when {
            rechazadas == null -> TipoError.CAMPO_REQUERIDO
            rechazadas < 0 -> TipoError.NUMERO_INVALIDO
            else -> null
        }

        val errorReproceso = when {
            enReproceso == null -> TipoError.CAMPO_REQUERIDO
            enReproceso < 0 -> TipoError.NUMERO_INVALIDO
            else -> null
        }

        val cantidadesValidas = errorInspeccionadas == null &&
                errorAprobadas == null &&
                errorRechazadas == null &&
                errorReproceso == null

        val errorSuma = if (cantidadesValidas) {
            val suma = aprobadas!! + rechazadas!! + enReproceso!!
            if (suma != inspeccionadas!!) TipoError.SUMA_NO_COINCIDE else null
        } else {
            null
        }

        val errores = ErroresCampos(
            ot = errorOt,
            lote = errorLote,
            operario = errorOperario,
            inspeccionadas = errorInspeccionadas,
            aprobadas = errorAprobadas,
            rechazadas = errorRechazadas,
            enReproceso = errorReproceso,
            suma = errorSuma
        )

        val resultado = if (!errores.hayErrores) {
            calcularResultado(aprobadas!!, rechazadas!!, enReproceso!!)
        } else {
            null
        }

        return ResultadoValidacion(errores, resultado)
    }

    /**
     * Sobrecarga que permite validar cadenas de texto directamente desde los formularios.
     */
    fun validar(
        ot: String?,
        lote: String?,
        operario: String?,
        inspeccionadasStr: String?,
        aprobadasStr: String?,
        rechazadasStr: String?,
        enReprocesoStr: String?
    ): ResultadoValidacion {
        val insp = parsearEntero(inspeccionadasStr)
        val apr = parsearEntero(aprobadasStr)
        val rech = parsearEntero(rechazadasStr)
        val repr = parsearEntero(enReprocesoStr)

        val errorInsp = if (inspeccionadasStr.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else if (insp == null) TipoError.NUMERO_INVALIDO else null
        val errorApr = if (aprobadasStr.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else if (apr == null) TipoError.NUMERO_INVALIDO else null
        val errorRech = if (rechazadasStr.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else if (rech == null) TipoError.NUMERO_INVALIDO else null
        val errorRepr = if (enReprocesoStr.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else if (repr == null) TipoError.NUMERO_INVALIDO else null

        if (errorInsp != null || errorApr != null || errorRech != null || errorRepr != null) {
            val errorOt = if (ot.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else null
            val errorLote = if (lote.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else null
            val errorOperario = if (operario.isNullOrBlank()) TipoError.CAMPO_REQUERIDO else null
            val errores = ErroresCampos(
                ot = errorOt,
                lote = errorLote,
                operario = errorOperario,
                inspeccionadas = errorInsp,
                aprobadas = errorApr,
                rechazadas = errorRech,
                enReproceso = errorRepr,
                suma = null
            )
            return ResultadoValidacion(errores, null)
        }

        return validar(ot, lote, operario, insp, apr, rech, repr)
    }

    /**
     * Calcula la etiqueta de resultado según las cantidades finales aprobadas, rechazadas y en reproceso.
     */
    fun calcularResultado(aprobadas: Int, rechazadas: Int, enReproceso: Int): String {
        return if (rechazadas == 0 && enReproceso == 0) {
            RESULTADO_SIN_DEFECTOS
        } else {
            RESULTADO_CON_DEFECTOS
        }
    }

    /**
     * Parsea un texto a Int >= 0; devuelve null si está vacío o no es un entero válido >= 0.
     */
    fun parsearEntero(texto: String?): Int? {
        if (texto.isNullOrBlank()) return null
        return texto.trim().toIntOrNull()?.takeIf { it >= 0 }
    }
}


package com.app_moviles.tejisurapp.calidad.ui

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.databinding.DialogDetalleDefectoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class DetalleDefectoDialog : DialogFragment() {

    private val viewModel: CalidadViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val id = requireArguments().getString(ARG_ID)
        val defecto = viewModel.defectos.value.orEmpty().firstOrNull { it.id == id }
        val dialogo = AlertDialog.Builder(requireContext())
            .setTitle(R.string.defectos_detalle_titulo)
            .setPositiveButton(android.R.string.ok, null)

        if (defecto == null) {
            return dialogo.setMessage(R.string.defectos_detalle_no_disponible).create()
        }

        val inspeccion = viewModel.inspecciones.value.orEmpty()
            .firstOrNull { it.id == defecto.inspeccionId }
        val fecha = defecto.fechaHora.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        val mensaje = buildList {
            add(getString(R.string.defectos_causa_formato, getString(defecto.causa.etiquetaRes)))
            add(getString(R.string.defectos_disposicion_formato, getString(defecto.disposicion.etiquetaRes)))
            add(getString(R.string.defectos_estado_formato, getString(defecto.estado.etiquetaRes)))
            add(getString(R.string.defectos_prendas_formato, defecto.prendasAfectadas))
            add(getString(R.string.defectos_lote_opcion, defecto.lote, defecto.ordenTrabajo))
            add(getString(R.string.defectos_inspeccion_formato, defecto.inspeccionId))
            inspeccion?.let { add(getString(R.string.defectos_operario_formato, it.operario)) }
            add(getString(R.string.defectos_fecha_formato, fecha))
        }.joinToString("\n")
        val contenido = DialogDetalleDefectoBinding.inflate(layoutInflater)
        contenido.tvDetalle.text = mensaje
        if (defecto.fotoUri != null) {
            contenido.tvEstadoFoto.setText(R.string.foto_defecto_cargando)
            val contexto = requireContext().applicationContext
            val uri = Uri.parse(defecto.fotoUri)
            lifecycleScope.launch {
                val bitmap = withContext(Dispatchers.IO) { decodificarFoto(contexto, uri, 1280) }
                contenido.imgEvidencia.isVisible = bitmap != null
                contenido.imgEvidencia.setImageBitmap(bitmap)
                contenido.tvEstadoFoto.isVisible = bitmap == null
                contenido.tvEstadoFoto.setText(R.string.foto_defecto_no_disponible)
            }
        }
        return dialogo.setTitle(defecto.tipo.etiquetaRes).setView(contenido.root).create()
    }

    companion object {
        const val TAG = "detalle_defecto"
        private const val ARG_ID = "defecto_id"

        fun crear(id: String) = DetalleDefectoDialog().apply {
            arguments = bundleOf(ARG_ID to id)
        }
    }
}

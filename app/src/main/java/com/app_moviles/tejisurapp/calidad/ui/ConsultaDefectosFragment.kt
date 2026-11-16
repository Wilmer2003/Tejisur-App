package com.app_moviles.tejisurapp.calidad.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.calidad.model.CatalogoCalidad
import com.app_moviles.tejisurapp.calidad.model.EstadoIncidencia
import com.app_moviles.tejisurapp.databinding.FragmentConsultaDefectosBinding
import com.app_moviles.tejisurapp.produccion.ui.adaptadorConHint
import com.app_moviles.tejisurapp.produccion.ui.alSeleccionar

class ConsultaDefectosFragment : Fragment(R.layout.fragment_consulta_defectos) {

    private val viewModel: CalidadViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentConsultaDefectosBinding.bind(view)
        val lotes = CatalogoCalidad.lotesPorOt.flatMap { (orden, lotes) ->
            lotes.map { lote -> orden to lote }
        }
        val estados = EstadoIncidencia.entries
        val adaptador = DefectosAdapter { defecto ->
            if (childFragmentManager.findFragmentByTag(DetalleDefectoDialog.TAG) == null) {
                DetalleDefectoDialog.crear(defecto.id)
                    .showNow(childFragmentManager, DetalleDefectoDialog.TAG)
            }
        }

        with(binding) {
            spinnerLote.adapter = requireContext().adaptadorConHint(
                getString(R.string.spinner_hint_lote),
                lotes.map { (orden, lote) -> getString(R.string.defectos_lote_opcion, lote, orden) }
            )
            spinnerEstado.adapter = requireContext().adaptadorConHint(
                getString(R.string.defectos_todos_estados),
                estados.map { getString(it.etiquetaRes) }
            )
            listaDefectos.layoutManager = LinearLayoutManager(requireContext())
            listaDefectos.adapter = adaptador

            fun actualizarConsulta() {
                val seleccion = lotes.getOrNull(spinnerLote.selectedItemPosition - 1)
                val estado = estados.getOrNull(spinnerEstado.selectedItemPosition - 1)
                val defectosDelLote = seleccion?.let { (orden, lote) ->
                    viewModel.defectosDeLote(orden, lote)
                }.orEmpty()
                val resultados = defectosDelLote
                    .filter { estado == null || it.estado == estado }
                    .sortedByDescending { it.fechaHora }

                adaptador.submitList(resultados)
                listaDefectos.isVisible = resultados.isNotEmpty()
                tvCantidadDefectos.isVisible = seleccion != null
                tvCantidadDefectos.text = resources.getQuantityString(
                    R.plurals.defectos_resultados, resultados.size, resultados.size
                )
                tvConsultaVacia.isVisible = resultados.isEmpty()
                tvConsultaVacia.setText(
                    when {
                        seleccion == null -> R.string.defectos_seleccionar_lote
                        defectosDelLote.isEmpty() -> R.string.defectos_lote_sin_registros
                        else -> R.string.defectos_sin_resultados_filtro
                    }
                )
            }

            spinnerLote.alSeleccionar { actualizarConsulta() }
            spinnerEstado.alSeleccionar { actualizarConsulta() }
            viewModel.defectos.observe(viewLifecycleOwner) { actualizarConsulta() }
        }
    }
}

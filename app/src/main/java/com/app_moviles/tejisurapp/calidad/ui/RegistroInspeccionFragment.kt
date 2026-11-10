package com.app_moviles.tejisurapp.calidad.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app_moviles.tejisurapp.MainActivity
import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.calidad.model.AnalisisInspeccion
import com.app_moviles.tejisurapp.calidad.model.InspeccionCalidad
import com.app_moviles.tejisurapp.databinding.FragmentRegistroInspeccionBinding
import com.app_moviles.tejisurapp.produccion.ui.adaptadorConHint
import com.app_moviles.tejisurapp.produccion.ui.alSeleccionar
import com.app_moviles.tejisurapp.produccion.ui.mostrarError
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * HU04-T04 - Pantalla de Registro y Validación de Inspección de Calidad.
 */
class RegistroInspeccionFragment : Fragment() {

    private var _binding: FragmentRegistroInspeccionBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: CalidadViewModel by activityViewModels()

    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroInspeccionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarDatosAutomaticos()
        configurarAdaptadoresFijos()
        renderizarFormulario()
        configurarListeners()
        observarEstadoViewModel()
    }

    private fun configurarDatosAutomaticos() = with(binding) {
        tvSupervisor.text = getString(R.string.inspeccion_supervisor_formato, CalidadViewModel.SUPERVISOR_SESION)
        tvFechaHora.text = getString(R.string.inspeccion_fecha_hora_formato, LocalDateTime.now().format(formatoFecha))
    }

    private fun configurarAdaptadoresFijos() = with(binding) {
        spinnerOt.adapter = requireContext().adaptadorConHint(
            getString(R.string.spinner_hint_ot),
            viewModel.ordenesTrabajo
        )
        spinnerOperario.adapter = requireContext().adaptadorConHint(
            getString(R.string.spinner_hint_operario),
            viewModel.operarios
        )
    }

    private fun configurarListeners() = with(binding) {
        spinnerOt.alSeleccionar { posicion ->
            val otIndex = posicion - 1
            if (otIndex != viewModel.inspeccionForm.otIndex) {
                viewModel.onInspeccionOtSeleccionada(otIndex)
                actualizarSpinnerLote()
            }
        }

        spinnerLote.alSeleccionar { posicion ->
            viewModel.onInspeccionLoteSeleccionado(posicion - 1)
        }

        spinnerOperario.alSeleccionar { posicion ->
            viewModel.onInspeccionOperarioSeleccionado(posicion - 1)
        }

        etInspeccionadas.doAfterTextChanged { texto ->
            viewModel.onInspeccionInspeccionadasCambiada(texto?.toString().orEmpty())
        }

        etAprobadas.doAfterTextChanged { texto ->
            viewModel.onInspeccionAprobadasCambiada(texto?.toString().orEmpty())
        }

        etRechazadas.doAfterTextChanged { texto ->
            viewModel.onInspeccionRechazadasCambiada(texto?.toString().orEmpty())
        }

        etReproceso.doAfterTextChanged { texto ->
            viewModel.onInspeccionReprocesoCambiada(texto?.toString().orEmpty())
        }

        btnRegistrarInspeccion.setOnClickListener {
            val inspeccion = viewModel.registrarInspeccion()
            if (inspeccion != null) {
                mostrarExito(inspeccion)
                renderizarFormulario()
            }
        }
    }

    private fun observarEstadoViewModel() = with(binding) {
        viewModel.indicadorSuma.observe(viewLifecycleOwner) { balance ->
            tvBalanceSuma.text = balance
        }

        viewModel.resultadoEnVivo.observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                AnalisisInspeccion.RESULTADO_SIN_DEFECTOS -> {
                    tvResultadoInsignia.setText(R.string.resultado_sin_rechazo_ni_reproceso)
                    tvResultadoInsignia.setTextColor(ContextCompat.getColor(requireContext(), R.color.merma_normal))
                }
                AnalisisInspeccion.RESULTADO_CON_DEFECTOS -> {
                    tvResultadoInsignia.setText(R.string.resultado_con_rechazo_o_reproceso)
                    tvResultadoInsignia.setTextColor(ContextCompat.getColor(requireContext(), R.color.tejisur_error))
                }
                else -> {
                    tvResultadoInsignia.setText(R.string.resultado_sin_definir)
                    tvResultadoInsignia.setTextColor(ContextCompat.getColor(requireContext(), R.color.tejisur_text_secondary))
                }
            }
        }

        viewModel.erroresInspeccion.observe(viewLifecycleOwner) { errores ->
            tvErrorOt.mostrarError(errores.ot)
            tvErrorLote.mostrarError(errores.lote)
            tvErrorOperario.mostrarError(errores.operario)
            tvErrorInspeccionadas.mostrarError(errores.inspeccionadas)
            tvErrorAprobadas.mostrarError(errores.aprobadas)
            tvErrorRechazadas.mostrarError(errores.rechazadas)
            tvErrorReproceso.mostrarError(errores.enReproceso)
            tvErrorSuma.mostrarError(errores.suma)

            tvErrorFormulario.mostrarError(
                R.string.error_formulario_incompleto.takeIf { errores.hayErrores }
            )
        }

        viewModel.solicitudRegistroCausa.observe(viewLifecycleOwner) { evento ->
            evento.obtenerSiNoManejado()?.let { inspeccionConDefectos ->
                mostrarDialogoDefecto(inspeccionConDefectos)
            }
        }
    }

    private fun renderizarFormulario() = with(binding) {
        val form = viewModel.inspeccionForm
        spinnerOt.setSelection(form.otIndex + 1)
        actualizarSpinnerLote()
        spinnerLote.setSelection(form.loteIndex + 1)
        spinnerOperario.setSelection(form.operarioIndex + 1)

        if (etInspeccionadas.text.toString() != form.inspeccionadas) {
            etInspeccionadas.setText(form.inspeccionadas)
        }
        if (etAprobadas.text.toString() != form.aprobadas) {
            etAprobadas.setText(form.aprobadas)
        }
        if (etRechazadas.text.toString() != form.rechazadas) {
            etRechazadas.setText(form.rechazadas)
        }
        if (etReproceso.text.toString() != form.enReproceso) {
            etReproceso.setText(form.enReproceso)
        }
    }

    private fun actualizarSpinnerLote() = with(binding) {
        val form = viewModel.inspeccionForm
        val lotes = viewModel.lotesDe(form.otIndex)
        spinnerLote.adapter = requireContext().adaptadorConHint(
            getString(R.string.spinner_hint_lote),
            lotes
        )
        val seleccion = if (form.loteIndex in lotes.indices) form.loteIndex + 1 else 0
        spinnerLote.setSelection(seleccion)
    }

    private fun mostrarExito(inspeccion: InspeccionCalidad) {
        Toast.makeText(
            requireContext(),
            getString(
                R.string.inspeccion_registrada_exito,
                inspeccion.inspeccionadas,
                inspeccion.aprobadas,
                inspeccion.ordenTrabajo,
                inspeccion.lote
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun mostrarDialogoDefecto(inspeccion: InspeccionCalidad) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialogo_defecto_titulo)
            .setMessage(
                getString(
                    R.string.dialogo_defecto_mensaje,
                    inspeccion.rechazadas,
                    inspeccion.enReproceso
                )
            )
            .setPositiveButton(R.string.dialogo_defecto_btn_registrar_causa) { _, _ ->
                (activity as? MainActivity)?.mostrarSeccion(MainActivity.Seccion.DEFECTOS)
            }
            .setNegativeButton(R.string.alerta_btn_entendido, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.app_moviles.tejisurapp.calidad.ui

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.calidad.model.AnalisisInspeccion
import com.app_moviles.tejisurapp.calidad.model.InspeccionCalidad
import com.app_moviles.tejisurapp.databinding.FragmentRegistroInspeccionBinding
import com.app_moviles.tejisurapp.databinding.ItemCausaInspeccionBinding
import com.app_moviles.tejisurapp.produccion.ui.adaptadorConHint
import com.app_moviles.tejisurapp.produccion.ui.alSeleccionar
import com.app_moviles.tejisurapp.produccion.ui.mostrarError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
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

    private val tomarFoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        if (tomada) viewModel.recibirFoto(requireContext().applicationContext)
        else viewModel.cancelarSolicitudFoto()
    }

    private val elegirFoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.recibirFoto(requireContext().applicationContext, uri)
        else viewModel.cancelarSolicitudFoto()
    }

    private fun solicitarFoto(idGrupo: Long, usarCamara: Boolean) {
        if (viewModel.fotoOcupada.value == true) return
        try {
            val foto = FotoDefecto.crear(requireContext())
            viewModel.iniciarSolicitudFoto(idGrupo, foto)
            if (usarCamara) tomarFoto.launch(foto.uri)
            else elegirFoto.launch("image/*")
        } catch (_: ActivityNotFoundException) {
            viewModel.cancelarSolicitudFoto()
            mostrarErrorFoto()
        } catch (_: IOException) {
            mostrarErrorFoto()
        } catch (_: SecurityException) {
            viewModel.cancelarSolicitudFoto()
            mostrarErrorFoto()
        }
    }

    private fun mostrarErrorFoto() {
        Toast.makeText(requireContext(), R.string.foto_defecto_error, Toast.LENGTH_LONG).show()
    }

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
        renderizarCausas()
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

        btnAgregarCausa.setOnClickListener {
            viewModel.agregarCausa()
            renderizarCausas()
        }

        btnAgregarAprobadas.setOnClickListener {
            viewModel.agregarCausa(esGrupoAprobado = true)
            renderizarCausas()
        }

        btnRegistrarInspeccion.setOnClickListener {
            val inspeccion = viewModel.registrarInspeccion()
            if (inspeccion != null) {
                mostrarExito(inspeccion)
                renderizarFormulario()
                renderizarCausas()
            }
        }
    }

    private fun observarEstadoViewModel() = with(binding) {
        viewModel.indicadorSuma.observe(viewLifecycleOwner) { balance ->
            tvBalanceSuma.text = balance
            val form = viewModel.inspeccionForm
            cardCausas.isVisible = (form.rechazadas.toIntOrNull() ?: 0) > 0 ||
                (form.enReproceso.toIntOrNull() ?: 0) > 0
            btnAgregarAprobadas.isVisible = (form.aprobadas.toIntOrNull() ?: 0) > 0
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

        viewModel.fotoOcupada.observe(viewLifecycleOwner) { ocupada ->
            btnRegistrarInspeccion.isEnabled = !ocupada
        }

        viewModel.resultadoFoto.observe(viewLifecycleOwner) { evento ->
            when (evento.obtenerSiNoManejado()) {
                true -> renderizarCausas()
                false -> mostrarErrorFoto()
                null -> Unit
            }
        }

        viewModel.errorCausas.observe(viewLifecycleOwner) { error ->
            tvErrorCausas.mostrarError(error)
        }
    }

    private fun renderizarCausas(): Unit = with(binding) {
        listaCausas.removeAllViews()
        viewModel.causasInspeccion.forEachIndexed { indice, causa ->
            val fila = ItemCausaInspeccionBinding.inflate(layoutInflater, listaCausas, false)
            fila.root.isSaveFromParentEnabled = false
            with(fila) {
                val numeroGrupo: Int = indice + 1
                tvGrupo.text = getString(
                    R.string.inspeccion_grupo_formato,
                    numeroGrupo,
                    getString(if (causa.esGrupoAprobado) R.string.inspeccion_grupo_aprobadas
                    else R.string.inspeccion_grupo_defecto)
                )
                spinnerTipo.isVisible = !causa.esGrupoAprobado
                spinnerCausa.isVisible = !causa.esGrupoAprobado
                spinnerDisposicion.isVisible = !causa.esGrupoAprobado
                if (causa.esGrupoAprobado) etCantidad.setHint(R.string.inspeccion_grupo_aprobadas)
                val tipos = viewModel.tiposDefecto
                val causas = viewModel.causasDefecto
                val disposiciones = viewModel.disposiciones
                spinnerTipo.adapter = requireContext().adaptadorConHint(
                    getString(R.string.inspeccion_tipo_defecto), tipos.map { getString(it.etiquetaRes) }
                )
                spinnerCausa.adapter = requireContext().adaptadorConHint(
                    getString(R.string.inspeccion_causa_defecto), causas.map { getString(it.etiquetaRes) }
                )
                spinnerDisposicion.adapter = requireContext().adaptadorConHint(
                    getString(R.string.inspeccion_disposicion_defecto), disposiciones.map { getString(it.etiquetaRes) }
                )
                spinnerTipo.setSelection(tipos.indexOfFirst { it == causa.tipo } + 1)
                spinnerCausa.setSelection(causas.indexOfFirst { it == causa.causa } + 1)
                spinnerDisposicion.setSelection(disposiciones.indexOfFirst { it == causa.disposicion } + 1)
                etCantidad.setText(causa.cantidad)
                panelFoto.isVisible = !causa.esGrupoAprobado
                btnQuitarFoto.isVisible = causa.foto != null
                btnTomarFoto.setOnClickListener { solicitarFoto(causa.idGrupo, usarCamara = true) }
                btnElegirFoto.setOnClickListener { solicitarFoto(causa.idGrupo, usarCamara = false) }
                btnQuitarFoto.setOnClickListener {
                    viewModel.quitarFoto(indice)
                    renderizarCausas()
                }
                causa.foto?.let { foto ->
                    btnTomarFoto.setText(R.string.foto_defecto_retomar)
                    btnElegirFoto.setText(R.string.foto_defecto_elegir_otra)
                    val contexto = requireContext().applicationContext
                    viewLifecycleOwner.lifecycleScope.launch {
                        val bitmap = withContext(Dispatchers.IO) { decodificarFoto(contexto, foto.uri, 480) }
                        if (root.parent == listaCausas) {
                            imgFoto.isVisible = bitmap != null
                            imgFoto.setImageBitmap(bitmap)
                        }
                    }
                }

                fun actualizarFila() {
                    if (root.parent != listaCausas) return
                    viewModel.actualizarCausa(
                        indice,
                        viewModel.causasInspeccion[indice].copy(
                            tipo = tipos.getOrNull(spinnerTipo.selectedItemPosition - 1),
                            causa = causas.getOrNull(spinnerCausa.selectedItemPosition - 1),
                            disposicion = disposiciones.getOrNull(spinnerDisposicion.selectedItemPosition - 1),
                            cantidad = etCantidad.text.toString()
                        )
                    )
                }

                spinnerTipo.alSeleccionar { actualizarFila() }
                spinnerCausa.alSeleccionar { actualizarFila() }
                spinnerDisposicion.alSeleccionar { actualizarFila() }
                etCantidad.doAfterTextChanged { actualizarFila() }
                btnQuitarCausa.setOnClickListener {
                    viewModel.quitarCausa(indice)
                    renderizarCausas()
                }
            }
            listaCausas.addView(fila.root)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

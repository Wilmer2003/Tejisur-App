package com.app_moviles.tejisurapp.calidad.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app_moviles.tejisurapp.databinding.FragmentConsultaDefectosBinding

/**
 * EP02 - Fragment provisional para Consulta de Defectos.
 */
class ConsultaDefectosFragment : Fragment() {

    private var _binding: FragmentConsultaDefectosBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val viewModel: CalidadViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultaDefectosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


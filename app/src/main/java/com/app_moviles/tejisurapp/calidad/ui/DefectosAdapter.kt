package com.app_moviles.tejisurapp.calidad.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.calidad.model.Defecto
import com.app_moviles.tejisurapp.calidad.model.EstadoIncidencia
import com.app_moviles.tejisurapp.databinding.ItemDefectoBinding

internal class DefectosAdapter(
    private val abrirDetalle: (Defecto) -> Unit
) : ListAdapter<Defecto, DefectosAdapter.DefectoViewHolder>(Diferencias) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefectoViewHolder =
        DefectoViewHolder(
            ItemDefectoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: DefectoViewHolder, position: Int) {
        holder.mostrar(getItem(position))
    }

    inner class DefectoViewHolder(
        private val binding: ItemDefectoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun mostrar(defecto: Defecto) = with(binding) {
            val contexto = root.context
            tvTipo.setText(defecto.tipo.etiquetaRes)
            tvCausa.text = contexto.getString(
                R.string.defectos_causa_formato, contexto.getString(defecto.causa.etiquetaRes)
            )
            tvDisposicion.text = contexto.getString(
                R.string.defectos_disposicion_cantidad,
                contexto.getString(defecto.disposicion.etiquetaRes),
                contexto.resources.getQuantityString(
                    R.plurals.defectos_prendas, defecto.prendasAfectadas, defecto.prendasAfectadas
                )
            )
            tvEstado.setText(defecto.estado.etiquetaRes)
            val pendiente = defecto.estado == EstadoIncidencia.PENDIENTE
            tvEstado.setTextColor(
                ContextCompat.getColor(contexto, if (pendiente) R.color.tejisur_error else R.color.merma_normal)
            )
            root.setOnClickListener { abrirDetalle(defecto) }
        }
    }

    private object Diferencias : DiffUtil.ItemCallback<Defecto>() {
        override fun areItemsTheSame(oldItem: Defecto, newItem: Defecto): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Defecto, newItem: Defecto): Boolean =
            oldItem == newItem
    }
}

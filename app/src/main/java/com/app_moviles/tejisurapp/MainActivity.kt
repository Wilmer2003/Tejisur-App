package com.app_moviles.tejisurapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.app_moviles.tejisurapp.databinding.ActivityMainBinding
import com.app_moviles.tejisurapp.produccion.model.AlertaMerma
import com.app_moviles.tejisurapp.produccion.ui.ConsultaMermasFragment
import com.app_moviles.tejisurapp.produccion.ui.LoginFragment
import com.app_moviles.tejisurapp.produccion.ui.ProduccionViewModel
import com.app_moviles.tejisurapp.produccion.ui.RegistroConsumo
import com.app_moviles.tejisurapp.produccion.ui.RegistroMermaFragment

class MainActivity : AppCompatActivity() {

    // PASO 01: Declaro mi variable con el tipo de control
    enum class Seccion { LOGIN, CONSUMO, MERMA, CONSULTA }

    private lateinit var binding: ActivityMainBinding

    private val viewModel: ProduccionViewModel by viewModels()

    private var seccionActual = Seccion.LOGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarMargenesDeSistema()

        seccionActual = savedInstanceState?.getString(KEY_SECCION)
            ?.let { Seccion.valueOf(it) }
            ?: Seccion.LOGIN

        if (savedInstanceState == null) {
            mostrarSeccion(seccionActual, forzar = true)
        } else {
            actualizarVisibilidadNavegacion()
            marcarNavegacion() // El FragmentManager ya restauró el fragment visible
        }

        binding.navConsumo.setOnClickListener { mostrarSeccion(Seccion.CONSUMO) }
        binding.navMerma.setOnClickListener { mostrarSeccion(Seccion.MERMA) }
        binding.navConsulta.setOnClickListener { mostrarSeccion(Seccion.CONSULTA) }

        // HU16 - Alerta por merma excesiva
        viewModel.alertaMerma.observe(this) { evento ->
            evento.obtenerSiNoManejado()?.let(::mostrarAlertaMerma)
        }
    }

    fun mostrarSeccion(seccion: Seccion, forzar: Boolean = false) {
        if (seccion == seccionActual && !forzar) return
        seccionActual = seccion
        actualizarVisibilidadNavegacion()
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.contenedorFragment, crearFragment(seccion))
        }
        marcarNavegacion()
    }

    private fun actualizarVisibilidadNavegacion() {
        val esLogin = seccionActual == Seccion.LOGIN
        binding.llNavegacion.visibility = if (esLogin) View.GONE else View.VISIBLE
    }

    private fun crearFragment(seccion: Seccion): Fragment = when (seccion) {
        Seccion.LOGIN -> LoginFragment()
        Seccion.CONSUMO -> RegistroConsumo()
        Seccion.MERMA -> RegistroMermaFragment()
        Seccion.CONSULTA -> ConsultaMermasFragment()
    }

    /** Resalta la pestaña activa con el color primario de TEJISUR. */
    private fun marcarNavegacion() {
        if (seccionActual == Seccion.LOGIN) return
        val pestanas: Map<Seccion, TextView> = mapOf(
            Seccion.CONSUMO to binding.navConsumo,
            Seccion.MERMA to binding.navMerma,
            Seccion.CONSULTA to binding.navConsulta
        )
        pestanas.forEach { (seccion, vista) ->
            val activa = seccion == seccionActual
            val color = ContextCompat.getColor(
                this, if (activa) R.color.tejisur_primary else R.color.tejisur_text_secondary
            )
            vista.isSelected = activa
            vista.setTextColor(color)
            TextViewCompat.setCompoundDrawableTintList(vista, ColorStateList.valueOf(color))
        }
    }

    /** HU16 - Muestra lote, OT, % de merma y umbral superado. */
    private fun mostrarAlertaMerma(alerta: AlertaMerma) {
        AlertDialog.Builder(this)
            .setTitle(R.string.alerta_titulo)
            .setMessage(
                getString(
                    R.string.alerta_mensaje,
                    alerta.lote,
                    alerta.ordenTrabajo,
                    alerta.porcentajeMerma,
                    alerta.umbral
                )
            )
            .setPositiveButton(R.string.alerta_btn_ver_consulta) { _, _ ->
                mostrarSeccion(Seccion.CONSULTA)
            }
            .setNegativeButton(R.string.alerta_btn_entendido, null)
            .show()
    }

    /** targetSdk 35 dibuja de borde a borde: evita que la barra quede bajo la del sistema. */
    private fun aplicarMargenesDeSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vista, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SECCION, seccionActual.name)
    }

    private companion object {
        const val KEY_SECCION = "seccion_actual"
    }
}
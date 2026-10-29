package com.app_moviles.tejisurapp.produccion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

// IMPORTS OFICIALES DE FIREBASE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import com.app_moviles.tejisurapp.MainActivity
import com.app_moviles.tejisurapp.R
import com.app_moviles.tejisurapp.databinding.LoginTejisurBinding

class LoginFragment : Fragment() {

    private var _binding: LoginTejisurBinding? = null
    private val binding get() = checkNotNull(_binding)

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginTejisurBinding.inflate(inflater, container, false)

        // Inicializar servicios de Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarListeners()
    }

    private fun configurarListeners() = with(binding) {
        btnIniciarSesion.setOnClickListener { intentarLogin() }

        etContrasena.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                intentarLogin()
                true
            } else false
        }

        etUsuario.doAfterTextChanged { etUsuario.error = null }
        etContrasena.doAfterTextChanged { etContrasena.error = null }
    }

    private fun intentarLogin() {
        val usuario = binding.etUsuario.text.toString().trim()
        val contrasena = binding.etContrasena.text.toString().trim()

        if (!camposValidos(usuario, contrasena)) return

        // 1. Autenticación real con Firebase Authentication
        auth.signInWithEmailAndPassword(usuario, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    // 2. Consultar rol sin usar clases personalizadas
                    consultarRolYRedirigir(uid)
                } else {
                    binding.etContrasena.text?.clear()
                    Toast.makeText(
                        requireContext(),
                        "Acceso denegado: Usuario o contraseña incorrectos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun consultarRolYRedirigir(uid: String) {
        val userRef = database.getReference("usuarios").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Lectura directa de campos sin crear clases nuevas
                val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                val rol = snapshot.child("rol").getValue(String::class.java) ?: "OPERARIO"

                Toast.makeText(
                    requireContext(),
                    "¡Bienvenido $nombre ($rol)!",
                    Toast.LENGTH_SHORT
                ).show()

                // Redirección por roles
                when (rol) {
                    "ADMIN", "SUPERVISOR" -> {
                        (activity as? MainActivity)?.mostrarSeccion(MainActivity.Seccion.CONSULTA)
                    }
                    else -> { // OPERARIO
                        (activity as? MainActivity)?.mostrarSeccion(MainActivity.Seccion.CONSUMO)
                            ?: run {
                                parentFragmentManager.commit {
                                    replace(android.R.id.content, RegistroConsumo())
                                }
                            }
                    }
                }
            } else {
                (activity as? MainActivity)?.mostrarSeccion(MainActivity.Seccion.CONSUMO)
            }
        }.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                "Error al conectar con la base de datos de usuarios",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun camposValidos(usuario: String, contrasena: String): Boolean {
        with(binding) {
            etUsuario.error = if (usuario.isEmpty()) getString(R.string.login_error_usuario_requerido) else null
            etContrasena.error = if (contrasena.isEmpty()) getString(R.string.login_error_contrasena_requerida) else null

            when {
                usuario.isEmpty() -> etUsuario.requestFocus()
                contrasena.isEmpty() -> etContrasena.requestFocus()
            }
        }
        return usuario.isNotEmpty() && contrasena.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
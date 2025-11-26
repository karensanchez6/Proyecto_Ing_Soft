package com.example.pfinal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class RegisterFragment : Fragment() {

    interface RegisterListener {
        fun onRegisterSuccess()
    }

    private var listener: RegisterListener? = null
    private lateinit var dbHelper: DBHelper

    private lateinit var spinnerFacultad: Spinner
    private lateinit var spinnerProvincia: Spinner
    private var listaFacultades = listOf<Pair<Int, String>>() // id y nombre
    private var listaProvincias = listOf<Pair<Int, String>>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RegisterListener) {
            listener = context
        }
        dbHelper = DBHelper(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val etCedula = view.findViewById<EditText>(R.id.etCedula)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etCorreo = view.findViewById<EditText>(R.id.etCorreo)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)

        spinnerFacultad = view.findViewById(R.id.spinnerFacultad)
        spinnerProvincia = view.findViewById(R.id.spinnerProvincia)

        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvGoLogin = view.findViewById<TextView>(R.id.tvGoLogin)

        cargarFacultades()
        cargarProvincias()

        btnRegister.setOnClickListener {
            val cedula = etCedula.text.toString().trim()
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            if (cedula.isEmpty() || nombre.isEmpty() || correo.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val facultadPos = spinnerFacultad.selectedItemPosition
            val provinciaPos = spinnerProvincia.selectedItemPosition

            if (facultadPos < 0 || provinciaPos < 0) {
                Toast.makeText(requireContext(), "Selecciona facultad y provincia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idFacultad = listaFacultades[facultadPos].first
            val idProvincia = listaProvincias[provinciaPos].first

            val resultadoRegistro = dbHelper.registrarUsuarioConPadron(
                cedula,
                nombre,
                correo,
                username,
                password,
                idFacultad,
                idProvincia
            )

            if (resultadoRegistro != null) {
                val pinGenerado = resultadoRegistro.pin
                val idUsuario = resultadoRegistro.idUsuario

                // Guardar idUsuario en SharedPreferences
                val prefs = requireContext().getSharedPreferences("mis_prefs", Context.MODE_PRIVATE)
                prefs.edit().putInt("id_usuario", idUsuario.toInt()).apply()

                Toast.makeText(requireContext(), "Registro exitoso. Tu PIN es: $pinGenerado", Toast.LENGTH_LONG).show()
                listener?.onRegisterSuccess()
            } else {
                Toast.makeText(requireContext(), "Error al registrar usuario", Toast.LENGTH_SHORT).show()
            }


        }

        tvGoLogin.setOnClickListener {
            (activity as? AuthActivity)?.showLogin()
        }

        return view
    }

    private fun cargarFacultades() {
        listaFacultades = dbHelper.obtenerFacultades()
        val nombres = listaFacultades.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFacultad.adapter = adapter
    }

    private fun cargarProvincias() {
        listaProvincias = dbHelper.obtenerProvincias()
        val nombres = listaProvincias.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvincia.adapter = adapter
    }
}

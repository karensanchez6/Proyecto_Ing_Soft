package com.example.pfinal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class LoginFragment : Fragment() {

    interface LoginListener {
        fun onLoginSuccess(rol: String)  // Ahora recibe el rol
    }

    private var listener: LoginListener? = null
    private lateinit var dbHelper: DBHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginListener) {
            listener = context
        }
        dbHelper = DBHelper(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val etUserOrEmail = view.findViewById<EditText>(R.id.etUserOrEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvGoRegister = view.findViewById<TextView>(R.id.tvGoRegister)

        btnLogin.setOnClickListener {
            val userOrEmail = etUserOrEmail.text.toString().trim()
            val password = etPassword.text.toString()

            val rol = dbHelper.verificarLogin(userOrEmail, password)
            if (rol != null) {
                // Obtener id_usuario
                val idUsuario = dbHelper.obtenerIdUsuarioPorUsernameOCorreo(userOrEmail)

                if (idUsuario != null) {
                    val prefs = requireContext().getSharedPreferences("mis_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("id_usuario", idUsuario).apply()
                }

                Toast.makeText(requireContext(), "Bienvenido $rol", Toast.LENGTH_SHORT).show()
                listener?.onLoginSuccess(rol)
            } else {
                Toast.makeText(requireContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }


        tvGoRegister.setOnClickListener {
            (activity as? AuthActivity)?.showRegister()
        }

        return view
    }
}

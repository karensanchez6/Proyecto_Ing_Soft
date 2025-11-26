package com.example.pfinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class VerificacionFragment : Fragment() {

    private lateinit var etCedula: EditText
    private lateinit var etPin: EditText
    private lateinit var btnVerificar: Button
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_verificacion, container, false)

        etCedula = view.findViewById(R.id.etCedula)
        etPin = view.findViewById(R.id.etPin)
        btnVerificar = view.findViewById(R.id.btnVerificar)
        dbHelper = DBHelper(requireContext())

        btnVerificar.setOnClickListener {
            val cedula = etCedula.text.toString().trim()
            val pin = etPin.text.toString().trim()

            if (cedula.isEmpty() || pin.isEmpty()) {
                Toast.makeText(context, "Debe ingresar cédula y PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.verificarPin(cedula, pin)) {
                val idPadron = dbHelper.obtenerIdPadronPorCedula(cedula)
                if (idPadron == null) {
                    Toast.makeText(context, "Cédula no encontrada", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!dbHelper.yaVoto(idPadron)) {
                    // Guardamos la verificación en SharedPreferences
                    val prefs = requireContext().getSharedPreferences("prefs_voto", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("verificado", true)
                        .putString("cedula", cedula)
                        .apply()

                    // Navegar a la papeleta
                    val fragment = PapeletaFragment().apply {
                        arguments = Bundle().apply { putString("cedula", cedula) }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit()

                    val bottomNav = requireActivity()
                        .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                    bottomNav.selectedItemId = R.id.nav_votar
                } else {
                    Toast.makeText(context, "Ya ha votado anteriormente", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Cédula o PIN incorrectos", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }
}

package com.example.pfinal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class InicioFragment : Fragment() {

    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())

        val tvPin = view.findViewById<TextView>(R.id.idPinInfo)
        val tvCandidatos = view.findViewById<TextView>(R.id.tvListaCandidatos)

        // Obtener id del usuario actual
        val prefs = requireContext().getSharedPreferences("mis_prefs", Context.MODE_PRIVATE)
        val idUsuario = prefs.getInt("id_usuario", -1).takeIf { it != -1 }

        if (idUsuario != null) {
            val pin = dbHelper.obtenerPinPorUsuarioId(idUsuario)
            tvPin.text = pin?.let { "Tu PIN es: $it" } ?: "PIN no disponible"
        } else {
            tvPin.text = "Usuario no identificado"
        }


        // Mostrar la lista de candidatos
        val candidatos = dbHelper.obtenerCandidatos()
        val listaFormateada = candidatos.joinToString("\n") {
            "• ${it.nombre}" + (if (it.idFacultad != null) " (Facultad ID: ${it.idFacultad})" else "")
        }
        tvCandidatos.text = if (listaFormateada.isNotEmpty()) listaFormateada else "No hay candidatos registrados."
    }
}

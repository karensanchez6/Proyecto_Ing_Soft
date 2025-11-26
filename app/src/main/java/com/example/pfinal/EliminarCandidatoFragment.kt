package com.example.pfinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class EliminarCandidatoFragment : Fragment() {

    private lateinit var spinnerEliminarCandidatos: Spinner
    private lateinit var btnEliminarCandidato: Button
    private lateinit var dbHelper: DBHelper
    private var listaCandidatos = listOf<CandidatoData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_eliminar_candidato, container, false)

        dbHelper = DBHelper(requireContext())

        spinnerEliminarCandidatos = view.findViewById(R.id.spinnerEliminarCandidatos)
        btnEliminarCandidato = view.findViewById(R.id.btnEliminarCandidato)

        cargarCandidatos()

        btnEliminarCandidato.setOnClickListener {
            val pos = spinnerEliminarCandidatos.selectedItemPosition
            if (pos < 0) {
                Toast.makeText(requireContext(), "Selecciona un candidato para eliminar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val candidato = listaCandidatos[pos]
            val eliminado = dbHelper.eliminarCandidato(candidato.idCandidato)

            if (eliminado) {
                Toast.makeText(requireContext(), "Candidato eliminado correctamente", Toast.LENGTH_SHORT).show()
                cargarCandidatos()
            } else {
                Toast.makeText(requireContext(), "Error al eliminar candidato", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun cargarCandidatos() {
        listaCandidatos = dbHelper.obtenerCandidatos()
        val nombres = listaCandidatos.map { it.nombre }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEliminarCandidatos.adapter = adapter
    }
}

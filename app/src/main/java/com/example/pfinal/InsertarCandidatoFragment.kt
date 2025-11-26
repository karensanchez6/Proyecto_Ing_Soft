package com.example.pfinal

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

class InsertarCandidatoFragment : Fragment() {

    private lateinit var etNombreCandidato: EditText
    private lateinit var spinnerFacultad: Spinner
    private lateinit var dbHelper: DBHelper
    private lateinit var listaFacultades: List<Pair<Int, String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_insertar_candidato, container, false)

        etNombreCandidato = view.findViewById(R.id.etNombreCandidato)
        spinnerFacultad = view.findViewById(R.id.spinnerFacultad)
        val btnAgregar = view.findViewById<Button>(R.id.btnAgregarCandidato)

        dbHelper = DBHelper(requireContext())
        cargarFacultades()

        btnAgregar.setOnClickListener {
            val nombre = etNombreCandidato.text.toString().trim()
            val facultadIndex = spinnerFacultad.selectedItemPosition

            if (nombre.isEmpty() || facultadIndex == -1) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idFacultad = listaFacultades[facultadIndex].first
            val success = dbHelper.agregarCandidato(nombre, idFacultad)

            if (success) {
                Toast.makeText(requireContext(), "Candidato insertado correctamente", Toast.LENGTH_SHORT).show()
                etNombreCandidato.text.clear()
                spinnerFacultad.setSelection(0)
            } else {
                Toast.makeText(requireContext(), "Error al insertar candidato", Toast.LENGTH_SHORT).show()
            }
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
}

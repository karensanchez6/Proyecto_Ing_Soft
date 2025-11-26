package com.example.pfinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class ActualizarCandidatoFragment : Fragment() {

    private lateinit var spinnerCandidatos: Spinner
    private lateinit var etNuevoNombre: EditText
    private lateinit var spinnerNuevaFacultad: Spinner
    private lateinit var btnActualizar: Button

    private lateinit var dbHelper: DBHelper
    private var listaCandidatos = listOf<CandidatoData>()
    private var listaFacultades = listOf<Pair<Int, String>>() // id y nombre

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_actualizar_candidato, container, false)

        dbHelper = DBHelper(requireContext())

        spinnerCandidatos = view.findViewById(R.id.spinnerCandidatos)
        etNuevoNombre = view.findViewById(R.id.etNuevoNombre)
        spinnerNuevaFacultad = view.findViewById(R.id.spinnerNuevaFacultad)
        btnActualizar = view.findViewById(R.id.btnActualizarCandidato)

        cargarFacultades()
        cargarCandidatos()

        btnActualizar.setOnClickListener {
            val posCandidato = spinnerCandidatos.selectedItemPosition
            val posFacultad = spinnerNuevaFacultad.selectedItemPosition

            if (posCandidato < 0 || posFacultad < 0) {
                Toast.makeText(requireContext(), "Selecciona candidato y facultad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val candidato = listaCandidatos[posCandidato]
            val nuevoNombre = etNuevoNombre.text.toString().trim()
            val idFacultad = listaFacultades[posFacultad].first

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val actualizado = dbHelper.actualizarCandidato(candidato.idCandidato, nuevoNombre, idFacultad)
            if (actualizado) {
                Toast.makeText(requireContext(), "Candidato actualizado correctamente", Toast.LENGTH_SHORT).show()
                cargarCandidatos()
                etNuevoNombre.setText("")
            } else {
                Toast.makeText(requireContext(), "Error al actualizar candidato", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun cargarCandidatos() {
        listaCandidatos = dbHelper.obtenerCandidatos()
        val nombres = listaCandidatos.map { it.nombre }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCandidatos.adapter = adapter
    }

    private fun cargarFacultades() {
        listaFacultades = dbHelper.obtenerFacultades()
        val nombres = listaFacultades.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNuevaFacultad.adapter = adapter
    }
}

package com.example.pfinal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class PapeletaFragment : Fragment() {

    private lateinit var rgCandidatos: RadioGroup
    private lateinit var btnConfirmarVoto: Button
    private lateinit var dbHelper: DBHelper
    private var cedula: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_papeleta, container, false)

        rgCandidatos = view.findViewById(R.id.rgCandidatos)
        btnConfirmarVoto = view.findViewById(R.id.btnConfirmarVoto)
        dbHelper = DBHelper(requireContext())
        cedula = arguments?.getString("cedula")

        cargarCandidatos()

        btnConfirmarVoto.setOnClickListener {
            val selectedId = rgCandidatos.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Debe seleccionar un candidato", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cedulaUsuario = cedula ?: run {
                Toast.makeText(requireContext(), "Cédula no encontrada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idPadron = dbHelper.obtenerIdPadronPorCedula(cedulaUsuario)
            val idCandidato = selectedId

            if (idPadron == null) {
                Toast.makeText(requireContext(), "Datos inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaHora = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())
            val exito = dbHelper.registrarVoto(idPadron, idCandidato, fechaHora)

            if (exito) {
                // Invalidamos la verificación
                val prefs = requireContext().getSharedPreferences("prefs_voto", android.content.Context.MODE_PRIVATE)
                prefs.edit().putBoolean("verificado", false).apply()

                // Mostrar el diálogo de confirmación (20 segundos)
                ConfirmacionDialogFragment().show(parentFragmentManager, "ConfirmacionDialog")
            } else {
                Toast.makeText(requireContext(), "Error al registrar el voto", Toast.LENGTH_LONG).show()
            }
        }

        return view
    }

    private fun cargarCandidatos() {
        rgCandidatos.removeAllViews()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id_candidato, nombre FROM candidatos", null)

        if (cursor.moveToFirst()) {
            do {
                val idCandidato = cursor.getInt(cursor.getColumnIndexOrThrow("id_candidato"))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))

                val rb = RadioButton(requireContext())
                rb.id = idCandidato
                rb.text = nombre
                rb.setTextColor(resources.getColor(android.R.color.black))
                rb.textSize = 16f

                rgCandidatos.addView(rb)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }
}

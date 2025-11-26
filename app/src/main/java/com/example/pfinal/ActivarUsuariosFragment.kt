package com.example.pfinal

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment

data class UsuarioData(
    val idUsuario: Int,
    val username: String,
    val activo: Boolean
)

class ActivarUsuariosFragment : Fragment() {

    private lateinit var listViewUsuarios: ListView
    private lateinit var dbHelper: DBHelper
    private lateinit var usuariosList: MutableList<UsuarioData>
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activar_usuarios, container, false)


        listViewUsuarios = view.findViewById(R.id.listViewUsuarios)
        dbHelper = DBHelper(requireContext())

        cargarUsuarios()

        listViewUsuarios.setOnItemClickListener { _, _, position, _ ->
            val usuario = usuariosList[position]
            mostrarDialogoCambioEstado(usuario)
        }

        return view
    }

    private fun cargarUsuarios() {
        usuariosList = dbHelper.obtenerUsuarios()
        val nombres = usuariosList.map {
            "${it.username} - ${if(it.activo) "Activo" else "Inactivo"}"
        }
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, nombres)
        listViewUsuarios.adapter = adapter
    }

    private fun mostrarDialogoCambioEstado(usuario: UsuarioData) {
        val nuevoEstado = !usuario.activo
        val mensaje = if (nuevoEstado) {
            "¿Quieres activar al usuario ${usuario.username}?"
        } else {
            "¿Quieres desactivar al usuario ${usuario.username}?"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar estado")
            .setMessage(mensaje)
            .setPositiveButton("Sí") { _, _ ->
                val exito = dbHelper.actualizarEstadoUsuario(usuario.idUsuario, nuevoEstado)
                if (exito) {
                    Toast.makeText(requireContext(), "Usuario actualizado", Toast.LENGTH_SHORT).show()
                    cargarUsuarios()
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}

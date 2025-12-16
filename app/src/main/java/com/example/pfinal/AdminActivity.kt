package com.example.pfinal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar

//TODO: Complete code
class AdminActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        toolbar = findViewById(R.id.toolbar)
        bottomNav = findViewById(R.id.bottom_nav_admin)

        // Toolbar settings
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationIcon(R.drawable.ic_logout)

        toolbar.setNavigationOnClickListener {
            // Para salir
            finishAffinity()

                //POR SI MEJOR PONIAMOS PARA QUE FUERA A LA PANTALLA DE LOGIN PERO NO LO HICE BIEN
            /*
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            */
        }


        // Carga inicial: fragmento Insertar
        replaceFragment(InsertarCandidatoFragment())
        toolbar.title = "Insertar Candidatos"

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_crear -> {
                    replaceFragment(InsertarCandidatoFragment())
                    toolbar.title = "Insertar Candidatos"
                    true
                }
                R.id.nav_actualizar -> {
                    replaceFragment(ActualizarCandidatoFragment())
                    toolbar.title = "Actualizar Candidatos"
                    true
                }
                R.id.nav_eliminar -> {
                    replaceFragment(EliminarCandidatoFragment())
                    toolbar.title = "Eliminar Candidatos"
                    true
                }
                R.id.nav_activar_usuarios -> {
                    replaceFragment(ActivarUsuariosFragment())
                    toolbar.title = "Activar/Desactivar Usuarios"
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}

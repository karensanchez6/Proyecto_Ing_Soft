package com.example.pfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private var tipoUsuario: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        toolbar.setNavigationIcon(R.drawable.ic_logout)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        bottomNav = findViewById(R.id.bottom_nav)
        tipoUsuario = intent.getStringExtra("tipo_usuario")

        if (tipoUsuario == "admin") {
            startActivity(Intent(this, AdminActivity::class.java))
        } else {
            loadFragment(InicioFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (tipoUsuario == "admin") {
                        startActivity(Intent(this, AdminActivity::class.java))
                    } else {
                        loadFragment(InicioFragment())
                    }
                }

                R.id.nav_votar -> {
                    val prefs = getSharedPreferences("prefs_voto", MODE_PRIVATE)
                    val verificado = prefs.getBoolean("verificado", false)
                    val cedula = prefs.getString("cedula", null)

                    if (verificado && cedula != null) {
                        val fragment = PapeletaFragment().apply {
                            arguments = Bundle().apply {
                                putString("cedula", cedula)
                            }
                        }
                        loadFragment(fragment)
                    } else {
                        Toast.makeText(this, "Primero debes verificar tu cédula y PIN", Toast.LENGTH_SHORT).show()
                    }
                }

                R.id.nav_estadisticas -> loadFragment(EstadisticasFragment())
                R.id.nav_info -> loadFragment(SobreNosotrosFragment())
                R.id.nav_verifica -> loadFragment(VerificacionFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}

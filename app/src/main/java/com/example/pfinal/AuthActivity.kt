package com.example.pfinal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class AuthActivity : AppCompatActivity(), LoginFragment.LoginListener, RegisterFragment.RegisterListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Poner ícono de logout a la izquierda del toolbar
        toolbar.setNavigationIcon(R.drawable.ic_logout) // Asegúrate que tienes este drawable

        // Acción al presionar el ícono de logout
        toolbar.setNavigationOnClickListener {
            // Por ejemplo, volver a la pantalla de login o cerrar la app
            // Aquí puedes simplemente cerrar esta Activity para "salir" del Auth
            finishAffinity()
        }

        // Cargar LoginFragment por defecto
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, LoginFragment())
            .commit()
    }


    override fun onLoginSuccess(rol: String) {
        if (rol == "admin") {
            // Abrir AdminActivity para admin
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        } else {
            // Abrir MainActivity para usuarios normales
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onRegisterSuccess() {
        // Registro correcto, abrir MainActivity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun showRegister() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, RegisterFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, LoginFragment())
            .addToBackStack(null)
            .commit()
    }
}

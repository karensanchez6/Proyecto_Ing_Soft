package com.example.pfinal

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ConfirmacionDialogFragment : DialogFragment() {

    private lateinit var tvContador: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCerrar: Button

    private val duracionSegundos = 20
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout que ya tienes definido (dialog_confirmacion.xml)
        val view = inflater.inflate(R.layout.dialogo_cierre, container, false)

        tvContador = view.findViewById(R.id.tvContador)
        progressBar = view.findViewById(R.id.progressBar)
        btnCerrar = view.findViewById(R.id.btnCerrarPapeleta)

        progressBar.max = duracionSegundos
        progressBar.progress = 0

        btnCerrar.setOnClickListener {
            countDownTimer?.cancel()
            dismiss()
            cerrarPapeleta()
        }

        iniciarContador()

        return view
    }

    private fun iniciarContador() {
        countDownTimer = object : CountDownTimer(duracionSegundos * 1000L, 1000L) {
            var segundosRestantes = duracionSegundos
            override fun onTick(millisUntilFinished: Long) {
                segundosRestantes--
                tvContador.text = "La papeleta se cerrará en $segundosRestantes segundos..."
                progressBar.progress = duracionSegundos - segundosRestantes
            }

            override fun onFinish() {
                dismiss()
                cerrarPapeleta()
            }
        }
        countDownTimer?.start()
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        super.onDestroyView()
    }

    private fun cerrarPapeleta() {
        // Reemplaza por EstadisticasFragment
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, EstadisticasFragment())
            ?.commit()
    }
}

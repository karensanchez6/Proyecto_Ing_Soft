package com.example.pfinal

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class EstadisticasFragment : Fragment() {

    private lateinit var tvTotalVotos: TextView
    private lateinit var tvVotosPorCandidato: TextView
    private lateinit var barChart: BarChart
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_estadisticas, container, false)

        tvTotalVotos = view.findViewById(R.id.tvTotalVotos)
        tvVotosPorCandidato = view.findViewById(R.id.tvVotosPorCandidato)
        barChart = view.findViewById(R.id.barChart)
        dbHelper = DBHelper(requireContext())

        mostrarEstadisticas()

        return view
    }

    private fun mostrarEstadisticas() {
        val conteoVotos = dbHelper.obtenerConteoVotos()  // Map<Int, Int> idCandidato -> votos
        val candidatos = dbHelper.obtenerCandidatos()    // List<CandidatoData>

        val totalVotos = conteoVotos.values.sum()
        tvTotalVotos.text = "Total de votos: $totalVotos"

        // Mapear id candidato a nombre para mostrar
        val votosPorNombre = conteoVotos.mapNotNull { (idCandidato, votos) ->
            val candidato = candidatos.find { it.idCandidato == idCandidato }
            candidato?.let { it.nombre to votos }
        }

        // Mostrar lista textual
        val texto = StringBuilder()
        votosPorNombre.forEach { (nombre, votos) ->
            texto.append("$nombre: $votos votos\n")
        }
        tvVotosPorCandidato.text = texto.toString().trim()

        // Preparar datos del gráfico
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0f

        votosPorNombre.forEach { (nombre, votos) ->
            entries.add(BarEntry(index, votos.toFloat()))
            labels.add(nombre)
            index += 1f
        }

        val dataSet = BarDataSet(entries, "Votos por candidato")
        dataSet.colors = listOf(
            Color.rgb(66, 133, 244), // Azul
            Color.rgb(219, 68, 55),  // Rojo
            Color.rgb(244, 180, 0),  // Amarillo
            Color.rgb(15, 157, 88)   // Verde
        )
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 14f

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)
        barChart.setDrawValueAboveBar(true)
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)
        barChart.axisRight.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -30f
        xAxis.textSize = 12f

        val yAxis = barChart.axisLeft
        yAxis.axisMinimum = 0f
        yAxis.textSize = 12f

        barChart.invalidate()
    }

}

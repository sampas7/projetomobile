package com.example.sentinela

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HistoricoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico)


        val btnAbrirCalendario = findViewById<Button>(R.id.btnAbrirCalendario)


        btnAbrirCalendario.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecione a data do registro que deseja visualizar")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()


            datePicker.addOnPositiveButtonClickListener { dataEmMillis ->

                val dataFormatada = formatarData(dataEmMillis)
                Toast.makeText(this, "Buscando registro de: $dataFormatada", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "A função não foi implementada 100% ainda, pit", Toast.LENGTH_SHORT).show()
                //val intent = Intent(this, RegistroDetalheActivity::class.java)
                //intent.putExtra("DATA_SELECIONADA", dataEmMillis)
                //startActivity(intent)
            }

            datePicker.show(supportFragmentManager, "DatePicker")
        }
    }


    private fun formatarData(dataEmMillis: Long): String {
        val data = Date(dataEmMillis)
        val formatador = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatador.format(data)
    }
}
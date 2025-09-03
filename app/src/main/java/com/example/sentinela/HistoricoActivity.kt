package com.example.sentinela

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class HistoricoActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var txtMesAno: TextView
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val diasComRegistro = mutableSetOf<LocalDate>()
    private val fusoHorarioUTC = ZoneId.of("UTC")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico)

        calendarView = findViewById(R.id.calendarView)
        txtMesAno = findViewById(R.id.txtMesAno)
        progressBar = findViewById(R.id.progressBarHistorico)

        buscarTodosOsRegistros()
    }

    private fun buscarTodosOsRegistros() {
        progressBar.isVisible = true
        val uid = auth.currentUser?.uid ?: return

        db.collection("registros")
            .whereEqualTo("uid_do_dono", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val timestamp = doc.getLong("timestamp") ?: continue
                    val data = Date(timestamp).toInstant()
                        .atZone(fusoHorarioUTC)
                        .toLocalDate()
                    diasComRegistro.add(data)
                }
                progressBar.isVisible = false
                configurarCalendario()
            }
            .addOnFailureListener {
                progressBar.isVisible = false
                Toast.makeText(this, "Erro ao buscar registros.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarCalendario() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        val daysOfWeek = daysOfWeek()

        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)


        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
                val dotView = container.dotView

                if (data.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
                    val dataDoDia = data.date
                    dotView.isVisible = diasComRegistro.contains(dataDoDia)
                    container.view.setOnClickListener {
                        if (diasComRegistro.contains(dataDoDia)) {
                            val intent = Intent(this@HistoricoActivity, RegistroDetalheActivity::class.java)
                            val millis = dataDoDia.atStartOfDay(fusoHorarioUTC).toInstant().toEpochMilli()
                            intent.putExtra("DATA_SELECIONADA", millis)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@HistoricoActivity, "Nenhum registro neste dia.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    container.textView.visibility = View.INVISIBLE
                    dotView.isVisible = false
                }
            }
        }


        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                val daysOfWeek = container.titlesContainer.children.map { it as TextView }
                daysOfWeek.forEachIndexed { index, textView ->
                    val dayOfWeek = data.weekDays.first()[index].date.dayOfWeek
                    val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                    textView.text = title
                }
            }
        }


        calendarView.monthScrollListener = { month ->
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))
            txtMesAno.text = formatter.format(month.yearMonth)
        }
    }
}


class DayViewContainer(view: View) : ViewContainer(view) {
    val textView: TextView = view.findViewById(R.id.calendarDayText)
    val dotView: View = view.findViewById(R.id.dotView)
}


class MonthViewContainer(view: View) : ViewContainer(view) {
    val titlesContainer = view as LinearLayout
}
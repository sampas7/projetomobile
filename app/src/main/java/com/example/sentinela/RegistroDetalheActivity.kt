package com.example.sentinela

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RegistroDetalheActivity : AppCompatActivity() {

    private lateinit var txtDataRegistro: TextView
    private lateinit var imgFotoDetalhe: ImageView
    private lateinit var btnTocarAudioDetalhe: Button
    private lateinit var txtSemRegistro: TextView
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_detalhe)

        txtDataRegistro = findViewById(R.id.txtDataRegistro)
        imgFotoDetalhe = findViewById(R.id.imgFotoDetalhe)
        btnTocarAudioDetalhe = findViewById(R.id.btnTocarAudioDetalhe)
        txtSemRegistro = findViewById(R.id.txtSemRegistro)
        progressBar = findViewById(R.id.progressBarDetalhe)

        val dataSelecionadaMillis = intent.getLongExtra("DATA_SELECIONADA", -1)

        if (dataSelecionadaMillis == -1L) {
            Toast.makeText(this, "Erro ao carregar a data.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        Log.d("RegistroDetalhe", "Data recebida (UTC millis): $dataSelecionadaMillis")

        formatarTituloData(dataSelecionadaMillis)
        buscarRegistroNoFirestore(dataSelecionadaMillis)
    }

    private fun buscarRegistroNoFirestore(dataMillis: Long) {
        val uid = auth.currentUser?.uid ?: return
        // NOVO: Log para vermos o UID usado na busca
        Log.d("RegistroDetalhe", "Buscando para o UID: $uid")


        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = dataMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val inicioDoDia = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val fimDoDia = calendar.timeInMillis

        // NOVO: Log para vermos a janela de tempo exata que estamos buscando
        Log.d("RegistroDetalhe", "Janela de busca (UTC): Início=$inicioDoDia, Fim=$fimDoDia")

        db.collection("registros")
            .whereEqualTo("uid_do_dono", uid)
            .whereGreaterThanOrEqualTo("timestamp", inicioDoDia)
            .whereLessThanOrEqualTo("timestamp", fimDoDia)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE

                Log.d("RegistroDetalhe", "Busca concluída. Documentos encontrados: ${documents.size()}")

                if (documents.isEmpty) {
                    txtSemRegistro.visibility = View.VISIBLE
                } else {
                    val registro = documents.documents[0]
                    val urlFoto = registro.getString("url_da_foto")
                    val urlAudio = registro.getString("url_do_audio")

                    if (urlFoto != null) {
                        Glide.with(this).load(urlFoto).into(imgFotoDetalhe)
                    } else {
                        imgFotoDetalhe.visibility = View.GONE
                    }

                    if (urlAudio != null) {
                        btnTocarAudioDetalhe.visibility = View.VISIBLE
                        prepararAudioPlayer(urlAudio)
                    }
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                txtSemRegistro.visibility = View.VISIBLE
                // NOVO: Log de erro mais detalhado
                Log.e("RegistroDetalhe", "Erro ao buscar registro no Firestore", exception)
                Toast.makeText(this, "Erro ao buscar registro: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun prepararAudioPlayer(url: String) {
        btnTocarAudioDetalhe.setOnClickListener {
            Toast.makeText(this, "Iniciando áudio...", Toast.LENGTH_SHORT).show()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { it.start() }
                prepareAsync()
            }
        }
    }

    private fun formatarTituloData(dataMillis: Long) {
        val data = Date(dataMillis)
        val formatador = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        txtDataRegistro.text = "Registro de ${formatador.format(data)}"
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
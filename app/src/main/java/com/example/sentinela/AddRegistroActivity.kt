package com.example.sentinela

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class AddRegistroActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnSelecionarFoto: Button
    private lateinit var btnGravarAudio: Button
    private lateinit var btnOuvirAudio: Button
    private lateinit var btnApagarAudio: Button
    private lateinit var txtContador: TextView

    private lateinit var audioManager: AudioManager

    private var contador = 0
    private var timer: CountDownTimer? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startRecording() else Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_registro)

        audioManager = AudioManager(this)

        imgPreview = findViewById(R.id.imgPreview)
        btnSelecionarFoto = findViewById(R.id.btnSelecionarFoto)
        btnGravarAudio = findViewById(R.id.btnGravarAudio)
        btnOuvirAudio = findViewById(R.id.btnOuvirAudio)
        btnApagarAudio = findViewById(R.id.btnApagarAudio)

        txtContador = TextView(this).apply {
            text = "0s"
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.white))
        }

        (btnGravarAudio.parent as RelativeLayout).addView(txtContador)

        val params = txtContador.layoutParams as RelativeLayout.LayoutParams
        params.addRule(RelativeLayout.RIGHT_OF, R.id.btnGravarAudio)
        params.setMargins(16, 0, 0, 0)
        txtContador.layoutParams = params

        btnGravarAudio.setOnClickListener {
            if (audioManager.isRecording) {
                audioManager.stopRecording()
                btnGravarAudio.text = "Gravar Áudio"
                stopTimer()
            } else {
                checkPermissionAndStart()
            }
        }

        btnOuvirAudio.setOnClickListener {
            if (audioManager.isRecording) {
                Toast.makeText(this, "Pare a gravação antes de ouvir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sucesso = audioManager.playAudio()
            if (!sucesso) {
                Toast.makeText(this, "Nenhuma gravação encontrada", Toast.LENGTH_SHORT).show()
            }
        }

        btnApagarAudio.setOnClickListener {
            if (audioManager.isRecording) {
                Toast.makeText(this, "Pare a gravação antes de apagar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val apagou = audioManager.deleteAudio()
            if (apagou) {
                Toast.makeText(this, "Gravação apagada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nenhuma gravação pra apagar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        val sucesso = audioManager.startRecording()
        if (sucesso) {
            btnGravarAudio.text = "Parar Gravação"
            startTimer()
        } else {
            Toast.makeText(this, "Erro ao iniciar gravação", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer() {
        contador = 0
        txtContador.text = "0s"
        timer = object : CountDownTimer(999000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                contador++
                txtContador.text = "${contador}s"
            }

            override fun onFinish() {}
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        txtContador.text = "0s"
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
    }
}

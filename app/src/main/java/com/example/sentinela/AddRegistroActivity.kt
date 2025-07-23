package com.example.sentinela

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class AddRegistroActivity : AppCompatActivity() {

    private val PREFS_NAME = "RegistroDiarioPrefs"
    private val KEY_URI_FOTO = "uri_foto"
    private val KEY_PATH_AUDIO = "path_audio"

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var imgPreview: ImageView
    private lateinit var btnSelecionarFoto: Button
    private lateinit var btnGravarAudio: Button
    private lateinit var btnOuvirAudio: Button
    private lateinit var btnApagarAudio: Button
    private lateinit var txtContador: TextView

    private lateinit var audioManager: AudioManager
    private lateinit var imageManager: ImageManager

    private var contador = 0
    private var timer: CountDownTimer? = null

    // Permissão do microfone
    private val requestMicrophonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startRecording()
        else Toast.makeText(this, "Permissão de microfone negada", Toast.LENGTH_SHORT).show()
    }

    // Permissão da câmera
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imageManager.openCamera(cameraLauncher)
        } else {
            Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            imgPreview.setImageURI(imageManager.currentPhotoUri)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            it.data?.data?.let { uri ->

                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)

                imageManager.currentPhotoUri = uri
                imgPreview.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_registro)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        audioManager = AudioManager(this)
        imageManager = ImageManager(this)

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
                audioManager.stopPlayback()
                checkMicrophonePermissionAndStart()
            }
        }

        btnOuvirAudio.setOnClickListener {
            if (audioManager.isRecording) {
                Toast.makeText(this, "Pare a gravação antes de ouvir, dog", Toast.LENGTH_SHORT).show()
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
            if (audioManager.deleteAudio()) {
                sharedPreferences.edit().remove(KEY_PATH_AUDIO).apply()
                Toast.makeText(this, "Gravação apagada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nenhuma gravação pra apagar", Toast.LENGTH_SHORT).show()
            }
        }

        btnSelecionarFoto.setOnClickListener {
            if (audioManager.isRecording) {
                Toast.makeText(this, "Pare a gravação antes de selecionar uma foto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val popup = PopupMenu(this, btnSelecionarFoto)
            popup.menu.add("Tirar Foto")
            popup.menu.add("Escolher da Galeria")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Tirar Foto" -> {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    "Escolher da Galeria" -> imageManager.openGallery(galleryLauncher)
                }
                true
            }

            popup.show()
        }

        carregarEstado()
    }

    private fun carregarEstado() {
        val uriFotoString = sharedPreferences.getString(KEY_URI_FOTO, null)
        if (uriFotoString != null) {
            val uriFoto = Uri.parse(uriFotoString)
            imageManager.currentPhotoUri = uriFoto
            imgPreview.setImageURI(uriFoto)
        }

        val pathAudioSalvo = sharedPreferences.getString(KEY_PATH_AUDIO, null)
        audioManager.setAudioPath(pathAudioSalvo)
    }

    private fun salvarEstado() {
        val editor = sharedPreferences.edit()

        // pra salvar a foto
        val uriFotoString = imageManager.currentPhotoUri?.toString()
        editor.putString(KEY_URI_FOTO, uriFotoString)

        // vai pegar o caminho do áudio do AudioManager e salvar
        val pathAudio = audioManager.getAudioPath()
        editor.putString(KEY_PATH_AUDIO, pathAudio)

        editor.apply()
    }

    override fun onStop() {
        super.onStop()
        salvarEstado()
    }

    private fun checkMicrophonePermissionAndStart() {
        requestMicrophonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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

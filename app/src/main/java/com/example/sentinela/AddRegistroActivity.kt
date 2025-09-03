package com.example.sentinela

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.*
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import java.util.Locale
import java.util.concurrent.TimeUnit

class AddRegistroActivity : AppCompatActivity() {

    // Constantes e Variáveis de estado
    private val PREFS_NAME = "RegistroDiarioPrefs"
    private val KEY_URI_FOTO = "uri_foto"
    private val KEY_PATH_AUDIO = "path_audio"
    private lateinit var sharedPreferences: SharedPreferences

    // Componentes da UI
    private lateinit var cardImgPreview: CardView
    private lateinit var imgPreview: ImageView
    private lateinit var txtPlaceholderFoto: TextView
    private lateinit var lottieRecordButton: LottieAnimationView
    private lateinit var playerContainer: LinearLayout
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnDeleteAudio: ImageButton
    private lateinit var seekBarAudio: SeekBar
    private lateinit var txtContadorGravacao: TextView

    // Managers e Timer
    private lateinit var audioManager: AudioManager
    private lateinit var imageManager: ImageManager
    private var timer: CountDownTimer? = null

    // Launchers
    private val requestMicrophonePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startRecording()
        else Toast.makeText(this, "Permissão de microfone negada", Toast.LENGTH_SHORT).show()
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) imageManager.openCamera(cameraLauncher)
        else Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show()
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageManager.currentPhotoUri?.let {
                imgPreview.setImageURI(it)
                txtPlaceholderFoto.visibility = View.GONE
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)

                imageManager.currentPhotoUri = uri
                imgPreview.setImageURI(uri)
                txtPlaceholderFoto.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_registro)

        // Inicializações
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        audioManager = AudioManager(this)
        imageManager = ImageManager(this)

        // Conexão com o Layout
        cardImgPreview = findViewById(R.id.cardImgPreview)
        imgPreview = findViewById(R.id.imgPreview)
        txtPlaceholderFoto = findViewById(R.id.txtPlaceholderFoto)
        lottieRecordButton = findViewById(R.id.lottieRecordButton)
        playerContainer = findViewById(R.id.playerContainer)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnDeleteAudio = findViewById(R.id.btnDeleteAudio)
        seekBarAudio = findViewById(R.id.seekBarAudio)
        txtContadorGravacao = findViewById(R.id.txtContadorGravacao)

        // Configuração da Lógica
        configurarLogicaDaFoto()
        configurarLogicaDoAudio()
        carregarEstado()
    }

    private fun configurarLogicaDaFoto() {
        cardImgPreview.setOnClickListener {
            if (audioManager.isRecording) {
                Toast.makeText(this, "Pare a gravação antes...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val popup = PopupMenu(this, it)
            popup.menu.add("Tirar Foto")
            popup.menu.add("Escolher da Galeria")
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Tirar Foto" -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    "Escolher da Galeria" -> imageManager.openGallery(galleryLauncher)
                }
                true
            }
            popup.show()
        }
    }

    private fun configurarLogicaDoAudio() {
        lottieRecordButton.setOnClickListener {
            if (audioManager.isRecording) {
                vibrar()
                audioManager.stopRecording()
                lottieRecordButton.pauseAnimation()
                lottieRecordButton.progress = 0f
                stopTimer()
                atualizarUiAudio()
            } else {
                audioManager.stopPlayback()
                checkMicrophonePermissionAndStart()
            }
        }

        btnPlayPause.setOnClickListener {
            if (audioManager.isPlaying) {
                audioManager.stopPlayback()
                btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                val sucesso = audioManager.playAudio(
                    onProgress = { progress, max ->
                        seekBarAudio.max = max
                        seekBarAudio.progress = progress
                    },
                    onCompletion = {
                        btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24)
                        seekBarAudio.progress = 0
                    }
                )
                if (sucesso) {
                    btnPlayPause.setImageResource(R.drawable.baseline_pause_24)
                } else {
                    Toast.makeText(this, "Não foi possível tocar o áudio.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnDeleteAudio.setOnClickListener {
            audioManager.deleteAudio()
            sharedPreferences.edit().remove(KEY_PATH_AUDIO).apply()
            atualizarUiAudio()
            Toast.makeText(this, "Gravação apagada", Toast.LENGTH_SHORT).show()
        }
    }


    private fun atualizarUiAudio() {
        val temAudio = audioManager.getAudioPath() != null
        if (temAudio) {
            // se tem áudio, some com o botão de gravar e aparece com o player
            crossfadeViews(lottieRecordButton, playerContainer)
        } else {
            // se n tem áudio, some com o player e aparece com o botão de gravar
            crossfadeViews(playerContainer, lottieRecordButton)
        }
    }


    private fun crossfadeViews(viewToFadeOut: View, viewToFadeIn: View) {
        val duration = 150L // duração da animação em milissegundos

        viewToFadeOut.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                viewToFadeOut.visibility = View.GONE
            }
            .start()

        viewToFadeIn.alpha = 0f
        viewToFadeIn.visibility = View.VISIBLE
        viewToFadeIn.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }

    private fun carregarEstado() {
        val uriFotoString = sharedPreferences.getString(KEY_URI_FOTO, null)
        if (uriFotoString != null) {
            val uriFoto = Uri.parse(uriFotoString)
            imageManager.currentPhotoUri = uriFoto
            imgPreview.setImageURI(uriFoto)
            txtPlaceholderFoto.visibility = View.GONE
        } else {
            txtPlaceholderFoto.visibility = View.VISIBLE
        }
        val pathAudioSalvo = sharedPreferences.getString(KEY_PATH_AUDIO, null)
        audioManager.setAudioPath(pathAudioSalvo)
        atualizarUiAudio()
    }

    private fun salvarEstado() {
        val editor = sharedPreferences.edit()
        val uriFotoString = imageManager.currentPhotoUri?.toString()
        editor.putString(KEY_URI_FOTO, uriFotoString)
        val pathAudio = audioManager.getAudioPath()
        editor.putString(KEY_PATH_AUDIO, pathAudio)
        editor.apply()
    }

    private fun vibrar() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    override fun onStop() {
        super.onStop()
        salvarEstado()
    }

    private fun checkMicrophonePermissionAndStart() {
        requestMicrophonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startRecording() {
        vibrar()
        val sucesso = audioManager.startRecording()
        if (sucesso) {
            lottieRecordButton.playAnimation()
            startTimer()
        } else {
            Toast.makeText(this, "Erro ao iniciar gravação", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer() {
        txtContadorGravacao.visibility = View.VISIBLE
        var currentSeconds = 0
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currentSeconds++
                val minutes = TimeUnit.SECONDS.toMinutes(currentSeconds.toLong())
                val seconds = currentSeconds % 60
                txtContadorGravacao.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        txtContadorGravacao.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.release()
    }
}
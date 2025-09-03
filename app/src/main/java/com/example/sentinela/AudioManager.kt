package com.example.sentinela

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import java.io.File

class AudioManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    var audioFile: File? = null
        private set
    var isRecording = false
        private set
    var isPlaying = false
        private set


    private var handler = Handler(Looper.getMainLooper())
    private lateinit var updateSeekBarRunnable: Runnable

    fun getAudioPath(): String? {
        return audioFile?.absolutePath
    }

    fun setAudioPath(path: String?) {
        if (path == null) {
            audioFile = null
            return
        }
        val file = File(path)
        if (file.exists()) {
            audioFile = file
        }
    }

    fun stopPlayback() {
        if (!isPlaying) return
        try {
            // MUDANÇA: Para o loop de atualização da seekbar
            handler.removeCallbacks(updateSeekBarRunnable)
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            isPlaying = false
        }
    }

    fun startRecording(): Boolean {
        // Sem mudanças aqui
        return try {
            val fileName = "audio_${System.currentTimeMillis()}.m4a"
            audioFile = File(context.externalCacheDir, fileName)

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            false
        }
    }

    fun stopRecording() {
        // Sem mudanças aqui
        if (!isRecording) return
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }

    fun playAudio(
        onProgress: (progress: Int, max: Int) -> Unit,
        onCompletion: () -> Unit
    ): Boolean {
        if (isRecording || isPlaying) return false
        val file = audioFile ?: return false
        if (!file.exists()) return false

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)


                setOnPreparedListener { player ->
                    this@AudioManager.isPlaying = true
                    player.start()


                    updateSeekBarRunnable = Runnable {

                        onProgress(player.currentPosition, player.duration)
                        // Agenda a tarefa para rodar de novo daqui a 100 milissegundos
                        handler.postDelayed(updateSeekBarRunnable, 100)
                    }

                    handler.post(updateSeekBarRunnable)
                }


                setOnCompletionListener {
                    this@AudioManager.isPlaying = false
                    // Para o loop de atualização
                    handler.removeCallbacks(updateSeekBarRunnable)

                    onCompletion()
                    mediaPlayer = null
                }

                // Prepara o áudio sem travar a tela
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun deleteAudio(): Boolean {

        stopPlayback()
        val deleted = audioFile?.delete() ?: false
        if (deleted) {
            audioFile = null
        }
        return deleted
    }

    fun release() {

        stopRecording()
        stopPlayback()
    }
}
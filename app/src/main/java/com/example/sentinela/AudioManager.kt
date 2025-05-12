package com.example.sentinela

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import java.io.File

class AudioManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    var audioFile: File? = null
        private set
    var isRecording = false
        private set

    fun startRecording(): Boolean {
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

    fun playAudio(): Boolean {
        if (isRecording) return false
        val file = audioFile ?: return false
        if (!file.exists()) return false

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun deleteAudio(): Boolean {
        val deleted = audioFile?.delete() ?: false
        if (deleted) {
            audioFile = null
        }
        return deleted
    }

    fun release() {
        try {
            mediaRecorder?.release()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        mediaPlayer = null
    }
}

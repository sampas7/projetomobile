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
    var isPlaying = false
        private set

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
        if (isRecording || isPlaying) return false
        val file = audioFile ?: return false
        if (!file.exists()) return false

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
                this@AudioManager.isPlaying = true
                setOnCompletionListener {
                    this@AudioManager.isPlaying = false
                    mediaPlayer = null
                }
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
        stopPlayback()
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
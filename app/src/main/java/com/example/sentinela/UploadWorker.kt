package com.example.sentinela

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Date

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val PREFS_NAME = "RegistroDiarioPrefs"
    private val KEY_URI_FOTO = "uri_foto"
    private val KEY_PATH_AUDIO = "path_audio"

    override suspend fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriFotoString = sharedPreferences.getString(KEY_URI_FOTO, null)
        val pathAudioString = sharedPreferences.getString(KEY_PATH_AUDIO, null)

        if (uriFotoString == null && pathAudioString == null) {
            Log.d("UploadWorker", "Nenhum dado salvo para upload. Trabalho concluído.")
            return Result.success()
        }

        try {
            if (uriFotoString != null && pathAudioString != null) {
                Log.d("UploadWorker", "Foto e Áudio encontrados. Tentando upload...")
                val firebaseManager = FirebaseStorageManager()

                val uriFoto = Uri.parse(uriFotoString)
                val uriAudio = Uri.parse("file://$pathAudioString")

                firebaseManager.uploadRegistro(
                    data = Date(),
                    anotacao = "Upload automático do dia.",
                    uriDaFoto = uriFoto,
                    uriDoAudio = uriAudio
                )
                Log.d("UploadWorker", "Upload (tentativa) finalizado.")
            } else {
                Log.d("UploadWorker", "Faltando foto ou áudio. Upload cancelado, preparando para limpar.")
            }
        } catch (e: Exception) {
            Log.e("UploadWorker", "Falha durante a tentativa de upload.", e)
        } finally {
            Log.d("UploadWorker", "Limpando SharedPreferences para o próximo dia.")
            sharedPreferences.edit().clear().apply()
        }

        return Result.success()
    }
}
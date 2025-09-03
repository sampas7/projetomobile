package com.example.sentinela

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.*

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
            Log.d("UploadWorker", "Dados encontrados. Tentando upload com o que houver...")
            val firebaseManager = FirebaseStorageManager()

            val uriFoto = uriFotoString?.let { Uri.parse(it) }
            val uriAudio = pathAudioString?.let { Uri.parse("file://$it") }


            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))


            if (calendar.get(Calendar.HOUR_OF_DAY) < 3) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }


            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val dataDoRegistro = calendar.time

            firebaseManager.uploadRegistro(
                data = dataDoRegistro, // Usamos a data corrigida
                anotacao = "Upload automático do dia.",
                uriDaFoto = uriFoto,
                uriDoAudio = uriAudio
            )
            Log.d("UploadWorker", "Upload (tentativa) finalizado para a data: $dataDoRegistro")

        } catch (e: Exception) {
            Log.e("UploadWorker", "Falha durante a tentativa de upload.", e)
        } finally {
            Log.d("UploadWorker", "Limpando SharedPreferences para o próximo dia.")
            sharedPreferences.edit().clear().apply()
        }

        return Result.success()
    }
}
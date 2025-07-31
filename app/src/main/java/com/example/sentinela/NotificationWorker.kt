package com.example.sentinela

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val PREFS_NAME = "RegistroDiarioPrefs"
    private val KEY_URI_FOTO = "uri_foto"
    private val KEY_PATH_AUDIO = "path_audio"

    override suspend fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val temFoto = sharedPreferences.getString(KEY_URI_FOTO, null) != null
        val temAudio = sharedPreferences.getString(KEY_PATH_AUDIO, null) != null

        // REGRA: Se tiver os dois, não faz nada.
        if (temFoto && temAudio) {
            Log.d("NotificationWorker", "Usuário já registrou tudo. Nenhuma notificação enviada.")
            return Result.success()
        }

        val titulo = "Lembrete do Sentinela"
        val mensagem: String


        when {
            // só tem foto
            temFoto && !temAudio -> {
                val mensagens = listOf(
                    "Ei, que tal gravar o áudio de hoje? A foto já tá aqui!",
                    "Falta pouco! Só gravar o áudio de hoje pra completar o registro.",
                    "Sua foto tá on, mas e o áudio? Bora gravar rapidinho."
                )
                mensagem = mensagens.random()
            }
            // somente o áudio
            !temFoto && temAudio -> {
                val mensagens = listOf(
                    "Opa, a imagem de hoje ainda não foi registrada, hein.",
                    "Seu áudio tá salvo! Falta só a foto pra fechar o dia.",
                    "Quase lá! Só falta mandar a foto de hoje."
                )
                mensagem = mensagens.random()
            }
            // se nao tiver nada
            else -> {
                val mensagens = listOf(
                    "E aí, bora... registrar? Você ainda não adicionou nada hoje.",
                    "Seu diário tá esperando! Que tal fazer o registro diário?",
                    "Lembrete amigável para você não esquecer de registrar seu dia.",
                    "Coloca uma ward aqui, vamos fazer o registro de hoje."
                )
                mensagem = mensagens.random()
            }
        }

        // chama pra notificar
        Log.d("NotificationWorker", "Enviando notificação: $mensagem")
        NotificationHelper.showNotification(applicationContext, titulo, mensagem)

        return Result.success()
    }
}
package com.example.sentinela

import android.app.Application
import android.util.Log
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SentinelaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        agendarUploadDiario()
        agendarLembretesDiarios()
        NotificationHelper.createNotificationChannel(this)

    }

    private fun agendarUploadDiario() {
        // vai calcular o tempo que falta até as 23:59 de hoje
        val calendar = Calendar.getInstance()
        val agoraEmMillis = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 0)


        if (calendar.timeInMillis < agoraEmMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val atrasoInicial = calendar.timeInMillis - agoraEmMillis

        // parte que faz a função ser ativada a cada 24h
        val uploadRequest = PeriodicWorkRequestBuilder<UploadWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(atrasoInicial, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Só roda se tiver internet
                    .build()
            )
            .build()

        // vai evitar criar duplicatas
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "UploadDiarioSentinela",
            ExistingPeriodicWorkPolicy.KEEP, // se já existir, mantém o agendamento antigo
            uploadRequest
        )
    }

    private fun agendarLembretesDiarios() {
        agendarLembreteUnico("Lembrete1245", 12, 45)
        agendarLembreteUnico("Lembrete2000", 20, 0)
        agendarLembreteUnico("Lembrete2300", 23, 0)
    }

    private fun agendarLembreteUnico(workName: String, hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        val agoraEmMillis = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis < agoraEmMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val atrasoInicial = calendar.timeInMillis - agoraEmMillis

        val lembreteRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(atrasoInicial, TimeUnit.MILLISECONDS)
            .build()

        // evitar duplicatas dnv
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP, // se já existir uma tarefa com esse nome, mantém a antiga
            lembreteRequest
        )
    }
}
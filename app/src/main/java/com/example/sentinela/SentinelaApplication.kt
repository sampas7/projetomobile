package com.example.sentinela

import android.app.Application
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SentinelaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        agendarUploadDiario()
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
            ExistingPeriodicWorkPolicy.KEEP, // Se já existir, mantém o agendamento antigo
            uploadRequest
        )
    }
}
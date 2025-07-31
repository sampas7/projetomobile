package com.example.sentinela

// NOVO: Imports necessários para a permissão de notificação e Toast
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // NOVO: Launcher para pedir a permissão de notificação e lidar com a resposta do usuário
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // O usuário permitiu!
            Toast.makeText(this, "Ótimo! Você receberá nossos lembretes.", Toast.LENGTH_SHORT).show()
        } else {
            // O usuário negou.
            Toast.makeText(this, "Ok, você não receberá lembretes.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // NOVO: Chama nossa função para pedir a permissão assim que a tela é criada
        pedirPermissaoDeNotificacao()

        // O resto do seu código do onCreate continua igual
        val logoSentinela = findViewById<ImageView>(R.id.logoSentinela)
        val containerBotoes = findViewById<LinearLayout>(R.id.containerBotoes)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        logoSentinela.startAnimation(fadeIn)
        containerBotoes.startAnimation(slideUp)

        val btnAddRegistro = findViewById<Button>(R.id.btnAddRegistro)
        val btnVerRegistros = findViewById<Button>(R.id.btnVerRegistros)
        val btnSair = findViewById<Button>(R.id.btnSair)

        btnAddRegistro.setOnClickListener {
            val intent = Intent(this, AddRegistroActivity::class.java)
            startActivity(intent)
        }

        btnVerRegistros.setOnClickListener {
            val intent = Intent(this, HistoricoActivity::class.java)
            startActivity(intent)
        }

        btnSair.setOnClickListener {
            val sharedPreferences = getSharedPreferences("RegistroDiarioPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    private fun pedirPermissaoDeNotificacao() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // permissão já concedida, não precisa fazer nada
                // Log.d("MainActivity", "Permissão de notificação já concedida.")
            } else {
                // Se não tem a permissão, a gente pede.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
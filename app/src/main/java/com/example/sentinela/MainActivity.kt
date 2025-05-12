package com.example.sentinela

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logoSentinela = findViewById<ImageView>(R.id.logoSentinela)
        val containerBotoes = findViewById<LinearLayout>(R.id.containerBotoes)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        logoSentinela.startAnimation(fadeIn)
        containerBotoes.startAnimation(slideUp)

        val btnAddRegistro = findViewById<Button>(R.id.btnAddRegistro)
        val btnVerRegistros = findViewById<Button>(R.id.btnVerRegistros)

        btnAddRegistro.setOnClickListener {
            val intent = Intent(this, AddRegistroActivity::class.java)
            startActivity(intent)
        }

        btnVerRegistros.setOnClickListener {

        }
    }
}

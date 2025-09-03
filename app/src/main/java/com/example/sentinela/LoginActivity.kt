package com.example.sentinela

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var edtEmail: EditText
    private lateinit var edtSenha: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtIrRegistro: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtEsqueciSenha: TextView

    private var senhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)
        btnLogin = findViewById(R.id.btnLogin)
        txtIrRegistro = findViewById(R.id.txtIrRegistro)
        progressBar = findViewById(R.id.progressBarLogin)
        txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha)

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val senha = edtSenha.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                btnLogin.isEnabled = false
                progressBar.visibility = View.VISIBLE

                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->

                        btnLogin.isEnabled = true
                        progressBar.visibility = View.GONE

                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login feito com sucesso!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Email ou senha inválidos. Tente novamente.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        txtIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // NOVO: Listener para o texto "Esqueci minha senha"
        txtEsqueciSenha.setOnClickListener {
            mostrarDialogoEsqueciSenha()
        }
    }

    private fun mostrarDialogoEsqueciSenha() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Redefinir Senha")

        val input = EditText(this)
        input.hint = "Digite seu email de cadastro"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Enviar") { dialog, _ ->
            val email = input.text.toString()
            if (email.isNotEmpty()) {
                enviarEmailDeRedefinicao(email)
            } else {
                Toast.makeText(this, "Por favor, digite seu email.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

        private fun enviarEmailDeRedefinicao(email: String) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Email de redefinição enviado para $email",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Falha ao enviar email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }


    override fun onStart() {
        super.onStart()
        val usuarioAtual = auth.currentUser
        if (usuarioAtual != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

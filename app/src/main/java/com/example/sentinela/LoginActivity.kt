package com.example.sentinela

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var edtEmail: EditText
    private lateinit var edtSenha: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtIrRegistro: TextView
    private lateinit var imgToggleSenha: ImageView

    private var senhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)
        btnLogin = findViewById(R.id.btnLogin)
        txtIrRegistro = findViewById(R.id.txtIrRegistro)
        imgToggleSenha = findViewById(R.id.imgToggleSenha)


        imgToggleSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                edtSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imgToggleSenha.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                edtSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imgToggleSenha.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            edtSenha.setSelection(edtSenha.text.length) // Mantém o cursor no final
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val senha = edtSenha.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login feito com sucesso!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        txtIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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

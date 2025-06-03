package com.example.sentinela

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var edtEmail: EditText
    private lateinit var edtSenha: EditText
    private lateinit var edtConfirmaSenha: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var txtIrLogin: TextView
    private lateinit var btnToggleSenha: ImageView
    private lateinit var btnToggleConfirmaSenha: ImageView

    private var senhaVisivel = false
    private var confirmaSenhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        edtEmail = findViewById(R.id.edtEmail)
        edtSenha = findViewById(R.id.edtSenha)
        edtConfirmaSenha = findViewById(R.id.edtConfirmaSenha)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        txtIrLogin = findViewById(R.id.txtIrLogin)
        btnToggleSenha = findViewById(R.id.imgToggleSenha)
        btnToggleConfirmaSenha = findViewById(R.id.imgToggleConfirmaSenha)

        // Toggle senha
        btnToggleSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                edtSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnToggleSenha.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                edtSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnToggleSenha.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            edtSenha.setSelection(edtSenha.text.length)
        }


        btnToggleConfirmaSenha.setOnClickListener {
            confirmaSenhaVisivel = !confirmaSenhaVisivel
            if (confirmaSenhaVisivel) {
                edtConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnToggleConfirmaSenha.setImageResource(R.drawable.baseline_visibility_24)
            } else {
                edtConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnToggleConfirmaSenha.setImageResource(R.drawable.baseline_visibility_off_24)
            }
            edtConfirmaSenha.setSelection(edtConfirmaSenha.text.length)
        }



        btnRegistrar.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString().trim()
            val confirmaSenha = edtConfirmaSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty() || confirmaSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else if (senha != confirmaSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            } else if (senha.length < 6) {
                Toast.makeText(this, "Senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


        txtIrLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

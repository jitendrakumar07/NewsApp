package com.example.newsapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var loginEmail: TextInputEditText
    private lateinit var loginPassword: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var errorMessage: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        loginEmail = findViewById(R.id.admin_email)
        loginPassword = findViewById(R.id.admin_password)
        loginButton = findViewById(R.id.admin_login_button)
        errorMessage = findViewById(R.id.error_message)

        loginButton.setOnClickListener { loginUser() }

        findViewById<TextView>(R.id.register_new_account).setOnClickListener {
            handleRegister()
        }

        // Add TextWatchers to clear error message on input
        addInputListeners()
    }

    private fun loginUser() {
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and Password must not be empty")
            return
        }

        // Firebase Authentication to log in the user
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, redirect to the main activity or home
                    "Login Successful".showToast()
                    startActivity(Intent(this, ArticlePostActivity::class.java))
                    finish()
                } else {
                    // If sign-in fails, display a message to the user
                    showError("Authentication Failed: ${task.exception?.message ?: "Unknown error"}")
                }
            }
    }

    private fun handleRegister() {
        // Navigate to registration activity
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun showError(message: String) {
        errorMessage.text = message
        errorMessage.visibility = TextView.VISIBLE
    }

    private fun String.showToast() {
        Toast.makeText(this@LoginActivity, this, Toast.LENGTH_SHORT).show()
    }

    private fun addInputListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                errorMessage.visibility = TextView.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        loginEmail.addTextChangedListener(textWatcher)
        loginPassword.addTextChangedListener(textWatcher)
    }
}
